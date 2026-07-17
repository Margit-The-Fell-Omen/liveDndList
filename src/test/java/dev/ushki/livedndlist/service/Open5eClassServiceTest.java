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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eClassDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndClass;
import dev.ushki.livedndlist.mapper.DndClassMapper;
import dev.ushki.livedndlist.repository.DndClassRepository;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
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
class Open5eClassServiceTest {

  @Mock
  private DndClassRepository dndClassRepository;

  @Mock
  private DndClassMapper dndClassMapper;

  @Mock
  private Open5eApiClient apiClient;

  @Mock
  private SyncMetrics syncMetrics;

  private DndClassService classService;

  private Open5eClassDto fighterDto;
  private Open5eClassDto wizardDto;
  private DndClass fighterEntity;
  private DndClass wizardEntity;

  @BeforeEach
  void setUp() {
    classService = new DndClassService(
        dndClassRepository,
        dndClassMapper,
        apiClient,
        syncMetrics
    );

    fighterDto = new Open5eClassDto();
    fighterDto.setName("Fighter");
    fighterDto.setKey("fighter");
    fighterDto.setHitDice("1d10");

    wizardDto = new Open5eClassDto();
    wizardDto.setName("Wizard");
    wizardDto.setKey("wizard");
    wizardDto.setHitDice("1d6");

    fighterEntity = DndClass.builder()
        .id(1L)
        .name("Fighter")
        .key("fighter")
        .hitDice("1d10")
        .build();

    wizardEntity = DndClass.builder()
        .id(2L)
        .name("Wizard")
        .key("wizard")
        .hitDice("1d6")
        .build();
  }

  // ---------------------------------------------------------------
  // Helper: mock apiClient.fetchAll to return the given DTOs.
  // Uses any() for the ParameterizedTypeReference argument because
  // Mockito cannot match generic type-ref instances by value.
  // ---------------------------------------------------------------
  private void mockFetchAll(List<Open5eClassDto> results) {
    when(apiClient.fetchAll(
        eq("/v2/classes/"),
        any(ParameterizedTypeReference.class))
    ).thenReturn(results);
  }

  private void mockFetchAllThrows(RuntimeException ex) {
    when(apiClient.fetchAll(
        eq("/v2/classes/"),
        any(ParameterizedTypeReference.class))
    ).thenThrow(ex);
  }

  // Blocks inside fetchAll until the latch is released — used for concurrency tests
  private void mockFetchAllBlocking(List<Open5eClassDto> results,
      CountDownLatch started,
      CountDownLatch canProceed) {
    when(apiClient.fetchAll(
        eq("/v2/classes/"),
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

  @Nested
  @DisplayName("getSyncStatus")
  class GetSyncStatusTests {

    @Test
    @DisplayName("Should return sync status when idle")
    void shouldReturnSyncStatusWhenIdle() {
      SyncStatusDto status = classService.getSyncStatus();

      assertThat(status).isNotNull();
      assertThat(status.isInProgress()).isFalse();
    }
  }

  @Nested
  @DisplayName("syncAllClasses")
  class SyncAllClassesTests {

    @Test
    @DisplayName("Should sync all classes successfully when creating new classes")
    void shouldSyncAllClassesSuccessfullyWhenCreating() {
      mockFetchAll(List.of(fighterDto, wizardDto));
      when(dndClassRepository.findByKey("fighter")).thenReturn(Optional.empty());
      when(dndClassRepository.findByKey("wizard")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(fighterDto)).thenReturn(fighterEntity);
      when(dndClassMapper.toEntity(wizardDto)).thenReturn(wizardEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsHappyPath();

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).isEqualTo("Sync completed successfully");
      assertThat(result.getStatistics()).isNotNull();
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(2);
      assertThat(result.getStatistics().getCreated()).isEqualTo(2);
      assertThat(result.getStatistics().getUpdated()).isZero();
      assertThat(result.getStatistics().getFailed()).isZero();

      verify(dndClassRepository, times(2)).save(any(DndClass.class));
      verify(syncMetrics).startOperation();
      verify(syncMetrics).endOperation();
    }

    @Test
    @DisplayName("Should sync all classes successfully when updating existing classes")
    void shouldSyncAllClassesSuccessfullyWhenUpdating() {
      mockFetchAll(List.of(fighterDto, wizardDto));
      when(dndClassRepository.findByKey("fighter")).thenReturn(Optional.of(fighterEntity));
      when(dndClassRepository.findByKey("wizard")).thenReturn(Optional.of(wizardEntity));
      when(dndClassRepository.save(any(DndClass.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsHappyPath();

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getCreated()).isZero();
      assertThat(result.getStatistics().getUpdated()).isEqualTo(2);

      verify(dndClassMapper, times(2))
          .updateEntity(any(DndClass.class), any(Open5eClassDto.class));
      verify(dndClassRepository, times(2)).save(any(DndClass.class));
    }

    @Test
    @DisplayName("Should handle mixed create and update operations")
    void shouldHandleMixedCreateAndUpdate() {
      mockFetchAll(List.of(fighterDto, wizardDto));
      when(dndClassRepository.findByKey("fighter")).thenReturn(Optional.of(fighterEntity));
      when(dndClassRepository.findByKey("wizard")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(wizardDto)).thenReturn(wizardEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsHappyPath();

      SyncResultDto result = classService.syncAllClasses();

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

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Critical error");
      assertThat(result.getErrors()).isNotNull();
      assertThat(result.getErrors()).contains("API connection failed");

      verify(dndClassRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle pagination transparently (client returns combined results)")
    void shouldHandlePaginationTransparently() {
      mockFetchAll(List.of(fighterDto, wizardDto));
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenAnswer(i -> i.getArgument(0));
      stubSyncMetricsHappyPath();

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(2);

      verify(apiClient, times(1))
          .fetchAll(eq("/v2/classes/"), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Should handle empty API response")
    void shouldHandleEmptyApiResponse() {
      mockFetchAll(List.of());
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getTotalFetched()).isZero();
      assertThat(result.getStatistics().getCreated()).isZero();

      verify(dndClassRepository, never()).save(any());
      verify(syncMetrics, never()).recordRequest(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Should record sync completed with errors when some classes fail")
    void shouldRecordSyncCompletedWithErrors() {
      mockFetchAll(List.of(fighterDto, wizardDto));
      when(dndClassRepository.findByKey("fighter")).thenReturn(Optional.empty());
      when(dndClassRepository.findByKey("wizard")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(fighterDto)).thenReturn(fighterEntity);
      when(dndClassMapper.toEntity(wizardDto))
          .thenThrow(new RuntimeException("Mapping error"));
      when(dndClassRepository.save(fighterEntity)).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).isEqualTo("Sync completed with errors");
      assertThat(result.getStatistics().getCreated()).isEqualTo(1);
      assertThat(result.getStatistics().getFailed()).isEqualTo(1);
      assertThat(result.getErrors()).isNotNull();
      assertThat(result.getErrors()).isNotEmpty();
    }
  }

  @Nested
  @DisplayName("clearAll")
  class ClearAllTests {

    @Test
    @DisplayName("Should clear all classes successfully")
    void shouldClearAllClassesSuccessfully() {
      when(dndClassRepository.count()).thenReturn(10L);
      doNothing().when(dndClassRepository).deleteAll();

      SyncResultDto result = classService.clearAll();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).isEqualTo("Deleted classes: 10");
      assertThat(result.getSyncedAt()).isNotNull();

      verify(dndClassRepository).count();
      verify(dndClassRepository).deleteAll();
    }

    @Test
    @DisplayName("Should handle clear all when no classes exist")
    void shouldHandleClearAllWhenNoClassesExist() {
      when(dndClassRepository.count()).thenReturn(0L);
      doNothing().when(dndClassRepository).deleteAll();

      SyncResultDto result = classService.clearAll();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).isEqualTo("Deleted classes: 0");

      verify(dndClassRepository).deleteAll();
    }

    @Test
    @DisplayName("Should handle database error when clearing all")
    void shouldHandleDatabaseErrorWhenClearingAll() {
      when(dndClassRepository.count()).thenReturn(10L);
      doThrow(new RuntimeException("Database error")).when(dndClassRepository).deleteAll();

      SyncResultDto result = classService.clearAll();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Delete error");
      assertThat(result.getMessage()).contains("Database error");
    }

    @Test
    @DisplayName("Should handle count error when clearing all")
    void shouldHandleCountErrorWhenClearingAll() {
      when(dndClassRepository.count()).thenThrow(new RuntimeException("Count failed"));

      SyncResultDto result = classService.clearAll();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Delete error");

      verify(dndClassRepository, never()).deleteAll();
    }
  }

  @Nested
  @DisplayName("Sync Metrics Integration")
  class SyncMetricsTests {

    @Test
    @DisplayName("Should call start and end operation on sync")
    void shouldCallStartAndEndOperationOnSync() {
      mockFetchAll(List.of(fighterDto, wizardDto));
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      classService.syncAllClasses();

      verify(syncMetrics).startOperation();
      verify(syncMetrics).endOperation();
    }

    @Test
    @DisplayName("Should record request for each class")
    void shouldRecordRequestForEachClass() {
      mockFetchAll(List.of(fighterDto, wizardDto));
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      classService.syncAllClasses();

      verify(syncMetrics, times(2)).recordRequest(anyLong(), eq(true));
    }

    @Test
    @DisplayName("Should record failure metric on API error")
    void shouldRecordFailureMetricOnApiError() {
      mockFetchAllThrows(new RuntimeException("API error"));
      stubSyncMetricsErrorPath();

      classService.syncAllClasses();

      verify(syncMetrics).recordRequest(anyLong(), eq(false));
    }

    @Test
    @DisplayName("Should call endOperation even on error")
    void shouldCallEndOperationEvenOnError() {
      mockFetchAllThrows(new RuntimeException("API error"));
      stubSyncMetricsErrorPath();

      classService.syncAllClasses();

      verify(syncMetrics).endOperation();
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle empty results from fetchAll")
    void shouldHandleEmptyResultsFromFetchAll() {
      mockFetchAll(List.of());
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getTotalFetched()).isZero();
    }

    @Test
    @DisplayName("Should handle class with null name")
    void shouldHandleClassWithNullName() {
      Open5eClassDto nullNameDto = new Open5eClassDto();
      nullNameDto.setName(null);
      nullNameDto.setKey("null-class");

      mockFetchAll(List.of(nullNameDto));
      when(dndClassRepository.findByKey("null-class")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(nullNameDto)).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("Concurrent Sync and Already In Progress Tests")
  class ConcurrentSyncTests {

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @Test
    @DisplayName("Should return already in progress when sync is already running")
    void shouldReturnAlreadyInProgressWhenSyncIsRunning() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(fighterDto, wizardDto), syncStarted, canProceed);
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<SyncResultDto> firstSync = executor.submit(() -> classService.syncAllClasses());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS), "First sync should start");

      SyncResultDto secondResult = classService.syncAllClasses();

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

      mockFetchAllBlocking(List.of(fighterDto, wizardDto), syncStarted, canProceed);
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> classService.syncAllClasses());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      SyncResultDto result = classService.syncAllClasses();

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

      mockFetchAllBlocking(List.of(fighterDto, wizardDto), syncStarted, canProceed);
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> classService.syncAllClasses());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result.getStatistics()).isNull();
      assertThat(result.getErrors()).isNull();

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should allow new sync after previous one completes")
    void shouldAllowNewSyncAfterPreviousOneCompletes() {
      mockFetchAll(List.of(fighterDto, wizardDto));
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      SyncResultDto firstResult = classService.syncAllClasses();
      assertThat(firstResult.isSuccess()).isTrue();

      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.of(fighterEntity));

      SyncResultDto secondResult = classService.syncAllClasses();
      assertThat(secondResult.isSuccess()).isTrue();
      assertThat(secondResult.getMessage()).isNotEqualTo("Sync already in progress");
    }

    @Test
    @DisplayName("Should release progress tracker even when sync fails")
    void shouldReleaseProgressTrackerEvenWhenSyncFails() {
      mockFetchAllThrows(new RuntimeException("API error"));
      stubSyncMetricsErrorPath();

      SyncResultDto firstResult = classService.syncAllClasses();
      assertThat(firstResult.isSuccess()).isFalse();

      org.mockito.Mockito.reset(apiClient, dndClassRepository, dndClassMapper, syncMetrics);

      mockFetchAll(List.of(fighterDto, wizardDto));
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      SyncResultDto secondResult = classService.syncAllClasses();
      assertThat(secondResult.isSuccess()).isTrue();
      assertThat(secondResult.getMessage()).isNotEqualTo("Sync already in progress");
    }

    @Test
    @DisplayName("Should show in progress status during sync")
    void shouldShowInProgressStatusDuringSync() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(fighterDto, wizardDto), syncStarted, canProceed);
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> classService.syncAllClasses());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      SyncStatusDto status = classService.getSyncStatus();
      assertThat(status.isInProgress()).isTrue();

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);

      SyncStatusDto finalStatus = classService.getSyncStatus();
      assertThat(finalStatus.isInProgress()).isFalse();
    }

    @Test
    @DisplayName("Should not call syncMetrics when sync is already in progress")
    void shouldNotCallSyncMetricsWhenAlreadyInProgress() throws Exception {
      CountDownLatch syncStarted = new CountDownLatch(1);
      CountDownLatch canProceed = new CountDownLatch(1);

      mockFetchAllBlocking(List.of(fighterDto, wizardDto), syncStarted, canProceed);
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> classService.syncAllClasses());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      org.mockito.Mockito.clearInvocations(syncMetrics);

      classService.syncAllClasses();

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

      mockFetchAllBlocking(List.of(fighterDto, wizardDto), syncStarted, canProceed);
      when(dndClassRepository.findByKey(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      stubSyncMetricsHappyPath();

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(() -> classService.syncAllClasses());

      assertTrue(syncStarted.await(5, TimeUnit.SECONDS));

      org.mockito.Mockito.clearInvocations(dndClassRepository);
      org.mockito.Mockito.clearInvocations(apiClient);

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result.getMessage()).isEqualTo("Sync already in progress");

      verify(dndClassRepository, never()).findByKey(anyString());
      verify(dndClassRepository, never()).save(any());
      verify(apiClient, never())
          .fetchAll(anyString(), any(ParameterizedTypeReference.class));

      canProceed.countDown();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
