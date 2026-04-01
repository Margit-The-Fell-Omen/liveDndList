package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import dev.ushki.livedndlist.dto.open5e.Open5eClassDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5eClassResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.DndClass;
import dev.ushki.livedndlist.mapper.DndClassMapper;
import dev.ushki.livedndlist.repository.DndClassRepository;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

  private Open5eClassService classService;

  private Open5eClassDto fighterDto;
  private Open5eClassDto wizardDto;
  private DndClass fighterEntity;
  private DndClass wizardEntity;
  private Open5eClassResponse apiResponse;

  @BeforeEach
  void setUp() {
    classService = new Open5eClassService(
        dndClassRepository,
        dndClassMapper,
        apiClient,
        syncMetrics
    );

    fighterDto = new Open5eClassDto();
    fighterDto.setName("Fighter");
    fighterDto.setSlug("fighter");
    fighterDto.setHitDice("1d10");

    wizardDto = new Open5eClassDto();
    wizardDto.setName("Wizard");
    wizardDto.setSlug("wizard");
    wizardDto.setHitDice("1d6");

    fighterEntity = DndClass.builder()
        .id(1L)
        .name("Fighter")
        .slug("fighter")
        .hitDice("1d10")
        .build();

    wizardEntity = DndClass.builder()
        .id(2L)
        .name("Wizard")
        .slug("wizard")
        .hitDice("1d6")
        .build();

    apiResponse = new Open5eClassResponse();
    apiResponse.setResults(List.of(fighterDto, wizardDto));
    apiResponse.setNext(null);
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
      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(apiResponse);
      when(apiClient.extractNextPath(null)).thenReturn(null);
      when(dndClassRepository.findBySlug("fighter")).thenReturn(Optional.empty());
      when(dndClassRepository.findBySlug("wizard")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(fighterDto)).thenReturn(fighterEntity);
      when(dndClassMapper.toEntity(wizardDto)).thenReturn(wizardEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenAnswer(i -> i.getArgument(0));
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(true));

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
      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(apiResponse);
      when(apiClient.extractNextPath(null)).thenReturn(null);
      when(dndClassRepository.findBySlug("fighter")).thenReturn(Optional.of(fighterEntity));
      when(dndClassRepository.findBySlug("wizard")).thenReturn(Optional.of(wizardEntity));
      when(dndClassRepository.save(any(DndClass.class))).thenAnswer(i -> i.getArgument(0));
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(true));

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getCreated()).isZero();
      assertThat(result.getStatistics().getUpdated()).isEqualTo(2);

      verify(dndClassMapper, times(2)).updateEntity(any(DndClass.class), any(Open5eClassDto.class));
      verify(dndClassRepository, times(2)).save(any(DndClass.class));
    }

    @Test
    @DisplayName("Should handle mixed create and update operations")
    void shouldHandleMixedCreateAndUpdate() {
      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(apiResponse);
      when(apiClient.extractNextPath(null)).thenReturn(null);
      when(dndClassRepository.findBySlug("fighter")).thenReturn(Optional.of(fighterEntity));
      when(dndClassRepository.findBySlug("wizard")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(wizardDto)).thenReturn(wizardEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenAnswer(i -> i.getArgument(0));
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(true));

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getCreated()).isEqualTo(1);
      assertThat(result.getStatistics().getUpdated()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle API error during sync")
    void shouldHandleApiErrorDuringSync() {
      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenThrow(new RuntimeException("API connection failed"));
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(false));

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Critical error");
      assertThat(result.getErrors()).isNotNull();
      assertThat(result.getErrors()).contains("API connection failed");

      verify(dndClassRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle pagination with multiple pages")
    void shouldHandlePaginationWithMultiplePages() {
      Open5eClassResponse firstPage = new Open5eClassResponse();
      firstPage.setResults(List.of(fighterDto));
      firstPage.setNext("https://api.open5e.com/classes/?page=2");

      Open5eClassResponse secondPage = new Open5eClassResponse();
      secondPage.setResults(List.of(wizardDto));
      secondPage.setNext(null);

      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(firstPage);
      when(apiClient.extractNextPath("https://api.open5e.com/classes/?page=2"))
          .thenReturn("/classes/?page=2");
      when(apiClient.getByPath("/classes/?page=2", Open5eClassResponse.class))
          .thenReturn(secondPage);
      when(apiClient.extractNextPath(null)).thenReturn(null);
      when(dndClassRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenAnswer(i -> i.getArgument(0));
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(true));

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(2);

      verify(apiClient, times(2)).getByPath(anyString(), eq(Open5eClassResponse.class));
    }

    @Test
    @DisplayName("Should handle empty API response")
    void shouldHandleEmptyApiResponse() {
      Open5eClassResponse emptyResponse = new Open5eClassResponse();
      emptyResponse.setResults(List.of());
      emptyResponse.setNext(null);

      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(emptyResponse);
      when(apiClient.extractNextPath(null)).thenReturn(null);
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getStatistics().getTotalFetched()).isZero();
      assertThat(result.getStatistics().getCreated()).isZero();

      verify(dndClassRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should record sync completed with errors when some classes fail")
    void shouldRecordSyncCompletedWithErrors() {
      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(apiResponse);
      when(apiClient.extractNextPath(null)).thenReturn(null);
      when(dndClassRepository.findBySlug("fighter")).thenReturn(Optional.empty());
      when(dndClassRepository.findBySlug("wizard")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(fighterDto)).thenReturn(fighterEntity);
      when(dndClassMapper.toEntity(wizardDto)).thenThrow(new RuntimeException("Mapping error"));
      when(dndClassRepository.save(fighterEntity)).thenReturn(fighterEntity);
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(true));

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
  @DisplayName("syncBySlug")
  class SyncBySlugTests {

    @Test
    @DisplayName("Should sync class by slug successfully when creating")
    void shouldSyncClassBySlugSuccessfullyWhenCreating() {
      when(apiClient.getBySlug("/classes/", "fighter", Open5eClassDto.class))
          .thenReturn(fighterDto);
      when(dndClassRepository.findBySlug("fighter")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(fighterDto)).thenReturn(fighterEntity);
      when(dndClassRepository.save(fighterEntity)).thenReturn(fighterEntity);

      SyncResultDto result = classService.syncBySlug("fighter");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).contains("Class created");
      assertThat(result.getMessage()).contains("Fighter");
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(1);
      assertThat(result.getStatistics().getCreated()).isEqualTo(1);
      assertThat(result.getStatistics().getUpdated()).isZero();

      verify(dndClassMapper).toEntity(fighterDto);
      verify(dndClassRepository).save(fighterEntity);
    }

    @Test
    @DisplayName("Should sync class by slug successfully when updating")
    void shouldSyncClassBySlugSuccessfullyWhenUpdating() {
      when(apiClient.getBySlug("/classes/", "fighter", Open5eClassDto.class))
          .thenReturn(fighterDto);
      when(dndClassRepository.findBySlug("fighter")).thenReturn(Optional.of(fighterEntity));
      when(dndClassRepository.save(fighterEntity)).thenReturn(fighterEntity);

      SyncResultDto result = classService.syncBySlug("fighter");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getMessage()).contains("Class updated");
      assertThat(result.getMessage()).contains("Fighter");
      assertThat(result.getStatistics().getCreated()).isZero();
      assertThat(result.getStatistics().getUpdated()).isEqualTo(1);

      verify(dndClassMapper).updateEntity(fighterEntity, fighterDto);
      verify(dndClassRepository).save(fighterEntity);
    }

    @Test
    @DisplayName("Should handle API error when syncing by slug")
    void shouldHandleApiErrorWhenSyncingBySlug() {
      when(apiClient.getBySlug("/classes/", "nonexistent", Open5eClassDto.class))
          .thenThrow(new RuntimeException("Class not found"));

      SyncResultDto result = classService.syncBySlug("nonexistent");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Critical error");
      assertThat(result.getErrors()).contains("Class not found");

      verify(dndClassRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle database error when syncing by slug")
    void shouldHandleDatabaseErrorWhenSyncingBySlug() {
      when(apiClient.getBySlug("/classes/", "fighter", Open5eClassDto.class))
          .thenReturn(fighterDto);
      when(dndClassRepository.findBySlug("fighter")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(fighterDto)).thenReturn(fighterEntity);
      when(dndClassRepository.save(fighterEntity))
          .thenThrow(new RuntimeException("Database error"));

      SyncResultDto result = classService.syncBySlug("fighter");

      assertThat(result).isNotNull();
      assertThat(result.isSuccess()).isFalse();
      assertThat(result.getMessage()).contains("Critical error");
    }

    @Test
    @DisplayName("Should record duration when syncing by slug")
    void shouldRecordDurationWhenSyncingBySlug() {
      when(apiClient.getBySlug("/classes/", "fighter", Open5eClassDto.class))
          .thenReturn(fighterDto);
      when(dndClassRepository.findBySlug("fighter")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(fighterDto)).thenReturn(fighterEntity);
      when(dndClassRepository.save(fighterEntity)).thenReturn(fighterEntity);

      SyncResultDto result = classService.syncBySlug("fighter");

      assertThat(result.getStatistics().getDurationMs()).isGreaterThanOrEqualTo(0);
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
      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(apiResponse);
      when(apiClient.extractNextPath(null)).thenReturn(null);
      when(dndClassRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(true));

      classService.syncAllClasses();

      verify(syncMetrics).startOperation();
      verify(syncMetrics).endOperation();
    }

    @Test
    @DisplayName("Should record request for each class")
    void shouldRecordRequestForEachClass() {
      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(apiResponse);
      when(apiClient.extractNextPath(null)).thenReturn(null);
      when(dndClassRepository.findBySlug(anyString())).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(any(Open5eClassDto.class))).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(true));

      classService.syncAllClasses();

      verify(syncMetrics, times(2)).recordRequest(anyLong(), eq(true));
    }

    @Test
    @DisplayName("Should record failure metric on API error")
    void shouldRecordFailureMetricOnApiError() {
      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenThrow(new RuntimeException("API error"));
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(false));

      classService.syncAllClasses();

      verify(syncMetrics).recordRequest(anyLong(), eq(false));
    }

    @Test
    @DisplayName("Should call endOperation even on error")
    void shouldCallEndOperationEvenOnError() {
      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenThrow(new RuntimeException("API error"));
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(false));

      classService.syncAllClasses();

      verify(syncMetrics).endOperation();
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle null results in API response")
    void shouldHandleNullResultsInApiResponse() {
      Open5eClassResponse nullResultsResponse = new Open5eClassResponse();
      nullResultsResponse.setResults(null);
      nullResultsResponse.setNext(null);

      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(nullResultsResponse);
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
      nullNameDto.setSlug("null-class");

      Open5eClassResponse response = new Open5eClassResponse();
      response.setResults(List.of(nullNameDto));
      response.setNext(null);

      when(apiClient.getByPath("/classes/", Open5eClassResponse.class))
          .thenReturn(response);
      when(apiClient.extractNextPath(null)).thenReturn(null);
      when(dndClassRepository.findBySlug("null-class")).thenReturn(Optional.empty());
      when(dndClassMapper.toEntity(nullNameDto)).thenReturn(fighterEntity);
      when(dndClassRepository.save(any(DndClass.class))).thenReturn(fighterEntity);
      doNothing().when(syncMetrics).startOperation();
      doNothing().when(syncMetrics).endOperation();
      doNothing().when(syncMetrics).recordRequest(anyLong(), eq(true));

      SyncResultDto result = classService.syncAllClasses();

      assertThat(result).isNotNull();
      assertThat(result.getStatistics().getTotalFetched()).isEqualTo(1);
    }
  }
}
