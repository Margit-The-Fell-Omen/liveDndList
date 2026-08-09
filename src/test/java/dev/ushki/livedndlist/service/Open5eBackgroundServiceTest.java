package dev.ushki.livedndlist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundBenefitDto;
import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.dndCharacter.background.Background;
import dev.ushki.livedndlist.entity.dndCharacter.background.BackgroundBenefit;
import dev.ushki.livedndlist.mapper.BackgroundMapper;
import dev.ushki.livedndlist.repository.BackgroundRepository;
import dev.ushki.livedndlist.service.features.FeatureCatalogService;
import dev.ushki.livedndlist.service.features.FeatureUpsertHelper;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import dev.ushki.livedndlist.service.sync.SyncProgressTracker;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class Open5eBackgroundServiceTest {

  @Mock
  private BackgroundRepository backgroundRepository;
  @Mock
  private BackgroundMapper backgroundMapper;
  @Mock
  private Open5eApiClient apiClient;
  @Mock
  private SyncMetrics syncMetrics;
  @Mock
  private FeatureUpsertHelper featureUpsertHelper;

  @Mock
  private FeatureCatalogService featureCatalogService;

  @InjectMocks
  private DndBackgroundService backgroundService;

  private Open5eBackgroundDto sampleDto;
  private Background sampleEntity;

  @BeforeEach
  void setUp() {
    sampleDto = new Open5eBackgroundDto();
    sampleDto.setKey("acolyte");
    sampleDto.setName("Acolyte");

    sampleEntity = Background.builder()
        .key("acolyte")
        .name("Acolyte")
        .benefits(Collections.emptyList())
        .build();
  }

  // ---------------------------------------------------------------
  // Helper: mock apiClient.fetchAll for backgrounds
  // ---------------------------------------------------------------

  private void mockFetchAll(List<Open5eBackgroundDto> results) {
    when(apiClient.fetchAll(
        anyString(),
        any(ParameterizedTypeReference.class))
    ).thenReturn(results);
  }

  private void mockFetchAllThrows(RuntimeException ex) {
    when(apiClient.fetchAll(
        anyString(),
        any(ParameterizedTypeReference.class))
    ).thenThrow(ex);
  }

  // ---------------------------------------------------------------
  // syncAllBackgrounds
  // ---------------------------------------------------------------

  @Test
  void syncAllBackgroundsShouldReturnErrorWhenAlreadyInProgress() {
    SyncProgressTracker tracker = (SyncProgressTracker) ReflectionTestUtils.getField(
        backgroundService, "progressTracker");
    assert tracker != null;
    tracker.tryStart();

    SyncResultDto result = backgroundService.syncAllBackgrounds();

    assertFalse(result.isSuccess());
    assertEquals("Sync already in progress", result.getMessage());

    tracker.finish();
  }

  @Test
  void syncAllBackgroundsShouldHandlePaginationAndCreateNew() {
    // Pagination is now handled entirely inside apiClient.fetchAll().
    // The service receives all results in a single list — we just return
    // the combined items that would have come from both pages.
    mockFetchAll(List.of(sampleDto));
    when(backgroundRepository.findByKey(anyString())).thenReturn(Optional.empty());
    when(backgroundMapper.toEntity(any())).thenReturn(sampleEntity);

    SyncResultDto result = backgroundService.syncAllBackgrounds();

    assertTrue(result.isSuccess());
    assertEquals(1, result.getStatistics().getTotalFetched());
    assertEquals(1, result.getStatistics().getCreated());
    verify(backgroundRepository).save(any());
  }

  @Test
  void syncAllBackgroundsShouldUpdateExisting() {
    mockFetchAll(List.of(sampleDto));
    when(backgroundRepository.findByKey(anyString())).thenReturn(Optional.of(sampleEntity));

    SyncResultDto result = backgroundService.syncAllBackgrounds();

    assertTrue(result.isSuccess());
    assertEquals(1, result.getStatistics().getUpdated());
    verify(backgroundMapper).updateEntity(eq(sampleEntity), eq(sampleDto));
    verify(backgroundRepository).save(sampleEntity);
  }

  @Test
  void syncAllBackgroundsShouldRecordErrorsWhenProcessBackgroundFails() {
    mockFetchAll(List.of(sampleDto));
    when(backgroundRepository.findByKey(anyString())).thenThrow(new RuntimeException("DB Error"));

    SyncResultDto result = backgroundService.syncAllBackgrounds();

    assertFalse(result.isSuccess());
    assertEquals(1, result.getStatistics().getFailed());
    assertNotNull(result.getErrors());
  }

  @Test
  void syncAllBackgroundsShouldHandleCriticalException() {
    mockFetchAllThrows(new RuntimeException("API Down"));

    SyncResultDto result = backgroundService.syncAllBackgrounds();

    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("Critical error"));
  }

  // ---------------------------------------------------------------
  // clearAll
  // ---------------------------------------------------------------

  @Test
  void clearAllShouldDeleteSuccessfully() {
    when(backgroundRepository.count()).thenReturn(5L);

    SyncResultDto result = backgroundService.clearAll();

    assertTrue(result.isSuccess());
    assertEquals("Deleted classes: 5", result.getMessage());
    verify(backgroundRepository).deleteAll();
  }

  @Test
  void clearAllShouldReturnErrorWhenRepositoryThrowsException() {
    doThrow(new RuntimeException("Delete Failed")).when(backgroundRepository).deleteAll();

    SyncResultDto result = backgroundService.clearAll();

    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("Delete error"));
  }

  // ---------------------------------------------------------------
  // getBenefitsByBackground
  // ---------------------------------------------------------------

  @Test
  void getBenefitsByBackgroundShouldReturnListWhenFound() {
    BackgroundBenefit benefit = new BackgroundBenefit();
    sampleEntity.setBenefits(List.of(benefit));
    Open5eBackgroundBenefitDto benefitDto = new Open5eBackgroundBenefitDto();

    when(backgroundRepository.findByKey("acolyte")).thenReturn(Optional.of(sampleEntity));
    when(backgroundMapper.toBenefitDto(benefit)).thenReturn(benefitDto);

    List<Open5eBackgroundBenefitDto> result =
        backgroundService.getBenefitsByBackground("acolyte");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void getBenefitsByBackgroundShouldReturnNullAndLogErrorWhenKeyNotFound() {
    when(backgroundRepository.findByKey("invalid-key")).thenReturn(Optional.empty());

    List<Open5eBackgroundBenefitDto> result =
        backgroundService.getBenefitsByBackground("invalid-key");

    assertNull(result);
  }

  // ---------------------------------------------------------------
  // getSyncStatus
  // ---------------------------------------------------------------

  @Test
  void getSyncStatusShouldReturnCurrentStatus() {
    SyncStatusDto status = backgroundService.getSyncStatus();

    assertNotNull(status);
    assertFalse(status.isInProgress());
  }

  // ---------------------------------------------------------------
  // getAllBackgrounds
  // ---------------------------------------------------------------

  @Test
  void getAllBackgroundsShouldReturnList() {
    when(backgroundRepository.findAll()).thenReturn(List.of(sampleEntity));
    when(backgroundMapper.toDto(sampleEntity)).thenReturn(sampleDto);

    List<Open5eBackgroundDto> result = backgroundService.getAllBackgrounds();

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void getAllBackgroundsShouldHandleEmptyList() {
    when(backgroundRepository.findAll()).thenReturn(Collections.emptyList());

    List<Open5eBackgroundDto> result = backgroundService.getAllBackgrounds();

    assertTrue(result.isEmpty());
  }
}
