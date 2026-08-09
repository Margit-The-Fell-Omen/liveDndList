package dev.ushki.livedndlist.service;

import static dev.ushki.livedndlist.service.DndClassService.getSyncResultDto;

import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eFeatDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5ePaginatedResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.response.DndFeatResponse;
import dev.ushki.livedndlist.entity.dndCharacter.DndFeat;
import dev.ushki.livedndlist.entity.dndCharacter.feature.Feature;
import dev.ushki.livedndlist.enums.AnnotationSource;
import dev.ushki.livedndlist.enums.FeatureSourceType;
import dev.ushki.livedndlist.enums.SyncAction;
import dev.ushki.livedndlist.mapper.FeatMapper;
import dev.ushki.livedndlist.repository.DndFeatRepository;
import dev.ushki.livedndlist.repository.FeatureRepository;
import dev.ushki.livedndlist.service.features.FeatureCatalogService;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import dev.ushki.livedndlist.service.sync.SyncProgressTracker;
import dev.ushki.livedndlist.service.sync.SyncResult;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DndFeatService {

  private static final String API_PATH = "/v2/feats/";
  private static final String FEATURE_KEY_PREFIX = "feat_";

  private final DndFeatRepository featRepository;
  private final FeatMapper featMapper;
  private final Open5eApiClient apiClient;
  private final SyncMetrics syncMetrics;
  private final FeatureRepository featureRepository;
  private final FeatureCatalogService featureCatalogService;

  private final SyncProgressTracker progressTracker = new SyncProgressTracker();

  public SyncStatusDto getSyncStatus() {
    return progressTracker.getStatus();
  }

  @Transactional
  public SyncResultDto syncAllFeats() {
    String taskId = UUID.randomUUID().toString();

    if (!progressTracker.tryStart()) {
      return buildAlreadyInProgressResult(taskId);
    }

    long startTime = System.currentTimeMillis();
    SyncResult result = new SyncResult();

    try {
      syncMetrics.startOperation();
      progressTracker.setOperation("Fetching data from API");
      log.info("Starting feat sync from Open5e API");

      List<Open5eFeatDto> allFeats = fetchAllFromApi();
      progressTracker.setTotal(allFeats.size());

      log.info("Fetched {} feats from API", allFeats.size());
      progressTracker.setOperation("Saving to database");

      for (Open5eFeatDto dto : allFeats) {
        long itemStart = System.currentTimeMillis();
        processFeat(dto, result);
        long itemDuration = System.currentTimeMillis() - itemStart;
        syncMetrics.recordRequest(itemDuration, true);
        progressTracker.incrementProcessed();
      }

      featureCatalogService.invalidateCache();

      long duration = System.currentTimeMillis() - startTime;
      log.info("Sync completed in {}ms. Created: {}, Updated: {}, Failed: {}",
          duration, result.getCreated(), result.getUpdated(), result.getFailed());

      return buildSuccessResult(result, allFeats.size(), duration, taskId);

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
      long featCount = featRepository.count();

      List<Feature> featFeatures = featureRepository.findAll().stream()
          .filter(f -> f.getSourceType() == FeatureSourceType.FEAT)
          .toList();
      featureRepository.deleteAll(featFeatures);

      featRepository.deleteAll();
      featureCatalogService.invalidateCache();

      return SyncResultDto.builder()
          .success(true)
          .message("Deleted feats: " + featCount + ", associated features: " + featFeatures.size())
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

  private List<Open5eFeatDto> fetchAllFromApi() {
    return apiClient.fetchAll(
        API_PATH,
        new ParameterizedTypeReference<Open5ePaginatedResponse<Open5eFeatDto>>() {
        }
    );
  }

  private void processFeat(Open5eFeatDto dto, SyncResult result) {
    try {
      SyncAction action = saveOrUpdate(dto);
      if (action == SyncAction.CREATED) {
        result.recordCreated();
      } else {
        result.recordUpdated();
      }
    } catch (Exception e) {
      result.recordError(dto.getName(), e);
      log.error("Error processing feat '{}': {}", dto.getName(), e.getMessage(), e);
    }
  }

  private SyncAction saveOrUpdate(Open5eFeatDto dto) {
    Optional<DndFeat> existing = featRepository.findByKey(dto.getKey());

    DndFeat feat;
    SyncAction action;
    if (existing.isPresent()) {
      feat = existing.get();
      featMapper.updateEntity(feat, dto);
      featRepository.save(feat);
      action = SyncAction.UPDATED;
    } else {
      feat = featMapper.toEntity(dto);
      featRepository.save(feat);
      action = SyncAction.CREATED;
    }

    upsertFeatureForFeat(feat);
    return action;
  }

  private void upsertFeatureForFeat(DndFeat feat) {
    String featureKey = FEATURE_KEY_PREFIX + feat.getKey();

    Optional<Feature> existingOpt = featureRepository.findByKey(featureKey);

    if (existingOpt.isPresent()) {
      Feature existing = existingOpt.get();
      if (existing.getEffectsAnnotatedBy() == AnnotationSource.MANUAL
          || existing.getEffectsAnnotatedBy() == AnnotationSource.FILE_LOADER) {
        existing.setName(feat.getName());
        existing.setDescription(buildDescription(feat));
        existing.setDocument(feat.getDocument());
        existing.setPrerequisite(feat.getPrerequisite());
        featureRepository.save(existing);
        return;
      }
      existing.setName(feat.getName());
      existing.setDescription(buildDescription(feat));
      existing.setDocument(feat.getDocument());
      existing.setPrerequisite(feat.getPrerequisite());
      existing.setEffectsAnnotatedAt(OffsetDateTime.now());
      featureRepository.save(existing);
    } else {
      Feature newFeature = Feature.builder()
          .key(featureKey)
          .name(feat.getName())
          .description(buildDescription(feat))
          .sourceType(FeatureSourceType.FEAT)
          .sourceKey(feat.getKey())
          .prerequisite(feat.getPrerequisite())
          .document(feat.getDocument())
          .effectsAnnotatedBy(AnnotationSource.OPEN5E_SYNC)
          .effectsAnnotatedAt(OffsetDateTime.now())
          .build();
      featureRepository.save(newFeature);
    }
  }

  private String buildDescription(DndFeat feat) {
    StringBuilder sb = new StringBuilder();
    if (feat.getDesc() != null && !feat.getDesc().isBlank()) {
      sb.append(feat.getDesc());
    }
    if (feat.getBenefits() != null && !feat.getBenefits().isEmpty()) {
      if (!sb.isEmpty()) {
        sb.append("\n\n");
      }
      for (String benefit : feat.getBenefits()) {
        sb.append("* ").append(benefit).append("\n");
      }
    }
    return !sb.isEmpty() ? sb.toString() : null;
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

  public List<DndFeatResponse> getAllFeats() {
    List<DndFeat> feats = featRepository.findAll();
    return feats.stream()
        .map(featMapper::toDto)
        .toList();
  }
}
