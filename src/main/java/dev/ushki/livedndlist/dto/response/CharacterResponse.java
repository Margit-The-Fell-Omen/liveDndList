package dev.ushki.livedndlist.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.dto.DndClassLevelDto;
import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
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
@Schema(description = "Full detailed response object for a character")
public class CharacterResponse {

  @Schema(description = "Unique identifier of the character", example = "1")
  private Long id;

  @Schema(description = "Character name", example = "Aragorn")
  private String name;

  @Schema(description = "Character race key", example = "srd_human")
  private String raceKey;

  @Schema(description = "Character alignment", example = "NEUTRAL_GOOD")
  private CharacterAlignment alignment;

  @Schema(description = "Character background key", example = "srd_2014_outlander")
  private String backgroundKey;

  @Schema(description = "Total experience points", example = "3500")
  private Integer experiencePoints;

  @Schema(description = "URL to character portrait", example = "https://example.com/aragorn.jpg")
  private String portraitUrl;

  @Schema(description = "List of character classes and levels")
  private List<DndClassLevelDto> classesInfo;

  @Schema(description = "Total level across all classes", example = "5")
  private Integer totalLevel;

  @Schema(description = "Ability scores and modifiers (computed)")
  private AbilityScoresResponse abilityScores;

  @Schema(description = "Maximum hit points", example = "45")
  private Integer maxHitPoints;

  @Schema(description = "Current hit points", example = "32")
  private Integer currentHitPoints;

  @Schema(description = "Temporary hit points", example = "0")
  private Integer temporaryHitPoints;

  @Schema(description = "Armor class (computed)", example = "18")
  private Integer armorClass;

  @Schema(description = "Manual AC bonus (editable escape hatch)", example = "-1")
  private Integer armorClassBonus;

  @Schema(description = "Initiative bonus (computed)", example = "4")
  private Integer initiative;

  @Schema(description = "Speeds by movement type (computed)")
  private Map<String, Integer> speeds;

  @Schema(description = "Proficiency bonus (computed from total level)", example = "3")
  private Integer proficiencyBonus;

  @Schema(description = "Hit dice grouped by die size")
  private Map<String, HitDiceEntryResponse> hitDice;

  @Schema(description = "Creature size (computed)", example = "MEDIUM")
  private String size;

  @Schema(description = "Creature type (computed)", example = "HUMANOID")
  private String creatureType;

  @Schema(description = "Number of successful death saves", example = "0")
  private Integer deathSaveSuccesses;

  @Schema(description = "Number of failed death saves", example = "1")
  private Integer deathSaveFailures;

  @Schema(description = "List of character skills with computed totals")
  private List<SkillResponse> skills;

  @Schema(description = "Ability proficiencies for saving throws (computed)")
  private Set<AbilityType> savingThrowProficiencies;

  @Schema(description = "Aggregated proficiencies (computed)")
  private ProficienciesResponse proficiencies;

  @Schema(description = "Senses granted by features (computed)")
  private List<SenseResponse> senses;

  @Schema(description = "Damage resistances (computed)")
  private Set<String> damageResistances;

  @Schema(description = "Damage immunities (computed)")
  private Set<String> damageImmunities;

  @Schema(description = "Damage vulnerabilities (computed)")
  private Set<String> damageVulnerabilities;

  @Schema(description = "Condition immunities (computed)")
  private Set<String> conditionImmunities;

  @Schema(description = "Inventory of equipment")
  private List<EquipmentResponse> equipment;

  @Schema(description = "Currency owned by character")
  private DndCurrencyResponse currency;

  @Schema(description = "Set of known spells")
  private Set<SpellResponse> spells;

  @Schema(description = "Ability used for spellcasting", example = "CHARISMA")
  private AbilityType spellcastingAbility;

  @Schema(description = "Spellcasting details per class (computed)")
  private SpellcastingResponse spellcasting;

  @Schema(description = "Character resources (spell slots, ki, rage, etc.)")
  private List<ResourceResponse> resources;

  @Schema(description = "Actions granted by features")
  private List<ActionResponse> actions;

  @Schema(description = "Attack modifiers applied to matching weapons")
  private List<AttackModifierResponse> attackModifiers;

  @Schema(description = "Aggregated features from all sources")
  private List<CharacterFeatureResponse> features;

  @Schema(description = "User-authored custom narrative features")
  private List<CustomFeatureResponse> customFeatures;

  @Schema(description = "Choices the user still needs to make")
  private List<PendingChoiceResponse> pendingChoices;

  @Schema(description = "Backstory text")
  private String backstory;

  @Schema(description = "Personality traits")
  private String personalityTraits;

  @Schema(description = "Ideals")
  private String ideals;

  @Schema(description = "Bonds")
  private String bonds;

  @Schema(description = "Flaws")
  private String flaws;

  @Schema(description = "Additional notes")
  private String notes;

  @Schema(description = "Creation timestamp", example = "2023-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Last update timestamp", example = "2023-01-02T12:00:00")
  private LocalDateTime updatedAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class HitDiceEntryResponse {

    private String die;
    private Integer count;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProficienciesResponse {

    private Set<String> armor;
    private Set<String> weapons;
    private Set<String> tools;
    private Set<String> languages;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SenseResponse {

    private String senseType;
    private Integer range;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SpellcastingResponse {

    private List<ClassSpellcastingResponse> classes;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ClassSpellcastingResponse {

    private String classKey;
    private String ability;
    private String casterType;
    private Integer spellSaveDc;
    private Integer spellAttackBonus;
    private Map<Integer, Integer> spellSlotsTotal;
    private Map<Integer, Integer> spellSlotsUsed;
    private Integer preparedSpellsCount;
    private String spellList;
    private Boolean ritualCasting;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ResourceResponse {

    private String resourceKey;
    private String displayName;
    private Integer currentUses;
    private Integer maxUses;
    private String refreshOn;
    private Long sourceFeatureId;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ActionResponse {

    private String kind;
    private String name;
    private String description;
    private String resourceKey;
    private Integer uses;
    private String refresh;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AttackModifierResponse {

    private Integer amount;
    private String dice;
    private JsonNode filter;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CharacterFeatureResponse {

    private Long id;
    private String name;
    private String description;
    private String source;
    private String sourceLabel;
    private JsonNode sourceContext;
    private List<FeatureChoiceAnswerResponse> choices;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FeatureChoiceAnswerResponse {

    private String choiceKey;
    private String name;
    private JsonNode selectedValues;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CustomFeatureResponse {

    private Long id;
    private String name;
    private String description;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PendingChoiceResponse {

    private Long characterFeatureId;
    private String choiceKey;
    private String name;
    private String description;
    private Integer chooseCount;
    private String optionsSource;
    private JsonNode optionsFilter;
    private JsonNode currentSelection;
  }
}
