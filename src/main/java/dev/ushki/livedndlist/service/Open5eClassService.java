package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eClassDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5eClassResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.DndClass;
import dev.ushki.livedndlist.enums.SyncAction;
import dev.ushki.livedndlist.mapper.DndClassMapper;
import dev.ushki.livedndlist.repository.DndClassRepository;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import dev.ushki.livedndlist.service.sync.SyncProgressTracker;
import dev.ushki.livedndlist.service.sync.SyncResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class Open5eClassService {

  private static final String API_PATH = "/classes/";

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
    if (!progressTracker.tryStart()) {
      return buildAlreadyInProgressResult();
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

      return buildSuccessResult(result, allClasses.size(), duration);

    } catch (Exception e) {
      syncMetrics.recordRequest(System.currentTimeMillis() - startTime, false);
      log.error("Critical sync error: {}", e.getMessage(), e);
      return buildErrorResult(e);

    } finally {
      syncMetrics.endOperation();
      progressTracker.finish();
    }
  }

  @Transactional
  public SyncResultDto syncBySlug(String slug) {
    long startTime = System.currentTimeMillis();

    try {
      log.info("Syncing class by slug: {}", slug);

      Open5eClassDto dto = apiClient.get(API_PATH + slug + "/", Open5eClassDto.class);
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
      return buildErrorResult(e);
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
    List<Open5eClassDto> allClasses = new ArrayList<>();
    String currentPath = API_PATH;
    int pageCount = 0;

    while (currentPath != null) {
      pageCount++;
      progressTracker.setOperation(String.format("Fetching page %d from API", pageCount));

      Open5eClassResponse response = apiClient.get(currentPath, Open5eClassResponse.class);

      if (response.getResults() != null) {
        allClasses.addAll(response.getResults());
        currentPath = apiClient.extractNextPath(response.getNext());
      } else {
        break;
      }
    }

    return allClasses;
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
    Optional<DndClass> existing = dndClassRepository.findBySlug(dto.getSlug());

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

  private SyncResultDto buildAlreadyInProgressResult() {
    return SyncResultDto.builder()
        .success(false)
        .message("Sync already in progress")
        .syncedAt(LocalDateTime.now())
        .build();
  }

  private SyncResultDto buildSuccessResult(SyncResult result, int totalFetched, long duration) {
    return SyncResultDto.builder()
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

  private SyncResultDto buildErrorResult(Exception e) {
    return SyncResultDto.builder()
        .success(false)
        .message("Critical error: " + e.getMessage())
        .syncedAt(LocalDateTime.now())
        .errors(List.of(e.getMessage()))
        .build();
  }
}
