package dev.ushki.livedndlist.service.features;

import dev.ushki.livedndlist.entity.dndCharacter.document.Document;
import dev.ushki.livedndlist.entity.dndCharacter.feature.Feature;
import dev.ushki.livedndlist.enums.AnnotationSource;
import dev.ushki.livedndlist.enums.FeatureSourceType;
import dev.ushki.livedndlist.repository.FeatureRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeatureUpsertHelper {

  private final FeatureRepository featureRepository;

  public void upsertNarrativeFeature(
      String featureKey,
      String name,
      String description,
      FeatureSourceType sourceType,
      String sourceKey,
      Integer gainedAtLevel,
      String prerequisite,
      Document document
  ) {
    Optional<Feature> existingOpt = featureRepository.findByKey(featureKey);

    if (existingOpt.isPresent()) {
      Feature existing = existingOpt.get();
      if (existing.getEffectsAnnotatedBy() == AnnotationSource.MANUAL
          || existing.getEffectsAnnotatedBy() == AnnotationSource.FILE_LOADER) {
        existing.setName(name);
        existing.setDescription(description);
        existing.setDocument(document);
        existing.setPrerequisite(prerequisite);
        existing.setGainedAtLevel(gainedAtLevel);
        featureRepository.save(existing);
        return;
      }
      existing.setName(name);
      existing.setDescription(description);
      existing.setDocument(document);
      existing.setPrerequisite(prerequisite);
      existing.setGainedAtLevel(gainedAtLevel);
      existing.setEffectsAnnotatedAt(OffsetDateTime.now());
      featureRepository.save(existing);
    } else {
      Feature newFeature = Feature.builder()
          .key(featureKey)
          .name(name)
          .description(description)
          .sourceType(sourceType)
          .sourceKey(sourceKey)
          .gainedAtLevel(gainedAtLevel)
          .prerequisite(prerequisite)
          .document(document)
          .effectsAnnotatedBy(AnnotationSource.OPEN5E_SYNC)
          .effectsAnnotatedAt(OffsetDateTime.now())
          .build();
      featureRepository.save(newFeature);
    }
  }

  public void deleteBySource(FeatureSourceType sourceType, String sourceKey) {
    featureRepository.findBySourceTypeAndSourceKey(sourceType, sourceKey).stream()
        .filter(f -> f.getEffectsAnnotatedBy() == AnnotationSource.OPEN5E_SYNC
            || f.getEffectsAnnotatedBy() == AnnotationSource.NONE)
        .forEach(featureRepository::delete);
  }
}
