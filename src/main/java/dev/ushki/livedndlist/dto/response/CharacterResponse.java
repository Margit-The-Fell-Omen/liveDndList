package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import dev.ushki.livedndlist.enums.CharacterRace;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing complete character information. Includes all character attributes, combat
 * stats, equipment, spells, and roleplay details.
 *
 * <p>This is the full character sheet representation returned when
 * viewing or editing a specific character.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterResponse {

  private Long id;
  private String name;
  private CharacterRace race;
  private String subrace;
  private CharacterAlignment alignment;
  private String background;
  private Integer experiencePoints;
  private String portraitUrl;

  // Classes
  private List<CharacterClassResponse> classes;
  private Integer totalLevel;

  // Ability Scores
  private AbilityScoresResponse abilityScores;

  // Combat
  private Integer maxHitPoints;
  private Integer currentHitPoints;
  private Integer temporaryHitPoints;
  private Integer armorClass;
  private Integer initiative;
  private Integer speed;
  private Integer proficiencyBonus;
  private String hitDice;
  private Integer deathSaveSuccesses;
  private Integer deathSaveFailures;

  // Skills
  private List<SkillResponse> skills;
  private Set<AbilityType> savingThrowProficiencies;

  // Equipment
  private List<EquipmentResponse> equipment;
  private DndCurrencyResponse currency;

  // Spells
  private Set<SpellResponse> spells;
  private AbilityType spellcastingAbility;

  // Character details
  private String featuresAndTraits;
  private String backstory;
  private String personalityTraits;
  private String ideals;
  private String bonds;
  private String flaws;
  private String notes;

  // Metadata
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Represents a character's class and level in that class. Characters can have multiple classes
   * when multiclassing.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CharacterClassResponse {

    private Long id;
    private String className;
    private String subclass;
    private Integer level;
  }

  /**
   * Represents the character's currency holdings in D&D coinage.
   *
   * <p>Exchange rates:
   * <ul>
   *   <li>10 copper (cp) = 1 silver (sp)</li>
   *   <li>10 silver (sp) = 1 gold (gp)</li>
   *   <li>2 electrum (ep) = 1 gold (gp)</li>
   *   <li>10 gold (gp) = 1 platinum (pp)</li>
   * </ul>
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DndCurrencyResponse {

    private Integer copper;
    private Integer silver;
    private Integer electrum;
    private Integer gold;
    private Integer platinum;
  }

}
