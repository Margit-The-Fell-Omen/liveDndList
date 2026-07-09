package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eClassDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5ePaginatedResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndClass;
import dev.ushki.livedndlist.enums.SyncAction;
import dev.ushki.livedndlist.mapper.DndClassMapper;
import dev.ushki.livedndlist.repository.DndClassRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class Open5eClassService {

  private static final String API_PATH = "/v2/classes/";

  private final DndClassRepository dndClassRepository;
  private final DndClassMapper dndClassMapper;
  private final Open5eApiClient apiClient;
  private final SyncMetrics syncMetrics;

  private final SyncProgressTracker progressTracker = new SyncProgressTracker();

  public SyncStatusDto getSyncStatus() {
    return progressTracker.getStatus();
  }

  @Transactional
  public SyncResultDto syncAllClasses() {
    String taskId = UUID.randomUUID().toString();

    if (!progressTracker.tryStart()) {
      return buildAlreadyInProgressResult(taskId);
    }

    long startTime = System.currentTimeMillis();
    SyncResult result = new SyncResult();

    try {
      syncMetrics.startOperation();
      progressTracker.setOperation("Fetching data from API");
      log.info("Starting class sync from Open5e API");

      List<Open5eClassDto> allClasses = fetchAllFromApi();
      progressTracker.setTotal(allClasses.size());

      log.info("Fetched {} classes from API", allClasses.size());
      progressTracker.setOperation("Saving to database");

      for (Open5eClassDto dto : allClasses) {
        long itemStart = System.currentTimeMillis();
        processClass(dto, result);
        long itemDuration = System.currentTimeMillis() - itemStart;
        syncMetrics.recordRequest(itemDuration, true);
        progressTracker.incrementProcessed();
      }

      long duration = System.currentTimeMillis() - startTime;
      log.info("Sync completed in {}ms. Created: {}, Updated: {}, Failed: {}",
          duration, result.getCreated(), result.getUpdated(), result.getFailed());

      return buildSuccessResult(result, allClasses.size(), duration, taskId);

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
      long count = dndClassRepository.count();
      dndClassRepository.deleteAll();

      return SyncResultDto.builder()
          .success(true)
          .message("Deleted classes: " + count)
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

  private List<Open5eClassDto> fetchAllFromApi() {
    return apiClient.fetchAll(
        API_PATH,
        new ParameterizedTypeReference<Open5ePaginatedResponse<Open5eClassDto>>() {
        }
    );
  }

  private void processClass(Open5eClassDto dto, SyncResult result) {
    try {
      SyncAction action = saveOrUpdate(dto);
      if (action == SyncAction.CREATED) {
        result.recordCreated();
      } else {
        result.recordUpdated();
      }
    } catch (Exception e) {
      result.recordError(dto.getName(), e);
      log.error("Error processing class '{}': {}", dto.getName(), e.getMessage(), e);
    }
  }

  private SyncAction saveOrUpdate(Open5eClassDto dto) {
    Optional<DndClass> existing = dndClassRepository.findByKey(dto.getKey());

    if (existing.isPresent()) {
      DndClass dndClass = existing.get();
      dndClassMapper.updateEntity(dndClass, dto);
      dndClassRepository.save(dndClass);
      return SyncAction.UPDATED;
    } else {
      DndClass dndClass = dndClassMapper.toEntity(dto);
      dndClassRepository.save(dndClass);
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

  static SyncResultDto getSyncResultDto(SyncResult result, int totalFetched, long duration,
      String taskId) {
    return SyncResultDto.builder()
        .taskId(taskId)
        .success(!result.hasErrors())
        .message(result.hasErrors() ? "Sync completed with errors" : "Sync completed successfully")
        .syncedAt(LocalDateTime.now())
        .statistics(SyncResultDto.SyncStatistics.builder()
            .totalFetched(totalFetched)
            .created(result.getCreated())
            .updated(result.getUpdated())
            .failed(result.getFailed())
            .durationMs(duration)
            .build())
        .errors(result.hasErrors() ? result.getErrors() : null)
        .build();
  }

  private SyncResultDto buildErrorResult(Exception e, String taskId) {
    return SyncResultDto.builder()
        .taskId(taskId)
        .success(false)
        .message("Critical error: " + e.getMessage())
        .syncedAt(LocalDateTime.now())
        .errors(List.of(e.getMessage()))
        .build();
  }

  public List<Open5eClassDto> getAllClasses() {
    List<DndClass> classes = dndClassRepository.findAll();
    return classes.stream()
        .map(dndClassMapper::toDto).toList();
  }

}
