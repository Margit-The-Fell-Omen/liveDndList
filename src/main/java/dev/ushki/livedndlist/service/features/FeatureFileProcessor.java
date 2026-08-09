package dev.ushki.livedndlist.service.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.entity.dndCharacter.feature.Feature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureEffect;
import dev.ushki.livedndlist.enums.AnnotationSource;
import dev.ushki.livedndlist.enums.ChoiceOptionsSource;
import dev.ushki.livedndlist.enums.FeatureEffectType;
import dev.ushki.livedndlist.enums.FeatureSourceType;
import dev.ushki.livedndlist.repository.FeatureRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeatureFileProcessor {

  private final FeatureRepository featureRepository;
  private final EffectPayloadValidator effectPayloadValidator;
  private final ObjectMapper objectMapper;

  @Transactional
  public void processFile(Resource resource) throws IOException {
    try (InputStream is = resource.getInputStream()) {
      JsonNode root = objectMapper.readTree(is);

      String sourceTypeStr = root.path("sourceType").asText(null);
      String sourceKey = root.path("sourceKey").asText(null);

      if (sourceTypeStr == null || sourceKey == null) {
        log.warn("Skipping file {} — missing sourceType or sourceKey", resource.getFilename());
        return;
      }

      FeatureSourceType sourceType;
      try {
        sourceType = FeatureSourceType.valueOf(sourceTypeStr);
      } catch (IllegalArgumentException e) {
        log.warn("Skipping file {} — unknown sourceType: {}", resource.getFilename(),
            sourceTypeStr);
        return;
      }

      JsonNode featuresArray = root.path("features");
      if (!featuresArray.isArray()) {
        log.warn("Skipping file {} — no 'features' array", resource.getFilename());
        return;
      }

      for (JsonNode featureNode : featuresArray) {
        processFeatureNode(featureNode, sourceType, sourceKey, resource.getFilename());
      }
    }
  }

  private void processFeatureNode(JsonNode node, FeatureSourceType sourceType, String sourceKey,
      String filename) {
    String key = node.path("key").asText(null);
    if (key == null || key.isBlank()) {
      log.warn("Skipping feature in {} — missing key", filename);
      return;
    }

    Optional<Feature> existingOpt = featureRepository.findByKey(key);

    if (existingOpt.isPresent()) {
      Feature existing = existingOpt.get();
      if (existing.getEffectsAnnotatedBy() == AnnotationSource.MANUAL) {
        log.debug("Skipping feature {} — manually annotated, not overwriting", key);
        return;
      }
      updateFeature(existing, node, sourceType, sourceKey);
      featureRepository.save(existing);
      log.debug("Updated feature {} from file {}", key, filename);
    } else {
      Feature feature = createFeature(node, sourceType, sourceKey);
      featureRepository.save(feature);
      log.debug("Created feature {} from file {}", key, filename);
    }
  }

  private Feature createFeature(JsonNode node, FeatureSourceType sourceType, String sourceKey) {
    Feature feature = Feature.builder()
        .key(node.path("key").asText())
        .name(node.path("name").asText("Unnamed Feature"))
        .description(node.path("description").asText(null))
        .sourceType(sourceType)
        .sourceKey(sourceKey)
        .gainedAtLevel(node.has("gainedAtLevel") && !node.get("gainedAtLevel").isNull()
            ? node.get("gainedAtLevel").asInt() : null)
        .prerequisite(node.path("prerequisite").asText(null))
        .displayOrder(node.path("displayOrder").asInt(0))
        .effectsAnnotatedBy(AnnotationSource.FILE_LOADER)
        .effectsAnnotatedAt(OffsetDateTime.now())
        .build();

    addEffectsFromNode(feature, node);
    addChoicesFromNode(feature, node);

    return feature;
  }

  private void updateFeature(Feature feature, JsonNode node, FeatureSourceType sourceType,
      String sourceKey) {
    feature.setName(node.path("name").asText(feature.getName()));
    if (node.has("description")) {
      feature.setDescription(node.get("description").asText(null));
    }
    feature.setSourceType(sourceType);
    feature.setSourceKey(sourceKey);
    if (node.has("gainedAtLevel") && !node.get("gainedAtLevel").isNull()) {
      feature.setGainedAtLevel(node.get("gainedAtLevel").asInt());
    }
    if (node.has("prerequisite")) {
      feature.setPrerequisite(node.get("prerequisite").asText(null));
    }
    feature.setDisplayOrder(node.path("displayOrder").asInt(feature.getDisplayOrder()));
    feature.setEffectsAnnotatedBy(AnnotationSource.FILE_LOADER);
    feature.setEffectsAnnotatedAt(OffsetDateTime.now());

    feature.getEffects().clear();
    feature.getChoices().clear();

    featureRepository.saveAndFlush(feature);

    addEffectsFromNode(feature, node);
    addChoicesFromNode(feature, node);
  }

  private void addEffectsFromNode(Feature feature, JsonNode featureNode) {
    JsonNode effectsArray = featureNode.path("effects");
    if (!effectsArray.isArray()) {
      return;
    }

    for (JsonNode effectNode : effectsArray) {
      String typeStr = effectNode.path("type").asText(null);
      if (typeStr == null) {
        log.warn("Skipping effect on feature {} — missing type", feature.getKey());
        continue;
      }

      FeatureEffectType effectType;
      try {
        effectType = FeatureEffectType.valueOf(typeStr);
      } catch (IllegalArgumentException e) {
        log.warn("Skipping effect on feature {} — unknown type: {}", feature.getKey(), typeStr);
        continue;
      }

      JsonNode payload =
          effectNode.has("payload") ? effectNode.get("payload") : objectMapper.createObjectNode();
      String choiceKey = effectNode.path("choiceKey").asText(null);

      try {
        effectPayloadValidator.validate(effectType, payload, choiceKey);
      } catch (IllegalArgumentException e) {
        log.warn("Skipping invalid effect on feature {}: {}", feature.getKey(), e.getMessage());
        continue;
      }

      FeatureEffect effect = FeatureEffect.builder()
          .effectType(effectType)
          .payload(payload)
          .choiceKey(choiceKey)
          .displayOrder(effectNode.path("displayOrder").asInt(0))
          .build();

      feature.addEffect(effect);
    }
  }

  private void addChoicesFromNode(Feature feature, JsonNode featureNode) {
    JsonNode choicesArray = featureNode.path("choices");
    if (!choicesArray.isArray()) {
      return;
    }

    for (JsonNode choiceNode : choicesArray) {
      String choiceKey = choiceNode.path("choiceKey").asText(null);
      if (choiceKey == null || choiceKey.isBlank()) {
        log.warn("Skipping choice on feature {} — missing choiceKey", feature.getKey());
        continue;
      }

      ChoiceOptionsSource optionsSource;
      try {
        optionsSource = ChoiceOptionsSource.valueOf(
            choiceNode.path("optionsSource").asText("INLINE"));
      } catch (IllegalArgumentException e) {
        log.warn("Skipping choice {} on feature {} — unknown optionsSource", choiceKey,
            feature.getKey());
        continue;
      }

      FeatureChoice choice = FeatureChoice.builder()
          .choiceKey(choiceKey)
          .name(choiceNode.path("name").asText(choiceKey))
          .description(choiceNode.path("description").asText(null))
          .chooseCount(choiceNode.path("chooseCount").asInt(1))
          .optionsSource(optionsSource)
          .optionsFilter(choiceNode.has("optionsFilter")
              ? choiceNode.get("optionsFilter") : objectMapper.createObjectNode())
          .displayOrder(choiceNode.path("displayOrder").asInt(0))
          .build();

      feature.addChoice(choice);
    }
  }
}
