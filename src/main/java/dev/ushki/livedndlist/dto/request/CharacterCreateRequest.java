package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import dev.ushki.livedndlist.enums.CharacterRace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object to create a new character")
public class CharacterCreateRequest {

  private static final int NAME_MIN_LENGTH = 2;

  private static final int NAME_MAX_LENGTH = 100;

  @NotBlank(message = "Character name is required")
  @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH,
      message = "Name must be between 2 and 100 characters")
  @Schema(description = "Name of the character", example = "Gandalf the Grey")
  private String name;

  @NotNull(message = "Race is required")
  @Schema(description = "Character race", example = "HUMAN")
  private CharacterRace race;

  @Schema(description = "Character subrace", example = "High Elf")
  private String subrace;

  @Schema(description = "Character alignment", example = "NEUTRAL_GOOD")
  private CharacterAlignment alignment;

  @Schema(description = "Character background", example = "Sage")
  private String background;

  @NotBlank(message = "Class name is required")
  @Schema(description = "Name of the character class", example = "Wizard")
  private String className;

  @Schema(description = "Name of the character subclass", example = "School of Evocation")
  private String subclass;

  @Valid
  @Schema(description = "Base ability scores")
  private AbilityScoresRequest abilityScores;

  @Schema(description = "Maximum hit points", example = "12")
  private Integer maxHitPoints;

  @Schema(description = "URL to character portrait image", example = "https://example.com/portrait.jpg")
  private String portraitUrl;

  @Schema(description = "Ability used for spellcasting", example = "INTELLIGENCE")
  private AbilityType spellcastingAbility;

}
