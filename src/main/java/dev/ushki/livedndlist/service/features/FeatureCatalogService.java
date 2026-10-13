package dev.ushki.livedndlist.service.features;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.entity.dndCharacter.feature.Feature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureEffect;
import dev.ushki.livedndlist.enums.FeatureSourceType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.FeatureRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FeatureCatalogService {

  private static final String CACHE_NAMESPACE = "FeatureCatalog";

  private final FeatureRepository featureRepository;
  private final CacheManager cacheManager;

  public List<FeatureRef> findBySource(FeatureSourceType type, String key) {
    if (type == null || key == null || key.isBlank()) {
      return List.of();
    }
    CompositeKey cacheKey = new CompositeKey("bySource", type, key);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey,
        () -> featureRepository.findBySourceTypeAndSourceKey(type, key).stream()
            .map(FeatureCatalogService::toRef)
            .toList());
  }

  public List<FeatureRef> findClassFeaturesUpToLevel(String classKey, int level) {
    if (classKey == null || classKey.isBlank() || level < 1) {
      return List.of();
    }
    CompositeKey cacheKey = new CompositeKey("classUpToLevel", classKey, level);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey,
        () -> featureRepository.findBySourceUpToLevel(FeatureSourceType.CLASS, classKey, level)
            .stream()
            .map(FeatureCatalogService::toRef)
            .toList());
  }

  public List<FeatureRef> findSubclassFeaturesUpToLevel(String subclassKey, int level) {
    if (subclassKey == null || subclassKey.isBlank() || level < 1) {
      return List.of();
    }
    CompositeKey cacheKey = new CompositeKey("subclassUpToLevel", subclassKey, level);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey,
        () -> featureRepository.findBySourceUpToLevel(FeatureSourceType.SUBCLASS, subclassKey, level)
            .stream()
            .map(FeatureCatalogService::toRef)
            .toList());
  }

  public Optional<FeatureRef> findByKey(String featureKey) {
    if (featureKey == null || featureKey.isBlank()) {
      return Optional.empty();
    }
    CompositeKey cacheKey = new CompositeKey("byKey", featureKey);
    FeatureRef cached = cacheManager.get(CACHE_NAMESPACE, cacheKey,
        () -> featureRepository.findByKey(featureKey).map(FeatureCatalogService::toRef).orElse(null));
    return Optional.ofNullable(cached);
  }

  public FeatureRef getByKey(String featureKey) {
    return findByKey(featureKey)
        .orElseThrow(() -> new ResourceNotFoundException("Feature", "key", featureKey));
  }

  public List<FeatureEffectRef> loadEffects(long featureId) {
    CompositeKey cacheKey = new CompositeKey("effects", featureId);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey, () -> {
      Feature feature = featureRepository.findById(featureId)
          .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", featureId));
      List<FeatureEffect> effects = feature.getEffects();
      effects.size();
      return effects.stream().map(FeatureCatalogService::toRef).toList();
    });
  }

  public List<FeatureChoiceRef> loadChoices(long featureId) {
    CompositeKey cacheKey = new CompositeKey("choices", featureId);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey, () -> {
      Feature feature = featureRepository.findById(featureId)
          .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", featureId));
      List<FeatureChoice> choices = feature.getChoices();
      choices.size();
      return choices.stream().map(FeatureCatalogService::toRef).toList();
    });
  }

  public Map<Long, List<FeatureEffectRef>> batchLoadEffects(Collection<Long> featureIds) {
    if (featureIds == null || featureIds.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Long, List<FeatureEffectRef>> result = new HashMap<>(featureIds.size());
    for (Long id : featureIds) {
      result.put(id, loadEffects(id));
    }
    return result;
  }

  public Map<Long, List<FeatureChoiceRef>> batchLoadChoices(Collection<Long> featureIds) {
    if (featureIds == null || featureIds.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Long, List<FeatureChoiceRef>> result = new HashMap<>(featureIds.size());
    for (Long id : featureIds) {
      result.put(id, loadChoices(id));
    }
    return result;
  }

  public void invalidateCache() {
    cacheManager.invalidate(CACHE_NAMESPACE);
  }

  private static FeatureRef toRef(Feature f) {
    return FeatureRef.builder()
        .id(f.getId())
        .key(f.getKey())
        .name(f.getName())
        .description(f.getDescription())
        .sourceType(f.getSourceType())
        .sourceKey(f.getSourceKey())
        .gainedAtLevel(f.getGainedAtLevel())
        .prerequisite(f.getPrerequisite())
        .displayOrder(f.getDisplayOrder())
        .build();
  }

  private static FeatureEffectRef toRef(FeatureEffect e) {
    return FeatureEffectRef.builder()
        .effectType(e.getEffectType())
        .payload(e.getPayload())
        .choiceKey(e.getChoiceKey())
        .build();
  }

  private static FeatureChoiceRef toRef(FeatureChoice c) {
    return FeatureChoiceRef.builder()
        .choiceKey(c.getChoiceKey())
        .name(c.getName())
        .description(c.getDescription())
        .chooseCount(c.getChooseCount())
        .optionsSource(c.getOptionsSource())
        .optionsFilter(c.getOptionsFilter())
        .build();
  }
}