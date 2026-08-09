package dev.ushki.livedndlist.service.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeatureChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.Feature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureEffect;
import dev.ushki.livedndlist.repository.CharacterFeatureRepository;
import dev.ushki.livedndlist.service.features.pipeline.PendingChoice;
import dev.ushki.livedndlist.service.features.pipeline.ResolvedEffect;
import dev.ushki.livedndlist.service.features.pipeline.ResolvedEffects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FeatureEffectResolver {

  private final CharacterFeatureRepository characterFeatureRepository;
  private final FeatureCatalogService featureCatalogService;
  private final ObjectMapper objectMapper;

  private static final int MAX_PASSES = 3;

  public ResolvedEffects resolve(long characterId) {
    List<CharacterFeature> characterFeatures = characterFeatureRepository.findByCharacterId(
        characterId);
    List<ResolvedEffect> allEffects = new ArrayList<>();
    List<PendingChoice> allPending = new ArrayList<>();

    Set<Long> processedFeatureIds = characterFeatures.stream()
        .map(cf -> cf.getFeature().getId())
        .collect(Collectors.toSet());

    resolvePass(characterFeatures, allEffects, allPending);

    for (int pass = 1; pass < MAX_PASSES; pass++) {
      List<Long> grantedFeatureIds = extractGrantedFeatureIds(allEffects);
      List<Long> newFeatureIds = grantedFeatureIds.stream()
          .filter(id -> !processedFeatureIds.contains(id))
          .toList();

      if (newFeatureIds.isEmpty()) {
        break;
      }

      processedFeatureIds.addAll(newFeatureIds);

      List<CharacterFeature> syntheticFeatures = newFeatureIds.stream()
          .map(id -> {
            return findExistingOrSynthetic(characterFeatures, id);
          })
          .toList();

      resolvePass(syntheticFeatures, allEffects, allPending);
    }

    return ResolvedEffects.builder()
        .effects(allEffects)
        .pendingChoices(allPending)
        .build();
  }

  private void resolvePass(
      List<CharacterFeature> characterFeatures,
      List<ResolvedEffect> effects,
      List<PendingChoice> pending) {

    Collection<Long> featureIds = characterFeatures.stream()
        .map(cf -> cf.getFeature().getId())
        .collect(Collectors.toSet());

    Map<Long, List<FeatureEffect>> effectsByFeature = featureCatalogService.batchLoadEffects(
        featureIds);
    Map<Long, List<FeatureChoice>> choicesByFeature = featureCatalogService.batchLoadChoices(
        featureIds);

    for (CharacterFeature cf : characterFeatures) {
      if (!cf.getActive()) {
        continue;
      }

      Feature feature = cf.getFeature();
      Long featureId = feature.getId();

      Map<String, CharacterFeatureChoice> answeredChoices = new HashMap<>();
      for (CharacterFeatureChoice cfc : cf.getChoices()) {
        answeredChoices.put(cfc.getChoiceKey(), cfc);
      }

      List<FeatureEffect> featureEffects = effectsByFeature.getOrDefault(featureId, List.of());
      List<FeatureChoice> featureChoices = choicesByFeature.getOrDefault(featureId, List.of());

      Map<String, FeatureChoice> choiceMap = new HashMap<>();
      for (FeatureChoice fc : featureChoices) {
        choiceMap.put(fc.getChoiceKey(), fc);
      }

      for (FeatureEffect effect : featureEffects) {
        if (effect.getChoiceKey() != null && !effect.getChoiceKey().isBlank()) {
          CharacterFeatureChoice answer = answeredChoices.get(effect.getChoiceKey());
          if (answer == null) {
            FeatureChoice choiceDefinition = choiceMap.get(effect.getChoiceKey());
            if (choiceDefinition != null && !alreadyPending(pending, cf.getId(),
                effect.getChoiceKey())) {
              pending.add(PendingChoice.builder()
                  .characterFeatureId(cf.getId())
                  .choiceKey(effect.getChoiceKey())
                  .name(choiceDefinition.getName())
                  .description(choiceDefinition.getDescription())
                  .chooseCount(choiceDefinition.getChooseCount())
                  .optionsSource(choiceDefinition.getOptionsSource())
                  .optionsFilter(choiceDefinition.getOptionsFilter())
                  .currentSelection(null)
                  .build());
            }
            continue;
          }
          expandChoiceEffects(cf, effect, answer, effects);
        } else {
          effects.add(ResolvedEffect.builder()
              .type(effect.getEffectType())
              .payload(effect.getPayload())
              .sourceCharacterFeatureId(cf.getId())
              .sourceFeatureId(featureId)
              .source(cf.getSource())
              .sourceContext(cf.getSourceContext())
              .build());
        }
      }
    }
  }

  private void expandChoiceEffects(
      CharacterFeature cf,
      FeatureEffect effect,
      CharacterFeatureChoice answer,
      List<ResolvedEffect> effects) {

    JsonNode selectedValues = answer.getSelectedValues();

    if (selectedValues.isArray()) {
      for (JsonNode value : selectedValues) {
        ObjectNode expandedPayload = objectMapper.createObjectNode();
        expandedPayload.setAll((ObjectNode) effect.getPayload().deepCopy());

        if (value.isTextual()) {
          String fieldName = guessPayloadField(effect.getEffectType());
          if (fieldName != null) {
            expandedPayload.put(fieldName, value.asText());
          }
        } else if (value.isObject()) {
          expandedPayload.setAll((ObjectNode) value);
        }

        effects.add(ResolvedEffect.builder()
            .type(effect.getEffectType())
            .payload(expandedPayload)
            .sourceCharacterFeatureId(cf.getId())
            .sourceFeatureId(cf.getFeature().getId())
            .source(cf.getSource())
            .sourceContext(cf.getSourceContext())
            .build());
      }
    }
  }

  private String guessPayloadField(dev.ushki.livedndlist.enums.FeatureEffectType type) {
    return switch (type) {
      case GRANT_SKILL_PROFICIENCY, GRANT_SKILL_EXPERTISE -> "skill";
      case GRANT_SAVING_THROW_PROFICIENCY -> "ability";
      case GRANT_LANGUAGE -> "languageKey";
      case GRANT_TOOL_PROFICIENCY -> "toolKey";
      case GRANT_ARMOR_PROFICIENCY -> "category";
      case GRANT_WEAPON_PROFICIENCY -> "weaponKey";
      default -> null;
    };
  }

  private List<Long> extractGrantedFeatureIds(List<ResolvedEffect> effects) {
    return effects.stream()
        .filter(e -> e.getType() == dev.ushki.livedndlist.enums.FeatureEffectType.GRANT_FEAT
            || e.getType() == dev.ushki.livedndlist.enums.FeatureEffectType.GRANT_FIGHTING_STYLE)
        .map(e -> {
          String featKey = e.getPayload().path("featKey").asText(null);
          if (featKey != null) {
            return featureCatalogService.findByKey(featKey)
                .map(Feature::getId)
                .orElse(null);
          }
          return null;
        })
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  private CharacterFeature findExistingOrSynthetic(List<CharacterFeature> existing,
      Long featureId) {
    return existing.stream()
        .filter(cf -> cf.getFeature().getId().equals(featureId))
        .findFirst()
        .orElseGet(() -> {
          Feature feature = featureCatalogService.getByKey(
              featureCatalogService.findByKey(String.valueOf(featureId))
                  .map(Feature::getKey)
                  .orElse(""));
          return CharacterFeature.builder()
              .id(0L)
              .feature(feature)
              .source(dev.ushki.livedndlist.enums.CharacterFeatureSource.FEAT)
              .active(true)
              .build();
        });
  }

  private boolean alreadyPending(List<PendingChoice> pending, long cfId, String choiceKey) {
    return pending.stream()
        .anyMatch(p -> p.getCharacterFeatureId() == cfId && p.getChoiceKey().equals(choiceKey));
  }
}
