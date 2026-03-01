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

  private static final int NAME_MIN_LENGTH = 2;

  private static final int NAME_MAX_LENGTH = 100;

  @NotBlank(message = "Character name is required")
  @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH,
      message = "Name must be between 2 and 100 characters")
  private String name;

  @NotNull(message = "Race is required")
  private CharacterRace race;

  private String subrace;

  private CharacterAlignment alignment;

  private String background;

  @NotBlank(message = "Class name is required")
  private String className;

  private String subclass;

  @Valid
  private AbilityScoresRequest abilityScores;

  private Integer maxHitPoints;

  private String portraitUrl;

  private AbilityType spellcastingAbility;

}
