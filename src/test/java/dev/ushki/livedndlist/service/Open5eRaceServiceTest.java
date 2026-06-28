package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eRaceDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.dndCharacter.Race;
import dev.ushki.livedndlist.mapper.RaceMapper;
import dev.ushki.livedndlist.repository.RaceRepository;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;

@ExtendWith(MockitoExtension.class)
class Open5eRaceServiceTest {

  @Mock
  private RaceRepository raceRepository;

  @Mock
  private RaceMapper raceMapper;

  @Mock
  private Open5eApiClient apiClient;

  @Mock
  private SyncMetrics syncMetrics;

  private Open5eRaceService raceService;

  private Open5eRaceDto humanDto;
  private Open5eRaceDto elfDto;
  private Race humanEntity;
  private Race elfEntity;

  @BeforeEach
  void setUp() {
    raceService = new Open5eRaceService(
        raceRepository,
        raceMapper,
        apiClient,
        syncMetrics
    );

    humanDto = new Open5eRaceDto();
    humanDto.setName("Human");
    humanDto.setSlug("human");
    humanDto.setSize("Medium");

    elfDto = new Open5eRaceDto();
    elfDto.setName("Elf");
    elfDto.setSlug("elf");
    elfDto.setSize("Medium");

    humanEntity = Race.builder()
        .id(1L)
        .name("Human")
        .slug("human")
        .sizeRaw("Medium")
        .build();

    elfEntity = Race.builder()
        .id(2L)
        .name("Elf")
        .slug("elf")
        .sizeRaw("Medium")
        .build();
  }

  // ---------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------

  private void mockFetchAll(List<Open5eRaceDto> results) {
    when(apiClient.fetchAll(
        eq("/v1/races/"),
        any(ParameterizedTypeReference.class))
    ).thenReturn(results);
  }

  private void mockFetchAllThrows(RuntimeException ex) {
    when(apiClient.fetchAll(
        eq("/v1/races/"),
        any(ParameterizedTypeReference.class))
    ).thenThrow(ex);
  }

  private void mockFetchAllBlocking(List<Open5eRaceDto> results,
      CountDownLatch started,
      CountDownLatch canProceed) {
    when(apiClient.fetchAll(
        eq("/v1/races/"),
        any(ParameterizedTypeReference.class))
    ).thenAnswer(invocation -> {
      started.countDown();
      canProceed.await(5, TimeUnit.SECONDS);
      return results;
    });
  }

  private void stubSyncMetricsHappyPath() {
    doNothing().when(syncMetrics).startOperation();
    doNothing().when(syncMetrics).endOperation();
    doNothing().when(syncMetrics).recordRequest(anyLong(), eq(true));
  }

  private void stubSyncMetricsErrorPath() {
    doNothing().when(syncMetrics).startOperation();
    doNothing().when(syncMetrics).endOperation();
    doNothing().when(syncMetrics).recordRequest(anyLong(), eq(false));
  }

  // ---------------------------------------------------------------
  // getSyncStatus
  // ---------------------------------------------------------------

  @Nested
  @DisplayName("getSyncStatus")
  class GetSyncStatusTests {

    @Test
    @DisplayName("Should return sync status when idle")
    void shouldReturnSyncStatusWhenIdle() {
      SyncStatusDto status = raceService.getSyncStatus();

      assertThat(status).isNotNull();
      assertThat(status.isInProgress()).isFalse();
    }
  }

  // ---------------------------------------------------------------
  // syncAllRaces
  // ---------------------------------------------------------------

  @Nested
  @DisplayName("syncAllRaces")
  class SyncAllRacesTests {

    @Test
    @DisplayName("Should sync all races successfully when creating new races")
    void shouldSyncAllRacesSuccessfullyWhenCreating() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findBySlug("human")).thenReturn(Optional.empty());
      when(raceRepository.findBySlug("elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(humanDto)).thenReturn(humanEntity);
      when(raceMapper.toEntity(elfDto)).thenReturn(elfEntity);
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsHappyPath();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).isEqualTo("Sync completed successfully");
      assertThat(result.getStatistics()).isNotNull();
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(2);
      assertThat(result.getStatistics().getCreated()).isEqualTo(2);
      assertThat(result.getStatistics().getUpdated()).isZero();
      assertThat(result.getStatistics().getFailed()).isZero();

      verify(raceRepository, times(2)).save(any(Race.class));
      verify(syncMetrics).startOperation();
      verify(syncMetrics).endOperation();
    }

    @Test
    @DisplayName("Should sync all races successfully when updating existing races")
    void shouldSyncAllRacesSuccessfullyWhenUpdating() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findBySlug("human")).thenReturn(Optional.of(humanEntity));
      when(raceRepository.findBySlug("elf")).thenReturn(Optional.of(elfEntity));
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsHappyPath();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getCreated()).isZero();
      assertThat(result.getStatistics().getUpdated()).isEqualTo(2);

      verify(raceMapper).updateEntity(humanEntity, humanDto);
      verify(raceMapper).updateEntity(elfEntity, elfDto);
      verify(raceRepository, times(2)).save(any(Race.class));
    }

    @Test
    @DisplayName("Should handle mixed create and update operations")
    void shouldHandleMixedCreateAndUpdate() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findBySlug("human")).thenReturn(Optional.of(humanEntity));
      when(raceRepository.findBySlug("elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(elfDto)).thenReturn(elfEntity);
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsHappyPath();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getCreated()).isEqualTo(1);
      assertThat(result.getStatistics().getUpdated()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle API error during sync")
    void shouldHandleApiErrorDuringSync() {
      mockFetchAllThrows(new RuntimeException("API connection failed"));
      stubSyncMetricsErrorPath();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Critical error");
      assertThat(result.getErrors()).isNotNull();
      assertThat(result.getErrors()).contains("API connection failed");

      verify(raceRepository, never()).save(any(Race.class));
    }

    @Test
    @DisplayName("Should handle empty API response")
    void shouldHandleEmptyApiResponse() {
      mockFetchAll(List.of());
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getTotalFetched()).isZero();
      assertThat(result.getStatistics().getCreated()).isZero();

      verify(raceRepository, never()).save(any(Race.class));
    }

    @Test
    @DisplayName("Should record sync completed with errors when some races fail")
    void shouldRecordSyncCompletedWithErrors() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findBySlug("human")).thenReturn(Optional.empty());
      when(raceRepository.findBySlug("elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(humanDto)).thenReturn(humanEntity);
      when(raceMapper.toEntity(elfDto)).thenThrow(new RuntimeException("Mapping error"));
      when(raceRepository.save(humanEntity)).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).isEqualTo("Sync completed with errors");
      assertThat(result.getStatistics().getCreated()).isEqualTo(1);
      assertThat(result.getStatistics().getFailed()).isEqualTo(1);
      assertThat(result.getErrors()).isNotNull();
      assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle null results from fetchAll gracefully")
    void shouldHandleNullResultsFromFetchAll() {
      // fetchAll itself never returns null (returns empty list on null page results),
      // so we verify the empty-list path covers the same behavior.
      mockFetchAll(List.of());
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getTotalFetched()).isZero();
    }

    @Test
    @DisplayName("Should handle large number of races returned by fetchAll")
    void shouldHandleLargeNumberOfRaces() {
      List<Open5eRaceDto> largeList =
          List.of(humanDto, elfDto, humanDto, elfDto, humanDto);
      mockFetchAll(largeList);
      when(raceRepository.findBySlug("human")).thenReturn(Optional.empty());
      when(raceRepository.findBySlug("elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(humanDto)).thenReturn(humanEntity);
      when(raceMapper.toEntity(elfDto)).thenReturn(elfEntity);
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsHappyPath();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(5);
    }

    @Test
    @DisplayName("Pagination is handled entirely inside the client — fetchAll called once")
    void shouldCallFetchAllExactlyOnce() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsHappyPath();

      raceService.syncAllRaces();

      verify(apiClient, times(1))
          .fetchAll(eq("/v1/races/"), any(ParameterizedTypeReference.class));
    }
  }

  // ---------------------------------------------------------------
  // syncBySlug
  // ---------------------------------------------------------------

  @Nested
  @DisplayName("syncBySlug")
  class SyncBySlugTests {

    @Test
    @DisplayName("Should sync race by slug successfully when creating")
    void shouldSyncRaceBySlugSuccessfullyWhenCreating() {
      when(apiClient.getBySlug("/v1/races/", "human", Open5eRaceDto.class))
          .thenReturn(humanDto);
      when(raceRepository.findBySlug("human")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(humanDto)).thenReturn(humanEntity);
      when(raceRepository.save(humanEntity)).thenReturn(humanEntity);

      SyncResultDto result = raceService.syncBySlug("human");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).contains("Race created");
      assertThat(result.getMessage()).contains("Human");
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(1);
      assertThat(result.getStatistics().getCreated()).isEqualTo(1);
      assertThat(result.getStatistics().getUpdated()).isZero();

      verify(raceMapper).toEntity(humanDto);
      verify(raceRepository).save(humanEntity);
    }

    @Test
    @DisplayName("Should sync race by slug successfully when updating")
    void shouldSyncRaceBySlugSuccessfullyWhenUpdating() {
      when(apiClient.getBySlug("/v1/races/", "human", Open5eRaceDto.class))
          .thenReturn(humanDto);
      when(raceRepository.findBySlug("human")).thenReturn(Optional.of(humanEntity));
      when(raceRepository.save(humanEntity)).thenReturn(humanEntity);

      SyncResultDto result = raceService.syncBySlug("human");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).contains("Race updated");
      assertThat(result.getMessage()).contains("Human");
      assertThat(result.getStatistics().getCreated()).isZero();
      assertThat(result.getStatistics().getUpdated()).isEqualTo(1);

      verify(raceMapper).updateEntity(humanEntity, humanDto);
      verify(raceRepository).save(humanEntity);
    }

    @Test
    @DisplayName("Should handle API error when syncing by slug")
    void shouldHandleApiErrorWhenSyncingBySlug() {
      when(apiClient.getBySlug("/v1/races/", "nonexistent", Open5eRaceDto.class))
          .thenThrow(new RuntimeException("Race not found"));

      SyncResultDto result = raceService.syncBySlug("nonexistent");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Critical error");
      assertThat(result.getErrors()).contains("Race not found");

      verify(raceRepository, never()).save(any(Race.class));
    }

    @Test
    @DisplayName("Should handle database error when syncing by slug")
    void shouldHandleDatabaseErrorWhenSyncingBySlug() {
      when(apiClient.getBySlug("/v1/races/", "human", Open5eRaceDto.class))
          .thenReturn(humanDto);
      when(raceRepository.findBySlug("human")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(humanDto)).thenReturn(humanEntity);
      when(raceRepository.save(humanEntity))
          .thenThrow(new RuntimeException("Database error"));

      SyncResultDto result = raceService.syncBySlug("human");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Critical error");
    }

    @Test
    @DisplayName("Should record duration when syncing by slug")
    void shouldRecordDurationWhenSyncingBySlug() {
      when(apiClient.getBySlug("/v1/races/", "human", Open5eRaceDto.class))
          .thenReturn(humanDto);
      when(raceRepository.findBySlug("human")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(humanDto)).thenReturn(humanEntity);
      when(raceRepository.save(humanEntity)).thenReturn(humanEntity);

      SyncResultDto result = raceService.syncBySlug("human");

      assertThat(result.getStatistics().getDurationMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should sync race with hyphenated slug")
    void shouldSyncRaceWithHyphenatedSlug() {
      Open5eRaceDto halfElfDto = new Open5eRaceDto();
      halfElfDto.setName("Half-Elf");
      halfElfDto.setSlug("half-elf");
      halfElfDto.setSize("Medium");

      Race halfElfEntity = Race.builder()
          .id(3L)
          .name("Half-Elf")
          .slug("half-elf")
          .sizeRaw("Medium")
          .build();

      when(apiClient.getBySlug("/v1/races/", "half-elf", Open5eRaceDto.class))
          .thenReturn(halfElfDto);
      when(raceRepository.findBySlug("half-elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(halfElfDto)).thenReturn(halfElfEntity);
      when(raceRepository.save(halfElfEntity)).thenReturn(halfElfEntity);

      SyncResultDto result = raceService.syncBySlug("half-elf");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).contains("Half-Elf");
    }
  }

  // ---------------------------------------------------------------
  // clearAll
  // ---------------------------------------------------------------

  @Nested
  @DisplayName("clearAll")
  class ClearAllTests {

    @Test
    @DisplayName("Should clear all races successfully")
    void shouldClearAllRacesSuccessfully() {
      when(raceRepository.count()).thenReturn(10L);
      doNothing().when(raceRepository).deleteAll();

      SyncResultDto result = raceService.clearAll();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).isEqualTo("Deleted races: 10");
      assertThat(result.getSyncedAt()).isNotNull();

      verify(raceRepository).count();
      verify(raceRepository).deleteAll();
    }

    @Test
    @DisplayName("Should handle clear all when no races exist")
    void shouldHandleClearAllWhenNoRacesExist() {
      when(raceRepository.count()).thenReturn(0L);
      doNothing().when(raceRepository).deleteAll();

      SyncResultDto result = raceService.clearAll();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).isEqualTo("Deleted races: 0");

      verify(raceRepository).deleteAll();
    }

    @Test
    @DisplayName("Should handle database error when clearing all")
    void shouldHandleDatabaseErrorWhenClearingAll() {
      when(raceRepository.count()).thenReturn(10L);
      doThrow(new RuntimeException("Database error")).when(raceRepository).deleteAll();

      SyncResultDto result = raceService.clearAll();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Delete error");
      assertThat(result.getMessage()).contains("Database error");
    }

    @Test
    @DisplayName("Should handle count error when clearing all")
    void shouldHandleCountErrorWhenClearingAll() {
      when(raceRepository.count()).thenThrow(new RuntimeException("Count failed"));

      SyncResultDto result = raceService.clearAll();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Delete error");

      verify(raceRepository, never()).deleteAll();
    }
  }

  // ---------------------------------------------------------------
  // Sync Metrics Integration
  // ---------------------------------------------------------------

  @Nested
  @DisplayName("Sync Metrics Integration")
  class SyncMetricsTests {

    @Test
    @DisplayName("Should call start and end operation on sync")
    void shouldCallStartAndEndOperationOnSync() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      raceService.syncAllRaces();

      verify(syncMetrics).startOperation();
      verify(syncMetrics).endOperation();
    }

    @Test
    @DisplayName("Should record request for each race")
    void shouldRecordRequestForEachRace() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      raceService.syncAllRaces();

      verify(syncMetrics, times(2)).recordRequest(anyLong(), eq(true));
    }

    @Test
    @DisplayName("Should record failure metric on API error")
    void shouldRecordFailureMetricOnApiError() {
      mockFetchAllThrows(new RuntimeException("API error"));
      stubSyncMetricsErrorPath();

      raceService.syncAllRaces();

      verify(syncMetrics).recordRequest(anyLong(), eq(false));
    }

    @Test
    @DisplayName("Should call endOperation even on error")
    void shouldCallEndOperationEvenOnError() {
      mockFetchAllThrows(new RuntimeException("API error"));
      stubSyncMetricsErrorPath();

      raceService.syncAllRaces();

      verify(syncMetrics).endOperation();
    }
  }

  // ---------------------------------------------------------------
  // Edge Cases
  // ---------------------------------------------------------------

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle race with different size")
    void shouldHandleRaceWithDifferentSize() {
      Open5eRaceDto halflingDto = new Open5eRaceDto();
      halflingDto.setName("Halfling");
      halflingDto.setSlug("halfling");
      halflingDto.setSize("Small");

      Race halflingEntity = Race.builder()
          .id(4L)
          .name("Halfling")
          .slug("halfling")
          .sizeRaw("Small")
          .build();

      when(apiClient.getBySlug("/v1/races/", "halfling", Open5eRaceDto.class))
          .thenReturn(halflingDto);
      when(raceRepository.findBySlug("halfling")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(halflingDto)).thenReturn(halflingEntity);
      when(raceRepository.save(halflingEntity)).thenReturn(halflingEntity);

      SyncResultDto result = raceService.syncBySlug("halfling");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).contains("Halfling");
    }

    @Test
    @DisplayName("Should handle race with null name in fetchAll results")
    void shouldHandleRaceWithNullName() {
      Open5eRaceDto nullNameDto = new Open5eRaceDto();
      nullNameDto.setName(null);
      nullNameDto.setSlug("null-race");

      mockFetchAll(List.of(nullNameDto));
      when(raceRepository.findBySlug("null-race")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(nullNameDto)).thenReturn(humanEntity);
      when(raceRepository.save(humanEntity)).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(1);
    }
  }

  // ---------------------------------------------------------------
  // Concurrent Sync / Already In Progress
  // ---------------------------------------------------------------

  @Nested
  @DisplayName("Concurrent Sync and Already In Progress Tests")
  class ConcurrentSyncTests {

    @Test
    @DisplayName("Should return already in progress when sync is already running")
    void shouldReturnAlreadyInProgressWhenSyncIsRunning() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<SyncResultDto> firstSync = executor.submit(() -> raceService.syncAllRaces());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS), "First sync should start");

      SyncResultDto secondResult = raceService.syncAllRaces();

      assertThat(secondResult).isNotNull();
      assertThat(secondResult.isSuccess()).isFalse();
      assertThat(secondResult.getMessage()).isEqualTo("Sync already in progress");
      assertThat(secondResult.getTaskId()).isNotNull();
      assertThat(secondResult.getSyncedAt()).isNotNull();

      canProceed.countDown();
      SyncResultDto firstResult = firstSync.get(5, TimeUnit.SECONDS);
      assertThat(firstResult.isSuccess()).isTrue();

      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should have valid UUID as task ID in already in progress result")
    void shouldHaveValidUuidInAlreadyInProgressResult() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> raceService.syncAllRaces());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result.getTaskId()).isNotNull();
      assertThat(result.getTaskId())
          .matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should not have statistics in already in progress result")
    void shouldNotHaveStatisticsInAlreadyInProgressResult() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> raceService.syncAllRaces());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result.getStatistics()).isNull();
      assertThat(result.getErrors()).isNull();

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should allow new sync after previous one completes")
    void shouldAllowNewSyncAfterPreviousOneCompletes() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      SyncResultDto firstResult = raceService.syncAllRaces();
      assertThat(firstResult.isSuccess()).isTrue();

      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.of(humanEntity));

      SyncResultDto secondResult = raceService.syncAllRaces();
      assertThat(secondResult.isSuccess()).isTrue();
      assertThat(secondResult.getMessage()).isNotEqualTo("Sync already in progress");
    }

    @Test
    @DisplayName("Should release progress tracker even when sync fails")
    void shouldReleaseProgressTrackerEvenWhenSyncFails() {
      mockFetchAllThrows(new RuntimeException("API error"));
      stubSyncMetricsErrorPath();

      SyncResultDto firstResult = raceService.syncAllRaces();
      assertThat(firstResult.isSuccess()).isFalse();

      // Reset and set up a successful second sync
      org.mockito.Mockito.reset(apiClient, raceRepository, raceMapper, syncMetrics);

      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      SyncResultDto secondResult = raceService.syncAllRaces();
      assertThat(secondResult.isSuccess()).isTrue();
      assertThat(secondResult.getMessage()).isNotEqualTo("Sync already in progress");
    }

    @Test
    @DisplayName("Should show in progress status during sync")
    void shouldShowInProgressStatusDuringSync() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> raceService.syncAllRaces());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      SyncStatusDto status = raceService.getSyncStatus();
      assertThat(status.isInProgress()).isTrue();

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);

      SyncStatusDto finalStatus = raceService.getSyncStatus();
      assertThat(finalStatus.isInProgress()).isFalse();
    }

    @Test
    @DisplayName("Should not call syncMetrics when sync is already in progress")
    void shouldNotCallSyncMetricsWhenAlreadyInProgress() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> raceService.syncAllRaces());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      org.mockito.Mockito.clearInvocations(syncMetrics);

      raceService.syncAllRaces();

      verify(syncMetrics, never()).startOperation();

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should not interact with database when sync is already in progress")
    void shouldNotInteractWithDatabaseWhenAlreadyInProgress() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> raceService.syncAllRaces());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      org.mockito.Mockito.clearInvocations(raceRepository);
      org.mockito.Mockito.clearInvocations(apiClient);

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result.getMessage()).isEqualTo("Sync already in progress");

      verify(raceRepository, never()).findBySlug(anyString());
      verify(raceRepository, never()).save(any());
      verify(apiClient, never())
          .fetchAll(anyString(), any(ParameterizedTypeReference.class));

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should have syncedAt timestamp in already in progress result")
    void shouldHaveSyncedAtTimestampInAlreadyInProgressResult() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> raceService.syncAllRaces());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      LocalDateTime beforeCall = LocalDateTime.now();
      SyncResultDto result = raceService.syncAllRaces();
      LocalDateTime afterCall = LocalDateTime.now();

      assertThat(result.getSyncedAt()).isNotNull();
      assertThat(result.getSyncedAt()).isAfterOrEqualTo(beforeCall.minusSeconds(1));
      assertThat(result.getSyncedAt()).isBeforeOrEqualTo(afterCall.plusSeconds(1));

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should generate unique task IDs for each rejected sync attempt")
    void shouldGenerateUniqueTaskIdsForEachRejectedSyncAttempt() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> raceService.syncAllRaces());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      SyncResultDto firstRejected = raceService.syncAllRaces();
      SyncResultDto secondRejected = raceService.syncAllRaces();

      assertThat(firstRejected.getTaskId()).isNotEqualTo(secondRejected.getTaskId());

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
