package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.config.Open5eRateLimitConfig;
import dev.ushki.livedndlist.dto.open5e.Open5eRaceDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5eRaceResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.Race;
import dev.ushki.livedndlist.mapper.RaceMapper;
import dev.ushki.livedndlist.repository.RaceRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class Open5eRaceService {

  private final RaceRepository raceRepository;
  private final RaceMapper raceMapper;
  private final RestClient open5eRestClient;
  private final Open5eRateLimitConfig rateLimitConfig;

  private final AtomicBoolean syncInProgress = new AtomicBoolean(false);
  private final AtomicInteger processedCount = new AtomicInteger(0);
  private final AtomicInteger totalCount = new AtomicInteger(0);
  private volatile String currentOperation = "";
  private volatile long lastRequestTime = 0;

  public SyncStatusDto getSyncStatus() {
    return SyncStatusDto.builder()
        .inProgress(syncInProgress.get())
        .currentOperation(currentOperation)
        .processedCount(processedCount.get())
        .totalCount(totalCount.get())
        .progressPercent(calculateProgress())
        .build();
  }

  private double calculateProgress() {
    int total = totalCount.get();
    if (total == 0) {
      return 0;
    }
    return Math.round((double) processedCount.get() / total * 10000.0) / 100.0;
  }

  private void rateLimitDelay() {
    long now = System.currentTimeMillis();
    long timeSinceLastRequest = now - lastRequestTime;

    if (timeSinceLastRequest < rateLimitConfig.getDelayMs()) {
      long sleepTime = rateLimitConfig.getDelayMs() - timeSinceLastRequest;
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Rate limit delay interrupted");
      }
    }

    lastRequestTime = System.currentTimeMillis();
  }

  private <T> T executeWithRetry(String uri, Class<T> responseType) {
    int attempt = 0;
    Exception lastException = null;

    while (attempt < rateLimitConfig.getMaxRetries()) {
      try {
        rateLimitDelay();

        T response = open5eRestClient.get()
            .uri(uri)
            .retrieve()
            .body(responseType);

        if (response != null) {
          return response;
        }

      } catch (Exception e) {
        lastException = e;
        attempt++;

        if (attempt < rateLimitConfig.getMaxRetries()) {
          log.warn("API request failed (attempt {}/{}): {} - {}",
              attempt, rateLimitConfig.getMaxRetries(), uri, e.getMessage());

          try {
            Thread.sleep(rateLimitConfig.getRetryDelayMs() * attempt);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sync interrupted", ie);
          }
        }
      }
    }

    throw new RuntimeException(
        "Request failed after " + rateLimitConfig.getMaxRetries() + " attempts: " + uri,
        lastException);
  }

  @Transactional
  public SyncResultDto syncAllRaces() {
    if (!syncInProgress.compareAndSet(false, true)) {
      return SyncResultDto.builder()
          .success(false)
          .message("Sync already in progress")
          .syncedAt(LocalDateTime.now())
          .build();
    }

    long startTime = System.currentTimeMillis();
    List<String> errors = new ArrayList<>();
    int created = 0;
    int updated = 0;
    int failed = 0;

    try {
      processedCount.set(0);
      currentOperation = "Fetching data from API";

      log.info("Starting race sync from Open5e API");

      List<Open5eRaceDto> allRaces = fetchAllRacesFromApi();
      totalCount.set(allRaces.size());

      log.info("Fetched {} races from API", allRaces.size());
      currentOperation = "Saving to database";

      for (Open5eRaceDto raceDto : allRaces) {
        try {
          SyncAction action = saveOrUpdateRace(raceDto);
          if (action == SyncAction.CREATED) {
            created++;
          } else if (action == SyncAction.UPDATED) {
            updated++;
          }
        } catch (Exception e) {
          failed++;
          String error = String.format("Error processing race '%s': %s",
              raceDto.getName(), e.getMessage());
          errors.add(error);
          log.error(error, e);
        }

        processedCount.incrementAndGet();
      }

      long duration = System.currentTimeMillis() - startTime;

      log.info("Sync completed in {}ms. Created: {}, Updated: {}, Failed: {}",
          duration, created, updated, failed);

      return SyncResultDto.builder()
          .success(errors.isEmpty())
          .message(errors.isEmpty() ? "Sync completed successfully" : "Sync completed with errors")
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(allRaces.size())
              .created(created)
              .updated(updated)
              .failed(failed)
              .durationMs(duration)
              .build())
          .errors(errors.isEmpty() ? null : errors)
          .build();

    } catch (Exception e) {
      log.error("Critical sync error: {}", e.getMessage(), e);

      return SyncResultDto.builder()
          .success(false)
          .message("Critical error: " + e.getMessage())
          .syncedAt(LocalDateTime.now())
          .errors(List.of(e.getMessage()))
          .build();

    } finally {
      syncInProgress.set(false);
      currentOperation = "";
      processedCount.set(0);
      totalCount.set(0);
    }
  }

  @Transactional
  public SyncResultDto syncRaceBySlug(String slug) {
    long startTime = System.currentTimeMillis();

    try {
      log.info("Syncing race by slug: {}", slug);

      Open5eRaceDto raceDto = executeWithRetry("/races/" + slug + "/", Open5eRaceDto.class);

      SyncAction action = saveOrUpdateRace(raceDto);
      long duration = System.currentTimeMillis() - startTime;

      return SyncResultDto.builder()
          .success(true)
          .message(action == SyncAction.CREATED ?
              "Race created: " + raceDto.getName() :
              "Race updated: " + raceDto.getName())
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

      return SyncResultDto.builder()
          .success(false)
          .message("API error: " + e.getMessage())
          .syncedAt(LocalDateTime.now())
          .errors(List.of(e.getMessage()))
          .build();
    }
  }

  private List<Open5eRaceDto> fetchAllRacesFromApi() {
    List<Open5eRaceDto> allRaces = new ArrayList<>();
    String currentPath = "/races/";
    int pageCount = 0;

    while (currentPath != null) {
      pageCount++;
      currentOperation = String.format("Fetching page %d from API", pageCount);

      Open5eRaceResponse response = executeWithRetry(currentPath, Open5eRaceResponse.class);

      if (response.getResults() != null) {
        allRaces.addAll(response.getResults());

        String nextFullUrl = response.getNext();
        currentPath = nextFullUrl != null
            ? nextFullUrl.replace("https://api.open5e.com/v1", "")
            : null;
      } else {
        break;
      }
    }

    return allRaces;
  }

  private SyncAction saveOrUpdateRace(Open5eRaceDto raceDto) {
    Optional<Race> existingRace = raceRepository.findBySlug(raceDto.getSlug());

    if (existingRace.isPresent()) {
      Race race = existingRace.get();
      raceMapper.updateEntity(race, raceDto);
      raceRepository.save(race);
      return SyncAction.UPDATED;
    } else {
      Race race = raceMapper.toEntity(raceDto);
      raceRepository.save(race);
      return SyncAction.CREATED;
    }
  }

  @Transactional
  public SyncResultDto clearAllRaces() {
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

  private enum SyncAction {
    CREATED, UPDATED
  }
}
