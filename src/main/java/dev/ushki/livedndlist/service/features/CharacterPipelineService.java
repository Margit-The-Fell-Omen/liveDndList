package dev.ushki.livedndlist.service.features;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.Equipment;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndCharacterClassLevel;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterCustomFeature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeatureChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterResource;
import dev.ushki.livedndlist.enums.ArmorCategory;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.enums.FeatureEffectType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.CharacterFeatureRepository;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.CharacterResourceRepository;
import dev.ushki.livedndlist.service.features.pipeline.ComputedCharacterState;
import dev.ushki.livedndlist.service.features.pipeline.ResolvedEffect;
import dev.ushki.livedndlist.service.features.pipeline.ResolvedEffects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CharacterPipelineService {

  private static final String[] ABILITIES = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
  private static final String[] ABILITY_FULL = {"STRENGTH", "DEXTERITY", "CONSTITUTION",
      "INTELLIGENCE", "WISDOM", "CHARISMA"};

  private final CharacterRepository characterRepository;
  private final CharacterFeatureMaterializer materializer;
  private final FeatureEffectResolver resolver;
  private final CharacterFeatureRepository characterFeatureRepository;
  private final CharacterCustomFeatureService customFeatureService;
  private final CharacterResourceRepository resourceRepository;

  public ComputedCharacterState compute(long characterId) {
    DndCharacter character = characterRepository.findById(characterId)
        .orElseThrow(() -> new ResourceNotFoundException("Character", "id", characterId));

    ResolvedEffects resolved = resolver.resolve(characterId);
    List<ResolvedEffect> effects = resolved.getEffects();

    ComputedCharacterState state = ComputedCharacterState.builder().build();

    computeLevelAndProfBonus(character, state);
    computeAbilityScores(character, effects, state);
    computeSize(effects, state);
    computeSpeed(character, effects, state);
    computeSenses(effects, state);
    computeResistancesAndImmunities(effects, state);
    computeProficiencies(effects, state);
    computeHitDice(character, effects, state);
    computeArmorClass(character, effects, state);
    computeInitiative(effects, state);
    computeSkillAndSaveTotals(state);
    computeResources(character, effects, state);
    computeActions(effects, state);
    computeAttackModifiers(effects, state);
    assembleFeatureDisplayList(characterId, resolved, state);

    return state;
  }

  private void computeLevelAndProfBonus(DndCharacter character, ComputedCharacterState state) {
    int totalLevel = character.getClasses().stream()
        .mapToInt(DndCharacterClassLevel::getLevel)
        .sum();
    state.setTotalLevel(Math.max(1, totalLevel));
    state.setProficiencyBonus(profBonusForLevel(state.getTotalLevel()));
  }

  private void computeAbilityScores(DndCharacter character, List<ResolvedEffect> effects,
      ComputedCharacterState state) {
    Map<String, Integer> scores = new HashMap<>();
    var abilityScores = character.getAbilityScores();
    scores.put("STR", abilityScores.getStrength());
    scores.put("DEX", abilityScores.getDexterity());
    scores.put("CON", abilityScores.getConstitution());
    scores.put("INT", abilityScores.getIntelligence());
    scores.put("WIS", abilityScores.getWisdom());
    scores.put("CHA", abilityScores.getCharisma());

    Map<String, Integer> caps = new HashMap<>();
    for (String ab : ABILITIES) {
      caps.put(ab, 20);
    }

    for (ResolvedEffect e : effects) {
      if (e.getType() == FeatureEffectType.MODIFY_ABILITY_SCORE) {
        String ability = e.getPayload().path("ability").asText();
        int amount = e.getPayload().path("amount").asInt(0);
        int max = e.getPayload().has("max") && !e.getPayload().get("max").isNull()
            ? e.getPayload().get("max").asInt() : caps.getOrDefault(ability, 20);
        caps.put(ability, Math.max(caps.getOrDefault(ability, 20), max));
        scores.merge(ability, amount, Integer::sum);
      }
      if (e.getType() == FeatureEffectType.SET_ABILITY_SCORE_MINIMUM) {
        String ability = e.getPayload().path("ability").asText();
        int value = e.getPayload().path("value").asInt(0);
        scores.merge(ability, 0, (current, ignored) -> Math.max(current, value));
      }
    }

    for (String ab : ABILITIES) {
      int capped = Math.min(scores.getOrDefault(ab, 10), caps.getOrDefault(ab, 20));
      scores.put(ab, capped);
    }

    state.setFinalAbilityScores(scores);

    Map<String, Integer> mods = new HashMap<>();
    for (String ab : ABILITIES) {
      mods.put(ab, Math.floorDiv(scores.getOrDefault(ab, 10) - 10, 2));
    }
    state.setAbilityModifiers(mods);
  }

  private void computeSize(List<ResolvedEffect> effects, ComputedCharacterState state) {
    for (ResolvedEffect e : effects) {
      if (e.getType() == FeatureEffectType.SET_CREATURE_SIZE) {
        try {
          state.setSize(dev.ushki.livedndlist.enums.CreatureSize.valueOf(
              e.getPayload().path("size").asText("MEDIUM")));
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
  }

  private void computeSpeed(DndCharacter character, List<ResolvedEffect> effects,
      ComputedCharacterState state) {
    int baseWalk = character.getBaseWalkingSpeedOverride() != null
        ? character.getBaseWalkingSpeedOverride() : 30;

    int walkBonus = 0;
    Map<String, Integer> otherSpeeds = new HashMap<>();

    for (ResolvedEffect e : effects) {
      if (e.getType() == FeatureEffectType.MODIFY_SPEED) {
        String speedType = e.getPayload().path("speedType").asText("WALK");
        int amount = e.getPayload().path("amount").asInt(0);
        if ("WALK".equals(speedType)) {
          walkBonus += amount;
        } else {
          otherSpeeds.merge(speedType, amount, Integer::sum);
        }
      }
      if (e.getType() == FeatureEffectType.GRANT_SPEED_TYPE) {
        String speedType = e.getPayload().path("speedType").asText();
        boolean matchWalking = e.getPayload().path("matchWalking").asBoolean(false);
        int value = e.getPayload().path("value").asInt(0);
        if (matchWalking) {
          otherSpeeds.put(speedType, baseWalk + walkBonus);
        } else {
          otherSpeeds.merge(speedType, value, Math::max);
        }
      }
    }

    Map<String, Integer> speeds = new HashMap<>();
    speeds.put("WALK", baseWalk + walkBonus);
    speeds.putAll(otherSpeeds);
    state.setSpeeds(speeds);
  }

  private void computeSenses(List<ResolvedEffect> effects, ComputedCharacterState state) {
    Map<String, Integer> senseMap = new HashMap<>();
    for (ResolvedEffect e : effects) {
      if (e.getType() == FeatureEffectType.GRANT_SENSE) {
        String senseType = e.getPayload().path("senseType").asText();
        int range = e.getPayload().path("range").asInt(0);
        senseMap.merge(senseType, range, Math::max);
      }
    }
    List<ComputedCharacterState.SenseEntry> senses = new ArrayList<>();
    senseMap.forEach((type, range) ->
        senses.add(
            ComputedCharacterState.SenseEntry.builder().senseType(type).range(range).build()));
    state.setSenses(senses);
  }

  private void computeResistancesAndImmunities(List<ResolvedEffect> effects,
      ComputedCharacterState state) {
    Set<String> resistances = new HashSet<>();
    Set<String> immunities = new HashSet<>();
    Set<String> vulnerabilities = new HashSet<>();
    Set<String> conditionImmunities = new HashSet<>();

    for (ResolvedEffect e : effects) {
      switch (e.getType()) {
        case GRANT_DAMAGE_RESISTANCE -> resistances.add(e.getPayload().path("damageType").asText());
        case GRANT_DAMAGE_IMMUNITY -> immunities.add(e.getPayload().path("damageType").asText());
        case GRANT_DAMAGE_VULNERABILITY ->
            vulnerabilities.add(e.getPayload().path("damageType").asText());
        case GRANT_CONDITION_IMMUNITY ->
            conditionImmunities.add(e.getPayload().path("condition").asText());
        default -> {
        }
      }
    }

    state.setDamageResistances(resistances);
    state.setDamageImmunities(immunities);
    state.setDamageVulnerabilities(vulnerabilities);
    state.setConditionImmunities(conditionImmunities);
  }

  private void computeProficiencies(List<ResolvedEffect> effects, ComputedCharacterState state) {
    Set<String> armor = new HashSet<>();
    Set<String> weapons = new HashSet<>();
    Set<String> tools = new HashSet<>();
    Set<String> languages = new HashSet<>();
    Set<String> saves = new HashSet<>();
    Set<String> skills = new HashSet<>();
    Set<String> expertise = new HashSet<>();

    for (ResolvedEffect e : effects) {
      switch (e.getType()) {
        case GRANT_ARMOR_PROFICIENCY -> armor.add(e.getPayload().path("category").asText());
        case GRANT_WEAPON_PROFICIENCY -> {
          String scope = e.getPayload().path("scope").asText("CATEGORY");
          if ("CATEGORY".equals(scope)) {
            weapons.add(e.getPayload().path("category").asText());
          } else {
            weapons.add(e.getPayload().path("weaponKey").asText());
          }
        }
        case GRANT_TOOL_PROFICIENCY -> {
          String toolKey = e.getPayload().path("toolKey").asText(null);
          if (toolKey != null) {
            tools.add(toolKey);
          }
        }
        case GRANT_LANGUAGE -> {
          String langKey = e.getPayload().path("languageKey").asText(null);
          if (langKey != null) {
            languages.add(langKey);
          }
        }
        case GRANT_SAVING_THROW_PROFICIENCY -> saves.add(e.getPayload().path("ability").asText());
        case GRANT_SKILL_PROFICIENCY -> {
          String skill = e.getPayload().path("skill").asText(null);
          if (skill != null) {
            skills.add(skill);
          }
        }
        case GRANT_SKILL_EXPERTISE -> {
          String skill = e.getPayload().path("skill").asText(null);
          if (skill != null) {
            expertise.add(skill);
          }
        }
        default -> {
        }
      }
    }

    state.setArmorProficiencies(armor);
    state.setWeaponProficiencies(weapons);
    state.setToolProficiencies(tools);
    state.setLanguages(languages);
    state.setSavingThrowProficiencies(saves);
    state.setSkillProficiencies(skills);
    state.setSkillExpertise(expertise);
  }

  private void computeHitDice(DndCharacter character, List<ResolvedEffect> effects,
      ComputedCharacterState state) {
    Map<String, ComputedCharacterState.HitDiceEntry> hitDice = new HashMap<>();
    int hpPerLevel = 0;
    int hpFlat = 0;

    for (ResolvedEffect e : effects) {
      if (e.getType() == FeatureEffectType.SET_HIT_DIE) {
        String die = e.getPayload().path("die").asText();
        String classKey = e.getSourceContext().path("classKey").asText("unknown");
        int classLevel = character.getClasses().stream()
            .filter(cl -> cl.getDndClass().getKey().equals(classKey))
            .mapToInt(DndCharacterClassLevel::getLevel)
            .findFirst().orElse(1);
        hitDice.put(die + "_" + classKey,
            ComputedCharacterState.HitDiceEntry.builder().die(die).count(classLevel).build());
      }
      if (e.getType() == FeatureEffectType.MODIFY_HIT_POINTS_PER_LEVEL) {
        hpPerLevel += e.getPayload().path("amount").asInt(0);
      }
      if (e.getType() == FeatureEffectType.MODIFY_MAX_HIT_POINTS) {
        hpFlat += e.getPayload().path("amount").asInt(0);
      }
    }

    state.setHitDice(hitDice);
    state.setHpPerLevelModifier(hpPerLevel);
    state.setHpFlatModifier(hpFlat);
  }

  private void computeArmorClass(DndCharacter character, List<ResolvedEffect> effects,
      ComputedCharacterState state) {
    int dexMod = state.getAbilityModifiers().getOrDefault("DEX", 0);

    Equipment equippedArmor = character.getEquipment() == null ? null :
        character.getEquipment().stream()
            .filter(e -> e.getType() == EquipmentType.ARMOR
                && e.getArmorCategory() != null
                && e.getArmorCategory() != ArmorCategory.SHIELD
                && e.isEquipped())
            .findFirst()
            .orElse(null);

    int shieldBonus = character.getEquipment() == null ? 0 :
        character.getEquipment().stream()
            .anyMatch(e -> e.getType() == EquipmentType.ARMOR
                && e.getArmorCategory() == ArmorCategory.SHIELD
                && e.isEquipped()) ? 2 : 0;

    int armorBaseAC;
    if (equippedArmor == null) {
      armorBaseAC = 10 + dexMod;
    } else {
      int armorAC = equippedArmor.getArmorClass() != null ? equippedArmor.getArmorClass() : 10;
      armorBaseAC = switch (equippedArmor.getArmorCategory()) {
        case LIGHT -> armorAC + dexMod;
        case MEDIUM -> armorAC + Math.min(dexMod, 2);
        case HEAVY -> armorAC;
        case SHIELD -> armorAC;
      };
    }

    int formulaBestAC = armorBaseAC;
    int flatBonus = 0;

    for (ResolvedEffect e : effects) {
      if (e.getType() != FeatureEffectType.MODIFY_ARMOR_CLASS) {
        continue;
      }
      String mode = e.getPayload().path("mode").asText("BONUS");

      if ("BONUS".equals(mode)) {
        flatBonus += e.getPayload().path("amount").asInt(0);
        continue;
      }

      if ("FORMULA".equals(mode)) {
        JsonNode formula = e.getPayload().path("formula");
        boolean requiresNoArmor = formula.path("requiresNoArmor").asBoolean(false);
        boolean requiresNoShield = formula.path("requiresNoShield").asBoolean(false);

        if (requiresNoArmor && equippedArmor != null) {
          continue;
        }
        if (requiresNoShield && shieldBonus > 0) {
          continue;
        }

        int formulaTotal = formula.path("base").asInt(10);
        JsonNode addArray = formula.path("add");
        if (addArray.isArray()) {
          for (JsonNode add : addArray) {
            if ("ABILITY_MOD".equals(add.path("type").asText())) {
              String ability = add.path("ability").asText("DEX");
              int mod = state.getAbilityModifiers().getOrDefault(ability, 0);
              int cap = add.has("cap") && !add.get("cap").isNull()
                  ? add.get("cap").asInt() : Integer.MAX_VALUE;
              formulaTotal += Math.min(mod, cap);
            }
          }
        }

        if (formulaTotal > formulaBestAC) {
          formulaBestAC = formulaTotal;
        }
      }
    }

    int armorClassBonus =
        character.getArmorClassBonus() != null ? character.getArmorClassBonus() : 0;
    state.setArmorClass(Math.max(1, formulaBestAC + shieldBonus + flatBonus + armorClassBonus));
  }

  private void computeInitiative(List<ResolvedEffect> effects, ComputedCharacterState state) {
    int dexMod = state.getAbilityModifiers().getOrDefault("DEX", 0);
    int bonus = 0;
    for (ResolvedEffect e : effects) {
      if (e.getType() == FeatureEffectType.MODIFY_INITIATIVE) {
        bonus += e.getPayload().path("amount").asInt(0);
      }
    }
    state.setInitiative(dexMod + bonus);
  }

  private void computeSkillAndSaveTotals(ComputedCharacterState state) {
    String[][] skillAbilityMap = {
        {"ACROBATICS", "DEX"}, {"ANIMAL_HANDLING", "WIS"}, {"ARCANA", "INT"}, {"ATHLETICS", "STR"},
        {"DECEPTION", "CHA"}, {"HISTORY", "INT"}, {"INSIGHT", "WIS"}, {"INTIMIDATION", "CHA"},
        {"INVESTIGATION", "INT"}, {"MEDICINE", "WIS"}, {"NATURE", "INT"}, {"PERCEPTION", "WIS"},
        {"PERFORMANCE", "CHA"}, {"PERSUASION", "CHA"}, {"RELIGION", "INT"},
        {"SLEIGHT_OF_HAND", "DEX"}, {"STEALTH", "DEX"}, {"SURVIVAL", "WIS"},
    };

    Map<String, Integer> skillTotals = new HashMap<>();
    for (String[] pair : skillAbilityMap) {
      String skill = pair[0];
      String ability = pair[1];
      int mod = state.getAbilityModifiers().getOrDefault(ability, 0);
      int bonus = 0;
      if (state.getSkillExpertise().contains(skill)) {
        bonus = 2 * state.getProficiencyBonus();
      } else if (state.getSkillProficiencies().contains(skill)) {
        bonus = state.getProficiencyBonus();
      }
      skillTotals.put(skill, mod + bonus);
    }
    state.setSkillTotals(skillTotals);

    Map<String, Integer> saveTotals = new HashMap<>();
    for (String ability : ABILITIES) {
      int mod = state.getAbilityModifiers().getOrDefault(ability, 0);
      int bonus =
          state.getSavingThrowProficiencies().contains(ability) ? state.getProficiencyBonus() : 0;
      saveTotals.put(ability, mod + bonus);
    }
    state.setSaveTotals(saveTotals);
  }

  private void computeResources(DndCharacter character, List<ResolvedEffect> effects,
      ComputedCharacterState state) {
    List<ComputedCharacterState.ResourceEntry> entries = new ArrayList<>();
    List<CharacterResource> existingResources = resourceRepository.findByCharacterId(
        character.getId());
    Map<String, CharacterResource> existingMap = new HashMap<>();
    for (CharacterResource r : existingResources) {
      existingMap.put(r.getResourceKey(), r);
    }

    for (ResolvedEffect e : effects) {
      if (e.getType() == FeatureEffectType.GRANT_RESOURCE) {
        String resourceKey = e.getPayload().path("resourceKey").asText();
        String displayName = e.getPayload().path("displayName").asText(resourceKey);
        String refresh = e.getPayload().path("refresh").asText("LONG");

        int maxUses = computeResourceMax(e.getPayload().path("usesFormula"), character, state);

        CharacterResource existing = existingMap.get(resourceKey);
        int currentUses = existing != null ? Math.min(existing.getCurrentUses(), maxUses) : maxUses;

        entries.add(ComputedCharacterState.ResourceEntry.builder()
            .resourceKey(resourceKey)
            .displayName(displayName)
            .currentUses(currentUses)
            .maxUses(maxUses)
            .refreshOn(refresh)
            .sourceFeatureId(e.getSourceFeatureId())
            .build());
      }
    }

    state.setResources(entries);
  }

  private int computeResourceMax(JsonNode usesFormula, DndCharacter character,
      ComputedCharacterState state) {
    if (usesFormula == null || usesFormula.isMissingNode()) {
      return 1;
    }

    if (usesFormula.has("perClassLevel")) {
      JsonNode perLevel = usesFormula.get("perClassLevel");
      int classLevel = state.getTotalLevel();
      int maxUses = 0;
      var fieldNames = perLevel.fieldNames();
      while (fieldNames.hasNext()) {
        String levelStr = fieldNames.next();
        int level = Integer.parseInt(levelStr);
        if (level <= classLevel) {
          maxUses = perLevel.get(levelStr).asInt();
        }
      }
      return maxUses;
    }

    int base = usesFormula.path("base").asInt(0);
    if (usesFormula.has("abilityMod") && !usesFormula.get("abilityMod").isNull()) {
      String ability = usesFormula.get("abilityMod").asText();
      base += state.getAbilityModifiers().getOrDefault(ability, 0);
    }
    if (usesFormula.path("classLevel").asBoolean(false)) {
      base += state.getTotalLevel();
    }

    return Math.max(1, base);
  }

  private void computeActions(List<ResolvedEffect> effects, ComputedCharacterState state) {
    List<ComputedCharacterState.ActionEntry> actions = new ArrayList<>();
    for (ResolvedEffect e : effects) {
      String kind = switch (e.getType()) {
        case GRANT_ACTION -> "ACTION";
        case GRANT_BONUS_ACTION -> "BONUS_ACTION";
        case GRANT_REACTION -> "REACTION";
        default -> null;
      };
      if (kind != null) {
        actions.add(ComputedCharacterState.ActionEntry.builder()
            .kind(kind)
            .name(e.getPayload().path("name").asText(""))
            .description(e.getPayload().path("description").asText(""))
            .resourceKey(e.getPayload().path("resourceKey").asText(null))
            .uses(e.getPayload().has("uses") ? e.getPayload().get("uses").asInt() : null)
            .refresh(e.getPayload().path("refresh").asText(null))
            .build());
      }
    }
    state.setActions(actions);
  }

  private void computeAttackModifiers(List<ResolvedEffect> effects, ComputedCharacterState state) {
    List<ComputedCharacterState.AttackModifierEntry> mods = new ArrayList<>();
    for (ResolvedEffect e : effects) {
      if (e.getType() == FeatureEffectType.MODIFY_ATTACK_BONUS
          || e.getType() == FeatureEffectType.MODIFY_DAMAGE
          || e.getType() == FeatureEffectType.ADD_ABILITY_MODIFIER_TO_DAMAGE) {
        mods.add(ComputedCharacterState.AttackModifierEntry.builder()
            .amount(e.getPayload().path("amount").asInt(0))
            .dice(e.getPayload().path("diceOverride").asText(null))
            .filter(e.getPayload().path("filter"))
            .build());
      }
    }
    state.setAttackModifiers(mods);
  }

  private void assembleFeatureDisplayList(long characterId, ResolvedEffects resolved,
      ComputedCharacterState state) {
    List<CharacterFeature> characterFeatures = characterFeatureRepository.findByCharacterId(
        characterId);
    List<ComputedCharacterState.DisplayFeature> display = new ArrayList<>();

    for (CharacterFeature cf : characterFeatures) {
      if (!cf.getActive()) {
        continue;
      }

      List<ComputedCharacterState.DisplayChoice> displayChoices = new ArrayList<>();
      for (CharacterFeatureChoice cfc : cf.getChoices()) {
        displayChoices.add(ComputedCharacterState.DisplayChoice.builder()
            .choiceKey(cfc.getChoiceKey())
            .name(cfc.getChoiceKey())
            .selectedValues(cfc.getSelectedValues())
            .build());
      }

      String sourceLabel = buildSourceLabel(cf);

      display.add(ComputedCharacterState.DisplayFeature.builder()
          .characterFeatureId(cf.getId())
          .name(cf.getFeature().getName())
          .description(cf.getFeature().getDescription())
          .source(cf.getSource().name())
          .sourceLabel(sourceLabel)
          .sourceContext(cf.getSourceContext())
          .choices(displayChoices)
          .build());
    }

    state.setDisplayFeatures(display);
    state.setPendingChoices(resolved.getPendingChoices());

    List<CharacterCustomFeature> customFeatures = customFeatureService.findByCharacterId(
        characterId);
    List<ComputedCharacterState.DisplayCustomFeature> customDisplay = customFeatures.stream()
        .filter(CharacterCustomFeature::getActive)
        .map(cf -> ComputedCharacterState.DisplayCustomFeature.builder()
            .id(cf.getId())
            .name(cf.getName())
            .description(cf.getDescription())
            .build())
        .toList();
    state.setDisplayCustomFeatures(customDisplay);
  }

  private String buildSourceLabel(CharacterFeature cf) {
    JsonNode ctx = cf.getSourceContext();
    return switch (cf.getSource()) {
      case CLASS -> {
        String classKey = ctx.path("classKey").asText("?");
        int level = ctx.path("classLevel").asInt(0);
        yield classKey + (level > 0 ? " (Level " + level + ")" : "");
      }
      case SUBCLASS -> {
        String subclassKey = ctx.path("subclassKey").asText("?");
        int level = ctx.path("classLevel").asInt(0);
        yield subclassKey + (level > 0 ? " (Level " + level + ")" : "");
      }
      case FEAT -> ctx.path("featKey").asText("Feat");
      case RACE, SUBRACE -> "Race";
      case BACKGROUND -> "Background";
      case FIGHTING_STYLE -> "Fighting Style";
      case CUSTOM -> "Custom";
    };
  }

  private static int profBonusForLevel(int level) {
    if (level <= 4) {
      return 2;
    }
    if (level <= 8) {
      return 3;
    }
    if (level <= 12) {
      return 4;
    }
    if (level <= 16) {
      return 5;
    }
    return 6;
  }
}
