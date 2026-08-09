package dev.ushki.livedndlist.service.features.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.enums.CreatureSize;
import dev.ushki.livedndlist.enums.CreatureType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComputedCharacterState {

  @Builder.Default
  private int totalLevel = 1;

  @Builder.Default
  private int proficiencyBonus = 2;

  @Builder.Default
  private Map<String, Integer> finalAbilityScores = new HashMap<>();

  @Builder.Default
  private Map<String, Integer> abilityModifiers = new HashMap<>();

  @Builder.Default
  private CreatureSize size = CreatureSize.MEDIUM;

  @Builder.Default
  private CreatureType creatureType = CreatureType.HUMANOID;

  @Builder.Default
  private Map<String, Integer> speeds = new HashMap<>();

  @Builder.Default
  private List<SenseEntry> senses = new ArrayList<>();

  @Builder.Default
  private Set<String> damageResistances = new HashSet<>();

  @Builder.Default
  private Set<String> damageImmunities = new HashSet<>();

  @Builder.Default
  private Set<String> damageVulnerabilities = new HashSet<>();

  @Builder.Default
  private Set<String> conditionImmunities = new HashSet<>();

  @Builder.Default
  private Set<String> armorProficiencies = new HashSet<>();

  @Builder.Default
  private Set<String> weaponProficiencies = new HashSet<>();

  @Builder.Default
  private Set<String> toolProficiencies = new HashSet<>();

  @Builder.Default
  private Set<String> languages = new HashSet<>();

  @Builder.Default
  private Set<String> savingThrowProficiencies = new HashSet<>();

  @Builder.Default
  private Set<String> skillProficiencies = new HashSet<>();

  @Builder.Default
  private Set<String> skillExpertise = new HashSet<>();

  @Builder.Default
  private int armorClass = 10;

  @Builder.Default
  private int initiative = 0;

  @Builder.Default
  private Map<String, Integer> skillTotals = new HashMap<>();

  @Builder.Default
  private Map<String, Integer> saveTotals = new HashMap<>();

  @Builder.Default
  private Map<String, HitDiceEntry> hitDice = new HashMap<>();

  @Builder.Default
  private int hpPerLevelModifier = 0;

  @Builder.Default
  private int hpFlatModifier = 0;

  private SpellcastingState spellcasting;

  @Builder.Default
  private List<ResourceEntry> resources = new ArrayList<>();

  @Builder.Default
  private List<ActionEntry> actions = new ArrayList<>();

  @Builder.Default
  private List<AttackModifierEntry> attackModifiers = new ArrayList<>();

  @Builder.Default
  private List<DisplayFeature> displayFeatures = new ArrayList<>();

  @Builder.Default
  private List<DisplayCustomFeature> displayCustomFeatures = new ArrayList<>();

  @Builder.Default
  private List<PendingChoice> pendingChoices = new ArrayList<>();

  @Data
  @Builder
  @AllArgsConstructor
  public static class SenseEntry {

    private String senseType;
    private int range;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class HitDiceEntry {

    private String die;
    private int count;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SpellcastingState {

    @Builder.Default
    private List<ClassSpellcasting> classes = new ArrayList<>();
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class ClassSpellcasting {

    private String classKey;
    private String ability;
    private String casterType;
    private int spellSaveDc;
    private int spellAttackBonus;
    @Builder.Default
    private Map<Integer, Integer> spellSlotsTotal = new HashMap<>();
    private Integer preparedSpellsCount;
    private String spellList;
    private boolean ritualCasting;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class ResourceEntry {

    private String resourceKey;
    private String displayName;
    private int currentUses;
    private int maxUses;
    private String refreshOn;
    private Long sourceFeatureId;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class ActionEntry {

    private String kind;
    private String name;
    private String description;
    private String resourceKey;
    private Integer uses;
    private String refresh;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class AttackModifierEntry {

    private int amount;
    private String dice;
    private JsonNode filter;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class DisplayFeature {

    private long characterFeatureId;
    private String name;
    private String description;
    private String source;
    private String sourceLabel;
    private JsonNode sourceContext;
    @Builder.Default
    private List<DisplayChoice> choices = new ArrayList<>();
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class DisplayChoice {

    private String choiceKey;
    private String name;
    private JsonNode selectedValues;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class DisplayCustomFeature {

    private long id;
    private String name;
    private String description;
  }
}
