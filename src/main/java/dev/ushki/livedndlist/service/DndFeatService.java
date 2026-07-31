package dev.ushki.livedndlist.service;

import static dev.ushki.livedndlist.service.DndClassService.getSyncResultDto;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eFeatDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5ePaginatedResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.response.DndFeatResponse;
import dev.ushki.livedndlist.entity.dndCharacter.DndFeat;
import dev.ushki.livedndlist.enums.SyncAction;
import dev.ushki.livedndlist.mapper.FeatMapper;
import dev.ushki.livedndlist.repository.DndFeatRepository;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import dev.ushki.livedndlist.service.sync.SyncProgressTracker;
import dev.ushki.livedndlist.service.sync.SyncResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DndFeatService {

  private static final String API_PATH = "/v2/feats/";

  private final DndFeatRepository featRepository;
  private final FeatMapper featMapper;
  private final Open5eApiClient apiClient;
  private final SyncMetrics syncMetrics;

  private final SyncProgressTracker progressTracker = new SyncProgressTracker();

  public SyncStatusDto getSyncStatus() {
    return progressTracker.getStatus();
  }

  @Transactional
  public SyncResultDto syncAllFeats() {
    String taskId = UUID.randomUUID().toString();

    if (!progressTracker.tryStart()) {
      return buildAlreadyInProgressResult(taskId);
    }

    long startTime = System.currentTimeMillis();
    SyncResult result = new SyncResult();

    try {
      syncMetrics.startOperation();
      progressTracker.setOperation("Fetching data from API");
      log.info("Starting feat sync from Open5e API");

      List<Open5eFeatDto> allFeats = fetchAllFromApi();
      progressTracker.setTotal(allFeats.size());

      log.info("Fetched {} feats from API", allFeats.size());
      progressTracker.setOperation("Saving to database");

      for (Open5eFeatDto dto : allFeats) {
        long itemStart = System.currentTimeMillis();
        processFeat(dto, result);
        long itemDuration = System.currentTimeMillis() - itemStart;
        syncMetrics.recordRequest(itemDuration, true);
        progressTracker.incrementProcessed();
      }

      long duration = System.currentTimeMillis() - startTime;
      log.info("Sync completed in {}ms. Created: {}, Updated: {}, Failed: {}",
          duration, result.getCreated(), result.getUpdated(), result.getFailed());

      return buildSuccessResult(result, allFeats.size(), duration, taskId);

    } catch (Exception e) {
      syncMetrics.recordRequest(System.currentTimeMillis() - startTime, false);
      log.error("Critical sync error: {}", e.getMessage(), e);
      return buildErrorResult(e, taskId);

    } finally {
      syncMetrics.endOperation();
      progressTracker.finish();
    }
  }

  @Transactional
  public SyncResultDto clearAll() {
    try {
      long count = featRepository.count();
      featRepository.deleteAll();

      return SyncResultDto.builder()
          .success(true)
          .message("Deleted feats: " + count)
          .syncedAt(LocalDateTime.now())
          .build();
    } catch (Exception e) {
      return SyncResultDto.builder()
          .success(false)
          .message("Delete error: " + e.getMessage())
          .syncedAt(LocalDateTime.now())
          .build();
    }
  }

  private List<Open5eFeatDto> fetchAllFromApi() {
    return apiClient.fetchAll(
        API_PATH,
        new ParameterizedTypeReference<Open5ePaginatedResponse<Open5eFeatDto>>() {
        }
    );
  }

  private void processFeat(Open5eFeatDto dto, SyncResult result) {
    try {
      SyncAction action = saveOrUpdate(dto);
      if (action == SyncAction.CREATED) {
        result.recordCreated();
      } else {
        result.recordUpdated();
      }
    } catch (Exception e) {
      result.recordError(dto.getName(), e);
      log.error("Error processing feat '{}': {}", dto.getName(), e.getMessage(), e);
    }
  }

  private SyncAction saveOrUpdate(Open5eFeatDto dto) {
    Optional<DndFeat> existing = featRepository.findByKey(dto.getKey());

    if (existing.isPresent()) {
      DndFeat feat = existing.get();
      featMapper.updateEntity(feat, dto);
      featRepository.save(feat);
      return SyncAction.UPDATED;
    } else {
      DndFeat feat = featMapper.toEntity(dto);
      featRepository.save(feat);
      return SyncAction.CREATED;
    }
  }

  private SyncResultDto buildAlreadyInProgressResult(String taskId) {
    return SyncResultDto.builder()
        .taskId(taskId)
        .success(false)
        .message("Sync already in progress")
        .syncedAt(LocalDateTime.now())
        .build();
  }

  private SyncResultDto buildSuccessResult(SyncResult result, int totalFetched, long duration,
      String taskId) {
    return getSyncResultDto(result, totalFetched, duration, taskId);
  }

  private SyncResultDto buildErrorResult(Exception e, String taskId) {
    String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    return SyncResultDto.builder()
        .taskId(taskId)
        .success(false)
        .message("Critical error: " + message)
        .syncedAt(LocalDateTime.now())
        .errors(List.of(message))
        .build();
  }

  public List<DndFeatResponse> getAllFeats() {
    List<DndFeat> feats = featRepository.findAll();
    return feats.stream()
        .map(featMapper::toDto).toList();
  }
}
