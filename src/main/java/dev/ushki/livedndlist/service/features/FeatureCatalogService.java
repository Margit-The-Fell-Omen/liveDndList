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

  public List<Feature> findBySource(FeatureSourceType type, String key) {
    if (type == null || key == null || key.isBlank()) {
      return List.of();
    }
    CompositeKey cacheKey = new CompositeKey("bySource", type, key);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey,
        () -> featureRepository.findBySourceTypeAndSourceKey(type, key));
  }

  public List<Feature> findClassFeaturesUpToLevel(String classKey, int level) {
    if (classKey == null || classKey.isBlank() || level < 1) {
      return List.of();
    }
    CompositeKey cacheKey = new CompositeKey("classUpToLevel", classKey, level);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey,
        () -> featureRepository.findBySourceUpToLevel(FeatureSourceType.CLASS, classKey, level));
  }

  public List<Feature> findSubclassFeaturesUpToLevel(String subclassKey, int level) {
    if (subclassKey == null || subclassKey.isBlank() || level < 1) {
      return List.of();
    }
    CompositeKey cacheKey = new CompositeKey("subclassUpToLevel", subclassKey, level);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey,
        () -> featureRepository.findBySourceUpToLevel(FeatureSourceType.SUBCLASS, subclassKey,
            level));
  }

  public Optional<Feature> findByKey(String featureKey) {
    if (featureKey == null || featureKey.isBlank()) {
      return Optional.empty();
    }
    CompositeKey cacheKey = new CompositeKey("byKey", featureKey);
    Feature cached = cacheManager.get(CACHE_NAMESPACE, cacheKey,
        () -> featureRepository.findByKey(featureKey).orElse(null));
    return Optional.ofNullable(cached);
  }

  public Feature getByKey(String featureKey) {
    return findByKey(featureKey)
        .orElseThrow(() -> new ResourceNotFoundException("Feature", "key", featureKey));
  }

  public List<FeatureEffect> loadEffects(long featureId) {
    CompositeKey cacheKey = new CompositeKey("effects", featureId);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey, () -> {
      Feature feature = featureRepository.findById(featureId)
          .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", featureId));
      List<FeatureEffect> effects = feature.getEffects();
      effects.size();
      return List.copyOf(effects);
    });
  }

  public List<FeatureChoice> loadChoices(long featureId) {
    CompositeKey cacheKey = new CompositeKey("choices", featureId);
    return cacheManager.get(CACHE_NAMESPACE, cacheKey, () -> {
      Feature feature = featureRepository.findById(featureId)
          .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", featureId));
      List<FeatureChoice> choices = feature.getChoices();
      choices.size();
      return List.copyOf(choices);
    });
  }

  public Map<Long, List<FeatureEffect>> batchLoadEffects(Collection<Long> featureIds) {
    if (featureIds == null || featureIds.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Long, List<FeatureEffect>> result = new HashMap<>(featureIds.size());
    for (Long id : featureIds) {
      result.put(id, loadEffects(id));
    }
    return result;
  }

  public Map<Long, List<FeatureChoice>> batchLoadChoices(Collection<Long> featureIds) {
    if (featureIds == null || featureIds.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Long, List<FeatureChoice>> result = new HashMap<>(featureIds.size());
    for (Long id : featureIds) {
      result.put(id, loadChoices(id));
    }
    return result;
  }

  public void invalidateCache() {
    cacheManager.invalidate(CACHE_NAMESPACE);
  }
}
