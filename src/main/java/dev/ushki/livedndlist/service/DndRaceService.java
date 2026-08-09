package dev.ushki.livedndlist.service;

import static dev.ushki.livedndlist.service.DndClassService.getSyncResultDto;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eRaceDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5ePaginatedResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.response.DndRaceResponse;
import dev.ushki.livedndlist.entity.dndCharacter.race.Race;
import dev.ushki.livedndlist.entity.dndCharacter.race.RaceTrait;
import dev.ushki.livedndlist.enums.FeatureSourceType;
import dev.ushki.livedndlist.enums.SyncAction;
import dev.ushki.livedndlist.mapper.RaceMapper;
import dev.ushki.livedndlist.repository.RaceRepository;
import dev.ushki.livedndlist.service.features.FeatureCatalogService;
import dev.ushki.livedndlist.service.features.FeatureUpsertHelper;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import dev.ushki.livedndlist.service.sync.SyncProgressTracker;
import dev.ushki.livedndlist.service.sync.SyncResult;
import java.time.LocalDateTime;
import java.util.Comparator;
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
public class DndRaceService {

  private static final String API_PATH = "/v2/species/";

  private final RaceRepository raceRepository;
  private final RaceMapper raceMapper;
  private final Open5eApiClient apiClient;
  private final SyncMetrics syncMetrics;
  private final FeatureUpsertHelper featureUpsertHelper;
  private final FeatureCatalogService featureCatalogService;

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

      allRaces.sort(Comparator.comparing(Open5eRaceDto::isSubspecies));

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

      featureCatalogService.invalidateCache();
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
    return apiClient.fetchAll(
        API_PATH,
        new ParameterizedTypeReference<Open5ePaginatedResponse<Open5eRaceDto>>() {
        }
    );
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
    Optional<Race> existing = raceRepository.findByKey(dto.getKey());
    Race race;
    SyncAction action;
    if (existing.isPresent()) {
      race = existing.get();
      raceMapper.updateEntity(race, dto);
      raceRepository.save(race);
      action = SyncAction.UPDATED;
    } else {
      race = raceMapper.toEntity(dto);
      raceRepository.save(race);
      action = SyncAction.CREATED;
    }

    syncRaceFeatures(race);
    return action;
  }

  private void syncRaceFeatures(Race race) {
    FeatureSourceType sourceType = race.isSubspecies()
        ? FeatureSourceType.SUBRACE
        : FeatureSourceType.RACE;
    String prefix = race.isSubspecies() ? "subrace_" : "race_";

    featureUpsertHelper.deleteBySource(sourceType, race.getKey());

    if (race.getTraits() == null) {
      return;
    }

    int order = 0;
    for (RaceTrait trait : race.getTraits()) {
      String featureKey = prefix + race.getKey() + "_" + slug(trait.getName(), order);
      featureUpsertHelper.upsertNarrativeFeature(
          featureKey,
          trait.getName(),
          trait.getDescription(),
          sourceType,
          race.getKey(),
          null,
          null,
          race.getDocument()
      );
      order++;
    }
  }

  private String slug(String name, int fallback) {
    if (name == null || name.isBlank()) {
      return "trait_" + fallback;
    }
    return name.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
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

  public List<DndRaceResponse> getAllRaces() {
    List<Race> races = raceRepository.findAll();
    return races.stream()
        .map(raceMapper::toDto).toList();
  }
}
