package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eRaceDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5ePaginatedResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.dndCharacter.race.Race;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Open5eRaceServiceTest {

  private static final String API_PATH = "/v2/species/";
  private static final long AWAIT_TIMEOUT_SECONDS = 5L;

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
    humanDto.setKey("human");

    elfDto = new Open5eRaceDto();
    elfDto.setName("Elf");
    elfDto.setKey("elf");

    humanEntity = Race.builder()
        .id(1L)
        .name("Human")
        .key("human")
        .subspecies(false)
        .build();

    elfEntity = Race.builder()
        .id(2L)
        .name("Elf")
        .key("elf")
        .subspecies(false)
        .build();
  }

  // ---------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------

  private static ParameterizedTypeReference<Open5ePaginatedResponse<Open5eRaceDto>> paginatedRef() {
    return new ParameterizedTypeReference<>() {
    };
  }

  private void mockFetchAll(List<Open5eRaceDto> results) {
    when(apiClient.fetchAll(eq(API_PATH), any(ParameterizedTypeReference.class)))
        .thenReturn(results);
  }

  private void mockFetchAllThrows(RuntimeException ex) {
    when(apiClient.fetchAll(eq(API_PATH), any(ParameterizedTypeReference.class)))
        .thenThrow(ex);
  }

  private void mockFetchAllBlocking(List<Open5eRaceDto> results,
      CountDownLatch started,
      CountDownLatch canProceed) {
    when(apiClient.fetchAll(eq(API_PATH), any(ParameterizedTypeReference.class)))
        .thenAnswer(invocation -> {
          started.countDown();
          boolean proceeded = canProceed.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
          if (!proceeded) {
            throw new IllegalStateException("canProceed latch was never released");
          }
          return results;
        });
  }

  private static ParameterizedTypeReference<Open5ePaginatedResponse<Open5eRaceDto>>
  anyRacePageReference() {
    return org.mockito.ArgumentMatchers.any();
  }

  private void stubSyncMetricsAll() {
    lenient().doNothing().when(syncMetrics).startOperation();
    lenient().doNothing().when(syncMetrics).endOperation();
    lenient().doNothing().when(syncMetrics).recordRequest(anyLong(), anyBoolean());
  }

  private static ExecutorService newExecutor() {
    return Executors.newSingleThreadExecutor();
  }

  private static void shutdownExecutor(ExecutorService executor) throws InterruptedException {
    executor.shutdown();
    boolean terminated = executor.awaitTermination(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    if (!terminated) {
      executor.shutdownNow();
    }
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
      when(raceRepository.findByKey("human")).thenReturn(Optional.empty());
      when(raceRepository.findByKey("elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(humanDto)).thenReturn(humanEntity);
      when(raceMapper.toEntity(elfDto)).thenReturn(elfEntity);
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsAll();

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
      when(raceRepository.findByKey("human")).thenReturn(Optional.of(humanEntity));
      when(raceRepository.findByKey("elf")).thenReturn(Optional.of(elfEntity));
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsAll();

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
      when(raceRepository.findByKey("human")).thenReturn(Optional.of(humanEntity));
      when(raceRepository.findByKey("elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(elfDto)).thenReturn(elfEntity);
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsAll();

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
      stubSyncMetricsAll();

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
      stubSyncMetricsAll();

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
      when(raceRepository.findByKey("human")).thenReturn(Optional.empty());
      when(raceRepository.findByKey("elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(humanDto)).thenReturn(humanEntity);
      when(raceMapper.toEntity(elfDto)).thenThrow(new RuntimeException("Mapping error"));
      when(raceRepository.save(humanEntity)).thenReturn(humanEntity);
      stubSyncMetricsAll();

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
      mockFetchAll(List.of());
      stubSyncMetricsAll();

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
      when(raceRepository.findByKey("human")).thenReturn(Optional.empty());
      when(raceRepository.findByKey("elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(humanDto)).thenReturn(humanEntity);
      when(raceMapper.toEntity(elfDto)).thenReturn(elfEntity);
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsAll();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(5);
    }

    @Test
    @DisplayName("Pagination is handled entirely inside the client — fetchAll called once")
    void shouldCallFetchAllExactlyOnce() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsAll();

      raceService.syncAllRaces();

      verify(apiClient, times(1))
          .fetchAll(eq(API_PATH), anyRacePageReference());
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
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      raceService.syncAllRaces();

      verify(syncMetrics).startOperation();
      verify(syncMetrics).endOperation();
    }

    @Test
    @DisplayName("Should record request for each race")
    void shouldRecordRequestForEachRace() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      raceService.syncAllRaces();

      verify(syncMetrics, times(2)).recordRequest(anyLong(), eq(true));
    }

    @Test
    @DisplayName("Should record failure metric on API error")
    void shouldRecordFailureMetricOnApiError() {
      mockFetchAllThrows(new RuntimeException("API error"));
      stubSyncMetricsAll();

      raceService.syncAllRaces();

      verify(syncMetrics).recordRequest(anyLong(), eq(false));
    }

    @Test
    @DisplayName("Should call endOperation even on error")
    void shouldCallEndOperationEvenOnError() {
      mockFetchAllThrows(new RuntimeException("API error"));
      stubSyncMetricsAll();

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
    @DisplayName("Should handle race marked as subspecies")
    void shouldHandleRaceMarkedAsSubspecies() {
      Open5eRaceDto woodElfDto = new Open5eRaceDto();
      woodElfDto.setName("Wood Elf");
      woodElfDto.setKey("wood-elf");

      Race woodElfEntity = Race.builder()
          .id(4L)
          .name("Wood Elf")
          .key("wood-elf")
          .subspecies(true)
          .parentRaceKey("elf")
          .build();

      mockFetchAll(List.of(woodElfDto));
      when(raceRepository.findByKey("wood-elf")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(woodElfDto)).thenReturn(woodElfEntity);
      when(raceRepository.save(woodElfEntity)).thenReturn(woodElfEntity);
      stubSyncMetricsAll();

      SyncResultDto result = raceService.syncAllRaces();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getCreated()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle race with null name in fetchAll results")
    void shouldHandleRaceWithNullName() {
      Open5eRaceDto nullNameDto = new Open5eRaceDto();
      nullNameDto.setName(null);
      nullNameDto.setKey("null-race");

      mockFetchAll(List.of(nullNameDto));
      when(raceRepository.findByKey("null-race")).thenReturn(Optional.empty());
      when(raceMapper.toEntity(nullNameDto)).thenReturn(humanEntity);
      when(raceRepository.save(humanEntity)).thenReturn(humanEntity);
      stubSyncMetricsAll();

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
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      ExecutorService executor = newExecutor();
      try {
        Future<SyncResultDto> firstSync = executor.submit(() -> raceService.syncAllRaces());
        assertTrue(syncStarted.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS),
            "First sync should start");

        SyncResultDto secondResult = raceService.syncAllRaces();

        assertThat(secondResult).isNotNull();
        assertThat(secondResult.isSuccess()).isFalse();
        assertThat(secondResult.getMessage()).isEqualTo("Sync already in progress");
        assertThat(secondResult.getTaskId()).isNotNull();
        assertThat(secondResult.getSyncedAt()).isNotNull();

        canProceed.countDown();
        SyncResultDto firstResult = firstSync.get(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(firstResult.isSuccess()).isTrue();
      } finally {
        shutdownExecutor(executor);
      }
    }

    @Test
    @DisplayName("Should have valid UUID as task ID in already in progress result")
    void shouldHaveValidUuidInAlreadyInProgressResult() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      ExecutorService executor = newExecutor();
      try {
        executor.submit(() -> raceService.syncAllRaces());
        assertTrue(syncStarted.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));

        SyncResultDto result = raceService.syncAllRaces();

        assertThat(result.getTaskId()).isNotNull();
        assertThat(result.getTaskId())
            .matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");

        canProceed.countDown();
      } finally {
        shutdownExecutor(executor);
      }
    }

    @Test
    @DisplayName("Should not have statistics in already in progress result")
    void shouldNotHaveStatisticsInAlreadyInProgressResult() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      ExecutorService executor = newExecutor();
      try {
        executor.submit(() -> raceService.syncAllRaces());
        assertTrue(syncStarted.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));

        SyncResultDto result = raceService.syncAllRaces();

        assertThat(result.getStatistics()).isNull();
        assertThat(result.getErrors()).isNull();

        canProceed.countDown();
      } finally {
        shutdownExecutor(executor);
      }
    }

    @Test
    @DisplayName("Should allow new sync after previous one completes")
    void shouldAllowNewSyncAfterPreviousOneCompletes() {
      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      SyncResultDto firstResult = raceService.syncAllRaces();
      assertThat(firstResult.isSuccess()).isTrue();

      when(raceRepository.findByKey(anyString())).thenReturn(Optional.of(humanEntity));

      SyncResultDto secondResult = raceService.syncAllRaces();
      assertThat(secondResult.isSuccess()).isTrue();
      assertThat(secondResult.getMessage()).isNotEqualTo("Sync already in progress");
    }

    @Test
    @DisplayName("Should release progress tracker even when sync fails")
    void shouldReleaseProgressTrackerEvenWhenSyncFails() {
      mockFetchAllThrows(new RuntimeException("API error"));
      stubSyncMetricsAll();

      SyncResultDto firstResult = raceService.syncAllRaces();
      assertThat(firstResult.isSuccess()).isFalse();

      org.mockito.Mockito.reset(apiClient, raceRepository, raceMapper, syncMetrics);

      mockFetchAll(List.of(humanDto, elfDto));
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

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
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      ExecutorService executor = newExecutor();
      try {
        executor.submit(() -> raceService.syncAllRaces());
        assertTrue(syncStarted.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));

        SyncStatusDto status = raceService.getSyncStatus();
        assertThat(status.isInProgress()).isTrue();

        canProceed.countDown();
      } finally {
        shutdownExecutor(executor);
      }

      SyncStatusDto finalStatus = raceService.getSyncStatus();
      assertThat(finalStatus.isInProgress()).isFalse();
    }

    @Test
    @DisplayName("Should not call syncMetrics when sync is already in progress")
    void shouldNotCallSyncMetricsWhenAlreadyInProgress() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      ExecutorService executor = newExecutor();
      try {
        executor.submit(() -> raceService.syncAllRaces());
        assertTrue(syncStarted.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));

        org.mockito.Mockito.clearInvocations(syncMetrics);

        raceService.syncAllRaces();

        verify(syncMetrics, never()).startOperation();

        canProceed.countDown();
      } finally {
        shutdownExecutor(executor);
      }
    }

    @Test
    @DisplayName("Should not interact with database when sync is already in progress")
    void shouldNotInteractWithDatabaseWhenAlreadyInProgress() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      ExecutorService executor = newExecutor();
      try {
        executor.submit(() -> raceService.syncAllRaces());
        assertTrue(syncStarted.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));

        org.mockito.Mockito.clearInvocations(raceRepository);
        org.mockito.Mockito.clearInvocations(apiClient);

        SyncResultDto result = raceService.syncAllRaces();

        assertThat(result.getMessage()).isEqualTo("Sync already in progress");

        verify(raceRepository, never()).findByKey(anyString());
        verify(raceRepository, never()).save(any());
        verify(apiClient, never())
            .fetchAll(anyString(), any(paginatedRef().getClass()));

        canProceed.countDown();
      } finally {
        shutdownExecutor(executor);
      }
    }

    @Test
    @DisplayName("Should have syncedAt timestamp in already in progress result")
    void shouldHaveSyncedAtTimestampInAlreadyInProgressResult() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      ExecutorService executor = newExecutor();
      try {
        executor.submit(() -> raceService.syncAllRaces());
        assertTrue(syncStarted.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));

        LocalDateTime beforeCall = LocalDateTime.now();
        SyncResultDto result = raceService.syncAllRaces();
        LocalDateTime afterCall = LocalDateTime.now();

        assertThat(result.getSyncedAt()).isNotNull();
        assertThat(result.getSyncedAt()).isAfterOrEqualTo(beforeCall.minusSeconds(1));
        assertThat(result.getSyncedAt()).isBeforeOrEqualTo(afterCall.plusSeconds(1));

        canProceed.countDown();
      } finally {
        shutdownExecutor(executor);
      }
    }

    @Test
    @DisplayName("Should generate unique task IDs for each rejected sync attempt")
    void shouldGenerateUniqueTaskIdsForEachRejectedSyncAttempt() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(humanDto, elfDto), syncStarted, canProceed);
      when(raceRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(raceMapper.toEntity(any(Open5eRaceDto.class))).thenReturn(humanEntity);
      when(raceRepository.save(any(Race.class))).thenReturn(humanEntity);
      stubSyncMetricsAll();

      ExecutorService executor = newExecutor();
      try {
        executor.submit(() -> raceService.syncAllRaces());
        assertTrue(syncStarted.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));

        SyncResultDto firstRejected = raceService.syncAllRaces();
        SyncResultDto secondRejected = raceService.syncAllRaces();

        assertThat(firstRejected.getTaskId()).isNotEqualTo(secondRejected.getTaskId());

        canProceed.countDown();
      } finally {
        shutdownExecutor(executor);
      }
    }
  }
}
