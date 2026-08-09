package dev.ushki.livedndlist.service.features;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeatureChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureEffect;
import dev.ushki.livedndlist.enums.FeatureEffectType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.CharacterFeatureChoiceRepository;
import dev.ushki.livedndlist.repository.CharacterFeatureRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CharacterChoiceService {

  private static final Set<FeatureEffectType> FEATURE_GRANTING_TYPES = Set.of(
      FeatureEffectType.GRANT_FEAT,
      FeatureEffectType.GRANT_FIGHTING_STYLE
  );

  private final CharacterFeatureRepository characterFeatureRepository;
  private final CharacterFeatureChoiceRepository choiceRepository;
  private final FeatureCatalogService featureCatalogService;
  private final CharacterFeatService characterFeatService;
  private final CharacterFeatureMaterializer materializer;

  public void submitChoice(long characterId, long characterFeatureId, String choiceKey,
      JsonNode selectedValues) {
    CharacterFeature cf = characterFeatureRepository.findById(characterFeatureId)
        .orElseThrow(
            () -> new ResourceNotFoundException("CharacterFeature", "id", characterFeatureId));

    if (!cf.getCharacter().getId().equals(characterId)) {
      throw new IllegalArgumentException(
          "CharacterFeature does not belong to character " + characterId);
    }

    List<FeatureChoice> featureChoices = featureCatalogService.loadChoices(cf.getFeature().getId());
    FeatureChoice choiceDefinition = featureChoices.stream()
        .filter(fc -> fc.getChoiceKey().equals(choiceKey))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "No choice with key '" + choiceKey + "' on feature " + cf.getFeature().getKey()));

    if (!selectedValues.isArray()) {
      throw new IllegalArgumentException("selectedValues must be a JSON array");
    }

    if (selectedValues.size() != choiceDefinition.getChooseCount()) {
      throw new IllegalArgumentException(
          "Expected " + choiceDefinition.getChooseCount() + " selections, got "
              + selectedValues.size());
    }

    Optional<CharacterFeatureChoice> existingChoice =
        choiceRepository.findByCharacterFeatureIdAndChoiceKey(characterFeatureId, choiceKey);

    if (existingChoice.isPresent()) {
      CharacterFeatureChoice choice = existingChoice.get();
      choice.setSelectedValues(selectedValues);
      choiceRepository.save(choice);
    } else {
      CharacterFeatureChoice choice = CharacterFeatureChoice.builder()
          .characterFeature(cf)
          .choiceKey(choiceKey)
          .selectedValues(selectedValues)
          .build();
      cf.addChoice(choice);
      choiceRepository.save(choice);
    }

    handleSideEffects(characterId, cf, choiceKey, selectedValues);

    log.info("Submitted choice {} for character feature {} on character {}",
        choiceKey, characterFeatureId, characterId);
  }

  public void clearChoice(long characterId, long characterFeatureId, String choiceKey) {
    CharacterFeature cf = characterFeatureRepository.findById(characterFeatureId)
        .orElseThrow(
            () -> new ResourceNotFoundException("CharacterFeature", "id", characterFeatureId));

    if (!cf.getCharacter().getId().equals(characterId)) {
      throw new IllegalArgumentException(
          "CharacterFeature does not belong to character " + characterId);
    }

    choiceRepository.deleteByCharacterFeatureIdAndChoiceKey(characterFeatureId, choiceKey);
    materializer.syncFeatures(characterId);
    log.info("Cleared choice {} for character feature {} on character {}",
        choiceKey, characterFeatureId, characterId);
  }

  private void handleSideEffects(long characterId, CharacterFeature cf, String choiceKey,
      JsonNode selectedValues) {
    List<FeatureEffect> effects = featureCatalogService.loadEffects(cf.getFeature().getId());

    boolean needsRematerialization = false;

    for (FeatureEffect effect : effects) {
      if (choiceKey.equals(effect.getChoiceKey()) && FEATURE_GRANTING_TYPES.contains(
          effect.getEffectType())) {
        if (effect.getEffectType() == FeatureEffectType.GRANT_FEAT) {
          for (JsonNode value : selectedValues) {
            String featKey =
                value.isTextual() ? value.asText() : value.path("featKey").asText(null);
            if (featKey != null) {
              characterFeatService.addFeat(characterId, featKey, null);
              needsRematerialization = true;
            }
          }
        }

        if (effect.getEffectType() == FeatureEffectType.GRANT_FIGHTING_STYLE) {
          needsRematerialization = true;
        }
      }
    }

    if (needsRematerialization) {
      materializer.syncFeatures(characterId);
    }
  }
}
