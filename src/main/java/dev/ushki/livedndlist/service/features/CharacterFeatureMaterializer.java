package dev.ushki.livedndlist.service.features;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndCharacterClassLevel;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeat;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterSubclassChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.Feature;
import dev.ushki.livedndlist.enums.CharacterFeatureSource;
import dev.ushki.livedndlist.enums.FeatureSourceType;
import dev.ushki.livedndlist.repository.CharacterFeatureRepository;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.CharacterSubclassChoiceRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterFeatureMaterializer {

  private final CharacterRepository characterRepository;
  private final CharacterFeatureRepository characterFeatureRepository;
  private final CharacterSubclassChoiceRepository subclassChoiceRepository;
  private final FeatureCatalogService featureCatalogService;

  private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

  @Transactional
  public void syncFeatures(long characterId) {
    DndCharacter character = characterRepository.findById(characterId)
        .orElseThrow(() -> new dev.ushki.livedndlist.exceptions.ResourceNotFoundException(
            "Character", "id", characterId));

    List<TargetFeature> target = computeTarget(character);
    List<CharacterFeature> existing = characterFeatureRepository.findByCharacterId(characterId);

    Map<String, CharacterFeature> existingMap = new HashMap<>();
    for (CharacterFeature cf : existing) {
      existingMap.put(reconciliationKey(cf), cf);
    }

    Map<String, TargetFeature> targetMap = new HashMap<>();
    for (TargetFeature tf : target) {
      targetMap.put(tf.reconciliationKey(), tf);
    }

    List<CharacterFeature> toDelete = existing.stream()
        .filter(cf -> !targetMap.containsKey(reconciliationKey(cf)))
        .toList();

    List<TargetFeature> toInsert = target.stream()
        .filter(tf -> !existingMap.containsKey(tf.reconciliationKey()))
        .toList();

    characterFeatureRepository.deleteAll(toDelete);

    int order = 0;
    for (TargetFeature tf : target) {
      String key = tf.reconciliationKey();
      CharacterFeature existingCf = existingMap.get(key);
      if (existingCf != null) {
        existingCf.setDisplayOrder(order++);
        characterFeatureRepository.save(existingCf);
      } else {
        CharacterFeature newCf = CharacterFeature.builder()
            .character(character)
            .feature(tf.feature)
            .source(tf.source)
            .sourceContext(tf.sourceContext)
            .active(true)
            .displayOrder(order++)
            .build();
        characterFeatureRepository.save(newCf);
      }
    }

    log.info("Synced features for character {}: deleted={}, inserted={}, total={}",
        characterId, toDelete.size(), toInsert.size(), target.size());
  }

  private List<TargetFeature> computeTarget(DndCharacter character) {
    List<TargetFeature> target = new ArrayList<>();

    String raceKey = character.getRace() != null ? character.getRace().getKey() : null;
    if (raceKey != null) {
      for (Feature f : featureCatalogService.findBySource(FeatureSourceType.RACE, raceKey)) {
        target.add(new TargetFeature(f, CharacterFeatureSource.RACE, emptyContext()));
      }
    }

    String backgroundKey =
        character.getBackground() != null ? character.getBackground().getKey() : null;
    if (backgroundKey != null) {
      for (Feature f : featureCatalogService.findBySource(FeatureSourceType.BACKGROUND,
          backgroundKey)) {
        target.add(new TargetFeature(f, CharacterFeatureSource.BACKGROUND, emptyContext()));
      }
    }

    List<CharacterSubclassChoice> subclassChoices = subclassChoiceRepository.findByCharacterId(
        character.getId());
    Map<String, String> subclassMap = subclassChoices.stream()
        .collect(Collectors.toMap(CharacterSubclassChoice::getClassKey,
            CharacterSubclassChoice::getSubclassKey));

    for (DndCharacterClassLevel classLevel : character.getClasses()) {
      String classKey = classLevel.getDndClass().getKey();
      int level = classLevel.getLevel();

      ObjectNode classContext = JSON.objectNode();
      classContext.put("classKey", classKey);

      for (Feature f : featureCatalogService.findClassFeaturesUpToLevel(classKey, level)) {
        ObjectNode ctx = classContext.deepCopy();
        ctx.put("classLevel", f.getGainedAtLevel());
        target.add(new TargetFeature(f, CharacterFeatureSource.CLASS, ctx));
      }

      String subclassKey = subclassMap.get(classKey);
      if (subclassKey != null) {
        ObjectNode subContext = classContext.deepCopy();
        subContext.put("subclassKey", subclassKey);

        for (Feature f : featureCatalogService.findSubclassFeaturesUpToLevel(subclassKey, level)) {
          ObjectNode ctx = subContext.deepCopy();
          ctx.put("classLevel", f.getGainedAtLevel());
          target.add(new TargetFeature(f, CharacterFeatureSource.SUBCLASS, ctx));
        }
      }
    }

    if (character.getFeats() != null) {
      for (CharacterFeat cf : character.getFeats()) {
        String featKey = cf.getFeat().getKey();
        for (Feature f : featureCatalogService.findBySource(FeatureSourceType.FEAT, featKey)) {
          ObjectNode ctx = JSON.objectNode();
          ctx.put("featKey", featKey);
          target.add(new TargetFeature(f, CharacterFeatureSource.FEAT, ctx));
        }
      }
    }

    return target;
  }

  private ObjectNode emptyContext() {
    return JSON.objectNode();
  }

  private String reconciliationKey(CharacterFeature cf) {
    String classKey = cf.getSourceContext().path("classKey").asText("");
    String subclassKey = cf.getSourceContext().path("subclassKey").asText("");
    String featKey = cf.getSourceContext().path("featKey").asText("");
    return cf.getFeature().getId() + "|" + cf.getSource() + "|" + classKey + "|" + subclassKey + "|"
        + featKey;
  }

  private record TargetFeature(Feature feature, CharacterFeatureSource source,
                               ObjectNode sourceContext) {

    String reconciliationKey() {
      String classKey = sourceContext.path("classKey").asText("");
      String subclassKey = sourceContext.path("subclassKey").asText("");
      String featKey = sourceContext.path("featKey").asText("");
      return feature.getId() + "|" + source + "|" + classKey + "|" + subclassKey + "|" + featKey;
    }
  }
}
