package dev.ushki.livedndlist.service.features;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.entity.dndCharacter.DndFeat;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeatureChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureChoice;
import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.ChoiceOptionsSource;
import dev.ushki.livedndlist.enums.DndFeatType;
import dev.ushki.livedndlist.enums.SkillType;
import dev.ushki.livedndlist.repository.DndFeatRepository;
import dev.ushki.livedndlist.service.features.pipeline.ComputedCharacterState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChoiceValidator {

  private final DndFeatRepository featRepository;

  public void validate(
      FeatureChoice choiceDefinition,
      JsonNode selectedValues,
      CharacterFeature characterFeature,
      ComputedCharacterState state
  ) {
    if (!selectedValues.isArray()) {
      throw new IllegalArgumentException("selectedValues must be a JSON array");
    }

    // Detect if this is a 2024 ASI distribution choice
    JsonNode filter = choiceDefinition.getOptionsFilter();
    boolean isAsiDistribution =
        choiceDefinition.getOptionsSource() == ChoiceOptionsSource.ABILITY_LIST
            && filter != null && filter.has("distributions");

    // Dynamic size validation
    if (isAsiDistribution) {
      if (selectedValues.size() < 2 || selectedValues.size() > 3) {
        throw new IllegalArgumentException(
            "Expected 2 or 3 ASI distributions, got " + selectedValues.size());
      }
    } else {
      if (selectedValues.size() != choiceDefinition.getChooseCount()) {
        throw new IllegalArgumentException(
            "Expected " + choiceDefinition.getChooseCount() + " selections, got "
                + selectedValues.size());
      }
    }

    Set<String> validOptions = resolveValidOptions(choiceDefinition, characterFeature, state);

    Set<String> seen = new HashSet<>();
    List<String> invalid = new ArrayList<>();

    for (JsonNode value : selectedValues) {
      String val;
      if (isAsiDistribution && value.isObject()) {
        val = value.path("ability").asText();
      } else {
        val = value.isTextual() ? value.asText() : value.toString();
      }

      if (val == null || val.isBlank()) {
        throw new IllegalArgumentException("Selection values must not be blank");
      }

      if (!seen.add(val)) {
        throw new IllegalArgumentException("Duplicate selection: " + val);
      }

      if (!validOptions.isEmpty() && !validOptions.contains(val)) {
        invalid.add(val);
      }
    }

    if (!invalid.isEmpty()) {
      throw new IllegalArgumentException(
          "Invalid selections for choice '" + choiceDefinition.getChoiceKey() + "': " + invalid
              + ". Valid options: " + validOptions);
    }
  }

  private Set<String> resolveValidOptions(
      FeatureChoice choiceDefinition,
      CharacterFeature characterFeature,
      ComputedCharacterState state
  ) {
    ChoiceOptionsSource source = choiceDefinition.getOptionsSource();
    JsonNode filter = choiceDefinition.getOptionsFilter();

    Set<String> options = switch (source) {
      case INLINE -> resolveInlineOptions(filter);
      case SKILL_LIST -> resolveSkillListOptions(filter, state);
      case LANGUAGE_LIST -> resolveLanguageListOptions(filter);
      case TOOL_LIST -> resolveToolListOptions(filter);
      case WEAPON_LIST -> resolveWeaponListOptions(filter);
      case ARMOR_LIST -> resolveArmorListOptions(filter);
      case ABILITY_LIST -> resolveAbilityListOptions(filter);
      case FEAT_LIST -> resolveFeatListOptions(filter);
      case SPELL_LIST -> resolveSpellListOptions(filter);
    };

    if (filter.path("excludeChosen").asBoolean(false)) {
      Set<String> alreadyChosen = collectAlreadyChosenValues(
          characterFeature, choiceDefinition.getChoiceKey());
      options.removeAll(alreadyChosen);
    }

    return options;
  }

  private Set<String> resolveInlineOptions(JsonNode filter) {
    Set<String> options = new HashSet<>();
    JsonNode optionsArray = filter.path("options");
    if (optionsArray.isArray()) {
      for (JsonNode opt : optionsArray) {
        options.add(opt.asText());
      }
    }
    return options;
  }

  private Set<String> resolveSkillListOptions(JsonNode filter, ComputedCharacterState state) {
    Set<String> options = Arrays.stream(SkillType.values())
        .map(Enum::name)
        .collect(Collectors.toCollection(HashSet::new));

    getRetainedStrings(filter, options);

    if (filter.path("onlyProficient").asBoolean(false) && state != null) {
      options.retainAll(state.getSkillProficiencies());
    }

    return options;
  }

  private Set<String> resolveLanguageListOptions(JsonNode filter) {
    return getStrings(filter);
  }

  @NonNull
  private Set<String> getStrings(JsonNode filter) {
    if (filter.has("fromList") && filter.get("fromList").isArray()) {
      Set<String> options = new HashSet<>();
      for (JsonNode item : filter.get("fromList")) {
        options.add(item.asText());
      }
      return options;
    }
    return new HashSet<>();
  }

  private Set<String> resolveToolListOptions(JsonNode filter) {
    return getStrings(filter);
  }

  private Set<String> resolveWeaponListOptions(JsonNode filter) {
    return getStrings(filter);
  }

  private Set<String> resolveArmorListOptions(JsonNode filter) {
    Set<String> options = new HashSet<>(Set.of("LIGHT", "MEDIUM", "HEAVY", "SHIELD"));
    return getStrings(filter, options);
  }

  private Set<String> getStrings(JsonNode filter, Set<String> options) {
    getRetainedStrings(filter, options);
    return options;
  }

  private void getRetainedStrings(JsonNode filter, Set<String> options) {
    if (filter.has("fromList") && filter.get("fromList").isArray()) {
      Set<String> fromList = new HashSet<>();
      for (JsonNode item : filter.get("fromList")) {
        fromList.add(item.asText());
      }
      options.retainAll(fromList);
    }
  }

  private Set<String> resolveAbilityListOptions(JsonNode filter) {
    Set<String> options = Arrays.stream(AbilityType.values())
        .map(Enum::name)
        .collect(Collectors.toCollection(HashSet::new));
    return getStrings(filter, options);
  }

  private Set<String> resolveFeatListOptions(JsonNode filter) {
    String typeStr = filter.path("type").asText(null);
    if (typeStr != null) {
      try {
        DndFeatType type = DndFeatType.valueOf(typeStr);
        return featRepository.searchFeats(null, type, null,
                org.springframework.data.domain.Pageable.unpaged())
            .map(DndFeat::getKey)
            .toSet();
      } catch (IllegalArgumentException e) {
        log.warn("Unknown feat type filter: {}", typeStr);
      }
    }
    return featRepository.findAll().stream()
        .map(DndFeat::getKey)
        .collect(Collectors.toSet());
  }

  private Set<String> resolveSpellListOptions(JsonNode filter) {
    return new HashSet<>();
  }

  private Set<String> collectAlreadyChosenValues(
      CharacterFeature characterFeature,
      String excludeChoiceKey
  ) {
    Set<String> alreadyChosen = new HashSet<>();
    for (CharacterFeatureChoice cfc : characterFeature.getChoices()) {
      if (cfc.getChoiceKey().equals(excludeChoiceKey)) {
        continue;
      }
      JsonNode values = cfc.getSelectedValues();
      if (values != null && values.isArray()) {
        for (JsonNode v : values) {
          if (v.isTextual()) {
            alreadyChosen.add(v.asText());
          }
        }
      }
    }
    return alreadyChosen;
  }
}
