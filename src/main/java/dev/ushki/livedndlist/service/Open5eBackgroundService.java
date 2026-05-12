package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundBenefitDto;
import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5eBackgroundResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.Background;
import dev.ushki.livedndlist.entity.character.BackgroundBenefit;
import dev.ushki.livedndlist.enums.SyncAction;
import dev.ushki.livedndlist.mapper.BackgroundMapper;
import dev.ushki.livedndlist.repository.BackgroundRepository;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import dev.ushki.livedndlist.service.sync.SyncProgressTracker;
import dev.ushki.livedndlist.service.sync.SyncResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
@RequiredArgsConstructor
public class Open5eBackgroundService {

  private static final String API_PATH = "/v2/backgrounds/";

  private final BackgroundRepository backgroundRepository;
  private final BackgroundMapper backgroundMapper;
  private final Open5eApiClient apiClient;
  private final SyncMetrics syncMetrics;

  private final SyncProgressTracker progressTracker = new SyncProgressTracker();

  public SyncStatusDto getSyncStatus() {
    return progressTracker.getStatus();
  }

  public SyncResultDto syncAllBackgrounds() {
    String taskId = UUID.randomUUID().toString();

    if (!progressTracker.tryStart()) {
      return buildAlreadyInProgressResult(taskId);
    }

    long startTime = System.currentTimeMillis();
    SyncResult result = new SyncResult();

    try {
      syncMetrics.startOperation();
      progressTracker.setOperation("Fetching data from API");
      log.info("Starting background sync from Open5e API");

      List<Open5eBackgroundDto> allBackgrounds = fetchAllFromApi();
      progressTracker.setTotal(allBackgrounds.size());

      log.info("Fetched {} backgrounds from API", allBackgrounds.size());
      progressTracker.setOperation("Saving to database");

      for (Open5eBackgroundDto dto : allBackgrounds) {
        long itemStart = System.currentTimeMillis();
        processBackground(dto, result);
        long itemDuration = System.currentTimeMillis() - itemStart;
        syncMetrics.recordRequest(itemDuration, true);
        progressTracker.incrementProcessed();
      }

      long duration = System.currentTimeMillis() - startTime;
      log.info("Sync completed in {}ms. Created: {}, Updated: {}, Failed: {}",
          duration, result.getCreated(), result.getUpdated(), result.getFailed());

      return buildSuccessResult(result, allBackgrounds.size(), duration, taskId);

    } catch (Exception e) {
      syncMetrics.recordRequest(System.currentTimeMillis() - startTime, false);
      log.error("Critical sync error: {}", e.getMessage(), e);
      return buildErrorResult(e, taskId);

    } finally {
      syncMetrics.endOperation();
      progressTracker.finish();
    }
  }

  public SyncResultDto syncBySlug(String slug) {
    long startTime = System.currentTimeMillis();

    try {
      log.info("Syncing class by slug: {}", slug);

      Open5eBackgroundDto dto = apiClient.getBySlug(API_PATH, slug, Open5eBackgroundDto.class);
      SyncAction action = saveOrUpdate(dto);

      long duration = System.currentTimeMillis() - startTime;

      return SyncResultDto.builder()
          .success(true)
          .message(action == SyncAction.CREATED
              ? "Class created: " + dto.getName()
              : "Class updated: " + dto.getName())
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(1)
              .created(action == SyncAction.CREATED ? 1 : 0)
              .updated(action == SyncAction.UPDATED ? 1 : 0)
              .failed(0)
              .durationMs(duration)
              .build())
          .build();

    } catch (Exception e) {
      log.error("API request error: {}", e.getMessage());
      return buildErrorResult(e, "");
    }
  }

  @Transactional
  public SyncResultDto clearAll() {
    try {
      long count = backgroundRepository.count();
      backgroundRepository.deleteAll();

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

  private List<Open5eBackgroundDto> fetchAllFromApi() {
    List<Open5eBackgroundDto> allBackgrounds = new ArrayList<>();
    String currentPath = API_PATH;
    int pageCount = 0;

    while (currentPath != null) {
      try {
        pageCount++;
        progressTracker.setOperation(String.format("Fetching page %d from API", pageCount));

        Open5eBackgroundResponse response = apiClient.getByPath(currentPath,
            Open5eBackgroundResponse.class);

        if (response != null && response.getResults() != null) {
          allBackgrounds.addAll(response.getResults());
          currentPath = apiClient.extractNextPath(response.getNext());
        } else {
          break;
        }
      } catch (HttpClientErrorException.NotFound e) {
        log.info("Reached end of pages at page {} (API returned 404)", pageCount);
        break;
      }
    }

    return allBackgrounds;
  }

  private void processBackground(Open5eBackgroundDto dto, SyncResult result) {
    try {
      SyncAction action = saveOrUpdate(dto);
      if (action == SyncAction.CREATED) {
        result.recordCreated();
      } else {
        result.recordUpdated();
      }
    } catch (Exception e) {
      result.recordError(dto.getName(), e);
      log.error("Error processing background '{}': {}", dto.getName(), e.getMessage(), e);
    }
  }

  private SyncAction saveOrUpdate(Open5eBackgroundDto dto) {
    Optional<Background> existing = backgroundRepository.findByKey(dto.getKey());

    if (existing.isPresent()) {
      Background background = existing.get();
      backgroundMapper.updateEntity(background, dto);
      backgroundRepository.save(background);
      return SyncAction.UPDATED;
    } else {
      Background dndClass = backgroundMapper.toEntity(dto);
      backgroundRepository.save(dndClass);
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

  public List<Open5eBackgroundDto> getAllBackgrounds() {
    List<Background> backgrounds = backgroundRepository.findAll();
    return backgrounds.stream()
        .map(backgroundMapper::toDto).toList();
  }

  public List<Open5eBackgroundBenefitDto> getBenefitsByBackground(String backgroundKey) {
    Optional<Background> background = backgroundRepository.findByKey(backgroundKey);

    if (background.isPresent()) {
      List<BackgroundBenefit> benefits = background.get().getBenefits();
      return benefits.stream().map(backgroundMapper::toBenefitDto).toList();
    } else {
      log.error("No background found with key: {}", backgroundKey);
      return null;
    }
  }
}
