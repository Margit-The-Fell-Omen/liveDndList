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
