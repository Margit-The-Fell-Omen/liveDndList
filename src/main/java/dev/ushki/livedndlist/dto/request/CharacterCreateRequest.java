package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import dev.ushki.livedndlist.enums.CharacterRace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new D&D character. Contains all required and optional fields for
 * character creation.
 *
 * <p>Required fields:
 * <ul>
 *   <li>{@code name} - The character's name</li>
 *   <li>{@code race} - The character's race</li>
 *   <li>{@code className} - The character's starting class</li>
 * </ul>
 *
 * <p>The character will be created at level 1 in the specified class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterCreateRequest {

  /**
   * Minimum length for character name.
   */
  private static final int NAME_MIN_LENGTH = 2;

  /**
   * Maximum length for character name.
   */
  private static final int NAME_MAX_LENGTH = 100;

  /**
   * The character's name. Must be between 2 and 100 characters.
   */
  @NotBlank(message = "Character name is required")
  @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH,
      message = "Name must be between 2 and 100 characters")
  private String name;

  /**
   * The character's race (e.g., HUMAN, ELF, DWARF). Determines racial traits and ability score
   * bonuses.
   */
  @NotNull(message = "Race is required")
  private CharacterRace race;

  /**
   * The character's subrace (e.g., High Elf, Hill Dwarf). Optional - not all races have subraces.
   */
  private String subrace;

  /**
   * The character's moral and ethical alignment. Optional (e.g., LAWFUL_GOOD, CHAOTIC_NEUTRAL).
   */
  private CharacterAlignment alignment;

  /**
   * The character's background (e.g., Noble, Soldier, Sage). Provides additional proficiencies and
   * features.
   */
  private String background;

  /**
   * The character's starting class name (e.g., Fighter, Wizard, Rogue). Determines hit dice,
   * proficiencies, and class features.
   */
  @NotBlank(message = "Class name is required")
  private String className;

  /**
   * The character's subclass (e.g., Champion, Evoker, Thief). Optional - typically chosen at level
   * 1-3 depending on class.
   */
  private String subclass;

  /**
   * The character's ability scores. If not provided, default scores (10 for all) will be used.
   */
  @Valid
  private AbilityScoresRequest abilityScores;

  /**
   * The character's maximum hit points. If not provided, will be calculated based on class and
   * Constitution.
   */
  private Integer maxHitPoints;

  /**
   * URL to the character's portrait image. Optional - used for visual representation.
   */
  private String portraitUrl;

  private AbilityType spellcastingAbility;

}
