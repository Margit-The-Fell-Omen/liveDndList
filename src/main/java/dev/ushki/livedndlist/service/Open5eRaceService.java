package dev.ushki.livedndlist.service;

import static dev.ushki.livedndlist.service.Open5eClassService.getSyncResultDto;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eRaceDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5eRaceResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.Race;
import dev.ushki.livedndlist.enums.SyncAction;
import dev.ushki.livedndlist.mapper.RaceMapper;
import dev.ushki.livedndlist.repository.RaceRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class Open5eRaceService {

  private static final String API_PATH = "/races/";

  private final RaceRepository raceRepository;
  private final RaceMapper raceMapper;
  private final Open5eApiClient apiClient;
  private final SyncMetrics syncMetrics;

  private final SyncProgressTracker progressTracker = new SyncProgressTracker();

  public SyncStatusDto getSyncStatus() {
    return progressTracker.getStatus();
  }

  @Transactional
  public SyncResultDto syncAllRaces() {
    String taskId = UUID.randomUUID().toString();

    if (!progressTracker.tryStart()) {
      return buildAlreadyInProgressResult(taskId);
    }

    long startTime = System.currentTimeMillis();
    SyncResult result = new SyncResult();

    try {
      syncMetrics.startOperation();
      progressTracker.setOperation("Fetching data from API");
      log.info("Starting race sync from Open5e API");

      List<Open5eRaceDto> allRaces = fetchAllFromApi();
      progressTracker.setTotal(allRaces.size());

      log.info("Fetched {} races from API", allRaces.size());
      progressTracker.setOperation("Saving to database");

      for (Open5eRaceDto dto : allRaces) {
        long itemStart = System.currentTimeMillis();
        processRace(dto, result);
        long itemDuration = System.currentTimeMillis() - itemStart;
        syncMetrics.recordRequest(itemDuration, true);
        progressTracker.incrementProcessed();
      }

      long duration = System.currentTimeMillis() - startTime;
      log.info("Sync completed in {}ms. Created: {}, Updated: {}, Failed: {}",
          duration, result.getCreated(), result.getUpdated(), result.getFailed());

      return buildSuccessResult(result, allRaces.size(), duration, taskId);

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
  public SyncResultDto syncBySlug(String slug) {
    long startTime = System.currentTimeMillis();

    try {
      log.info("Syncing race by slug: {}", slug);

      Open5eRaceDto dto = apiClient.getBySlug(API_PATH, slug, Open5eRaceDto.class);
      SyncAction action = saveOrUpdate(dto);

      long duration = System.currentTimeMillis() - startTime;

      return SyncResultDto.builder()
          .success(true)
          .message(action == SyncAction.CREATED
              ? "Race created: " + dto.getName()
              : "Race updated: " + dto.getName())
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
      long count = raceRepository.count();
      raceRepository.deleteAll();

      return SyncResultDto.builder()
          .success(true)
          .message("Deleted races: " + count)
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

  private List<Open5eRaceDto> fetchAllFromApi() {
    List<Open5eRaceDto> allRaces = new ArrayList<>();
    String currentPath = API_PATH;
    int pageCount = 0;

    while (currentPath != null) {
      pageCount++;
      progressTracker.setOperation(String.format("Fetching page %d from API", pageCount));

      Open5eRaceResponse response = apiClient.getByPath(currentPath, Open5eRaceResponse.class);

      if (response.getResults() != null) {
        allRaces.addAll(response.getResults());
        currentPath = apiClient.extractNextPath(response.getNext());
      } else {
        break;
      }
    }

    return allRaces;
  }

  private void processRace(Open5eRaceDto dto, SyncResult result) {
    try {
      SyncAction action = saveOrUpdate(dto);
      if (action == SyncAction.CREATED) {
        result.recordCreated();
      } else {
        result.recordUpdated();
      }
    } catch (Exception e) {
      result.recordError(dto.getName(), e);
      log.error("Error processing race '{}': {}", dto.getName(), e.getMessage(), e);
    }
  }

  private SyncAction saveOrUpdate(Open5eRaceDto dto) {
    Optional<Race> existing = raceRepository.findBySlug(dto.getSlug());

    if (existing.isPresent()) {
      Race race = existing.get();
      raceMapper.updateEntity(race, dto);
      raceRepository.save(race);
      return SyncAction.UPDATED;
    } else {
      Race race = raceMapper.toEntity(dto);
      raceRepository.save(race);
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
    return SyncResultDto.builder()
        .taskId(taskId)
        .success(false)
        .message("Critical error: " + e.getMessage())
        .syncedAt(LocalDateTime.now())
        .errors(List.of(e.getMessage()))
        .build();
  }
}
