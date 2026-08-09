package dev.ushki.livedndlist.service.features;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.enums.FeatureEffectType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EffectPayloadValidator {

  private static final Map<FeatureEffectType, Set<String>> REQUIRED_FIELDS = new EnumMap<>(
      FeatureEffectType.class);
  private static final Map<FeatureEffectType, Set<String>> ALLOWED_FIELDS = new EnumMap<>(
      FeatureEffectType.class);

  static {
    register(FeatureEffectType.MODIFY_ABILITY_SCORE,
        Set.of("ability", "amount"), Set.of("ability", "amount", "max"));
    register(FeatureEffectType.SET_ABILITY_SCORE_MINIMUM,
        Set.of("ability", "value"), Set.of("ability", "value"));
    register(FeatureEffectType.GRANT_ABILITY_SCORE_IMPROVEMENT,
        Set.of("choiceKey"), Set.of("choiceKey"));
    register(FeatureEffectType.GRANT_SKILL_PROFICIENCY,
        Set.of(), Set.of("skill"));
    register(FeatureEffectType.GRANT_SKILL_EXPERTISE,
        Set.of(), Set.of("skill"));
    register(FeatureEffectType.GRANT_SAVING_THROW_PROFICIENCY,
        Set.of("ability"), Set.of("ability"));
    register(FeatureEffectType.GRANT_ARMOR_PROFICIENCY,
        Set.of("category"), Set.of("category", "multiclass"));
    register(FeatureEffectType.GRANT_WEAPON_PROFICIENCY,
        Set.of("scope"), Set.of("scope", "category", "weaponKey", "multiclass"));
    register(FeatureEffectType.GRANT_TOOL_PROFICIENCY,
        Set.of(), Set.of("toolKey"));
    register(FeatureEffectType.GRANT_LANGUAGE,
        Set.of(), Set.of("languageKey"));
    register(FeatureEffectType.MODIFY_SPEED,
        Set.of("amount"), Set.of("amount", "speedType"));
    register(FeatureEffectType.GRANT_SPEED_TYPE,
        Set.of("speedType"), Set.of("speedType", "value", "matchWalking"));
    register(FeatureEffectType.MODIFY_ARMOR_CLASS,
        Set.of("mode"), Set.of("mode", "formula", "amount"));
    register(FeatureEffectType.MODIFY_INITIATIVE,
        Set.of("amount"), Set.of("amount"));
    register(FeatureEffectType.GRANT_DAMAGE_RESISTANCE,
        Set.of("damageType"), Set.of("damageType"));
    register(FeatureEffectType.GRANT_DAMAGE_IMMUNITY,
        Set.of("damageType"), Set.of("damageType"));
    register(FeatureEffectType.GRANT_DAMAGE_VULNERABILITY,
        Set.of("damageType"), Set.of("damageType"));
    register(FeatureEffectType.GRANT_CONDITION_IMMUNITY,
        Set.of("condition"), Set.of("condition"));
    register(FeatureEffectType.GRANT_SENSE,
        Set.of("senseType", "range"), Set.of("senseType", "range"));
    register(FeatureEffectType.GRANT_SPELLCASTING,
        Set.of("ability", "casterType"),
        Set.of("ability", "casterType", "spellList", "ritualCasting", "spellbook"));
    register(FeatureEffectType.GRANT_SPELL_SLOTS,
        Set.of("byClassLevel"), Set.of("byClassLevel"));
    register(FeatureEffectType.GRANT_SPELL,
        Set.of("spellKey"),
        Set.of("spellKey", "alwaysPrepared", "atWill", "usesPerRest", "castingAbilityOverride"));
    register(FeatureEffectType.GRANT_CANTRIP,
        Set.of("spellKey"),
        Set.of("spellKey", "alwaysPrepared", "atWill", "usesPerRest", "castingAbilityOverride"));
    register(FeatureEffectType.GRANT_RITUAL_CASTING,
        Set.of(), Set.of());
    register(FeatureEffectType.MODIFY_SPELL_ATTACK_BONUS,
        Set.of("amount"), Set.of("amount"));
    register(FeatureEffectType.MODIFY_SPELL_SAVE_DC,
        Set.of("amount"), Set.of("amount"));
    register(FeatureEffectType.PREPARED_SPELLS_COUNT,
        Set.of("formula"), Set.of("formula"));
    register(FeatureEffectType.SET_HIT_DIE,
        Set.of("die"), Set.of("die"));
    register(FeatureEffectType.MODIFY_HIT_POINTS_PER_LEVEL,
        Set.of("amount"), Set.of("amount"));
    register(FeatureEffectType.MODIFY_MAX_HIT_POINTS,
        Set.of("amount"), Set.of("amount"));
    register(FeatureEffectType.SET_CREATURE_SIZE,
        Set.of("size"), Set.of("size"));
    register(FeatureEffectType.SET_CREATURE_TYPE,
        Set.of("type"), Set.of("type"));
    register(FeatureEffectType.GRANT_FEAT,
        Set.of(), Set.of("featKey", "choiceKey", "filter"));
    register(FeatureEffectType.GRANT_FIGHTING_STYLE,
        Set.of("choiceKey"), Set.of("choiceKey", "options"));
    register(FeatureEffectType.GRANT_RESOURCE,
        Set.of("resourceKey", "displayName", "usesFormula", "refresh"),
        Set.of("resourceKey", "displayName", "usesFormula", "refresh"));
    register(FeatureEffectType.GRANT_ACTION,
        Set.of("name"), Set.of("name", "description", "resourceKey", "uses", "refresh"));
    register(FeatureEffectType.GRANT_BONUS_ACTION,
        Set.of("name"), Set.of("name", "description", "resourceKey", "uses", "refresh"));
    register(FeatureEffectType.GRANT_REACTION,
        Set.of("name"), Set.of("name", "description", "resourceKey", "uses", "refresh"));
    register(FeatureEffectType.MODIFY_ATTACK_BONUS,
        Set.of("amount"), Set.of("amount", "filter"));
    register(FeatureEffectType.MODIFY_DAMAGE,
        Set.of(), Set.of("amount", "diceOverride", "filter"));
    register(FeatureEffectType.ADD_ABILITY_MODIFIER_TO_DAMAGE,
        Set.of("ability"), Set.of("ability", "filter"));
    register(FeatureEffectType.NARRATIVE_ONLY,
        Set.of(), Set.of());
  }

  private static void register(FeatureEffectType type, Set<String> required, Set<String> allowed) {
    REQUIRED_FIELDS.put(type, required);
    ALLOWED_FIELDS.put(type, allowed);
  }

  public void validate(FeatureEffectType type, JsonNode payload, String choiceKey) {
    if (type == null) {
      throw new IllegalArgumentException("Effect type must not be null");
    }
    if (payload == null || payload.isMissingNode()) {
      throw new IllegalArgumentException("Effect payload must not be null for type " + type);
    }

    if (choiceKey != null && !choiceKey.isBlank()) {
      return;
    }

    Set<String> required = REQUIRED_FIELDS.get(type);
    Set<String> allowed = ALLOWED_FIELDS.get(type);

    if (required == null) {
      log.warn("No validation rules registered for effect type {}, skipping", type);
      return;
    }

    for (String field : required) {
      if (!payload.has(field) || payload.get(field).isNull()) {
        throw new IllegalArgumentException(
            "Effect type " + type + " requires field '" + field + "' in payload");
      }
    }

    payload.fieldNames().forEachRemaining(field -> {
      if (!allowed.contains(field)) {
        throw new IllegalArgumentException(
            "Effect type " + type + " does not allow field '" + field + "' in payload");
      }
    });
  }
}
