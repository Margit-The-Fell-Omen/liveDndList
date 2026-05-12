package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
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

  @Schema(description = "Character race name", example = "Human")
  private String raceName;

  @Schema(description = "Character alignment", example = "NEUTRAL_GOOD")
  private CharacterAlignment alignment;

  @Schema(description = "Character background", example = "Outlander")
  private String backgroundName;

  @Schema(description = "Total experience points", example = "3500")
  private Integer experiencePoints;

  @Schema(description = "URL to character portrait", example = "https://example.com/aragorn.jpg")
  private String portraitUrl;

  @Schema(description = "List of character classes and levels")
  private List<String> classesInfo;

  @Schema(description = "Total level across all classes", example = "5")
  private Integer totalLevel;

  @Schema(description = "Ability scores and modifiers")
  private AbilityScoresResponse abilityScores;

  @Schema(description = "Maximum hit points", example = "45")
  private Integer maxHitPoints;

  @Schema(description = "Current hit points", example = "32")
  private Integer currentHitPoints;

  @Schema(description = "Temporary hit points", example = "0")
  private Integer temporaryHitPoints;

  @Schema(description = "Armor class", example = "18")
  private Integer armorClass;

  @Schema(description = "Initiative bonus", example = "4")
  private Integer initiative;

  @Schema(description = "Movement speed", example = "30")
  private Integer speed;

  @Schema(description = "Proficiency bonus", example = "3")
  private Integer proficiencyBonus;

  @Schema(description = "Hit dice string", example = "5d10")
  private String hitDice;

  @Schema(description = "Number of successful death saves", example = "0")
  private Integer deathSaveSuccesses;

  @Schema(description = "Number of failed death saves", example = "1")
  private Integer deathSaveFailures;

  @Schema(description = "List of character skills")
  private List<SkillResponse> skills;

  @Schema(description = "Abilities proficient for saving throws")
  private Set<AbilityType> savingThrowProficiencies;

  @Schema(description = "Inventory of equipment")
  private List<EquipmentResponse> equipment;

  @Schema(description = "Currency owned by character")
  private DndCurrencyResponse currency;

  @Schema(description = "Set of known spells")
  private Set<SpellResponse> spells;

  @Schema(description = "Ability used for spellcasting", example = "CHARISMA")
  private AbilityType spellcastingAbility;

  @Schema(description = "Features and traits description", example = "Ranger's Companion")
  private String featuresAndTraits;

  @Schema(description = "Backstory text", example = "Heir to the throne of Gondor...")
  private String backstory;

  @Schema(description = "Personality traits", example = "Noble, Brave")
  private String personalityTraits;

  @Schema(description = "Ideals", example = "Duty, Honor")
  private String ideals;

  @Schema(description = "Bonds", example = "Protect the Shire")
  private String bonds;

  @Schema(description = "Flaws", example = "Secretly fears failure")
  private String flaws;

  @Schema(description = "Additional notes", example = "Needs to refit armor.")
  private String notes;

  @Schema(description = "Creation timestamp", example = "2023-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Last update timestamp", example = "2023-01-02T12:00:00")
  private LocalDateTime updatedAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Represents a single class of the character")
  public static class CharacterClassResponse {

    private Long id;
    private Long classId;
    private String className;
    private String classSlug;
    private Long archetypeId;
    private String archetypeName;
    private String archetypeSlug;
    private Integer level;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Represents D&D currency breakdown")
  public static class DndCurrencyResponse {

    @Schema(description = "Copper pieces", example = "50")
    private Integer copper;

    @Schema(description = "Silver pieces", example = "10")
    private Integer silver;

    @Schema(description = "Electrum pieces", example = "0")
    private Integer electrum;

    @Schema(description = "Gold pieces", example = "100")
    private Integer gold;

    @Schema(description = "Platinum pieces", example = "5")
    private Integer platinum;
  }

}
