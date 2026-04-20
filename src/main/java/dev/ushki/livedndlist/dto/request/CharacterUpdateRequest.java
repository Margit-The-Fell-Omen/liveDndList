package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.enums.CharacterAlignment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object to update character")
public class CharacterUpdateRequest {

  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  @Schema(description = "Character name", example = "Gandalf the White")
  private String name;

  @Schema(description = "Race ID", example = "1")
  private Long raceId;

  @Schema(description = "Character alignment", example = "LAWFUL_GOOD")
  private CharacterAlignment alignment;

  @Schema(description = "Character background", example = "Sage")
  private String background;

  @Valid
  @Schema(description = "Ability scores")
  private AbilityScoresRequest abilityScores;

  @Min(1)
  @Schema(description = "Maximum hit points", example = "45")
  private Integer maxHitPoints;

  @Min(0)
  @Schema(description = "Current hit points", example = "32")
  private Integer currentHitPoints;

  @Min(0)
  @Schema(description = "Temporary hit points", example = "5")
  private Integer temporaryHitPoints;

  @Min(1)
  @Schema(description = "Armor class", example = "16")
  private Integer armorClass;

  @Min(0)
  @Schema(description = "Speed in feet", example = "30")
  private Integer speed;

  @Schema(description = "Portrait URL", example = "https://example.com/portrait.jpg")
  private String portraitUrl;

  @Schema(description = "Character backstory")
  private String backstory;

  @Schema(description = "Personality traits")
  private String personalityTraits;

  @Schema(description = "Ideals")
  private String ideals;

  @Schema(description = "Bonds")
  private String bonds;

  @Schema(description = "Flaws")
  private String flaws;

  @Schema(description = "Notes")
  private String notes;

  @Schema(description = "Spellcasting ability", example = "INTELLIGENCE")
  private String spellcastingAbility;

  @Min(0)
  @Max(3)
  @Schema(description = "Death saves successes", example = "0")
  private Integer deathSavesSuccesses;

  @Min(0)
  @Max(3)
  @Schema(description = "Death saves failures", example = "0")
  private Integer deathSavesFailures;
}
