package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.entity.character.Race;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object to update an existing character")
public class CharacterUpdateRequest {

  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  @Schema(description = "Name of the character", example = "Gandalf the White")
  private String name;

  @Schema(description = "Character race", example = "HUMAN")
  private Race race;

  @Schema(description = "Character alignment", example = "NEUTRAL_GOOD")
  private CharacterAlignment alignment;

  @Schema(description = "Character background", example = "Sage")
  private String background;

  @Valid
  @Schema(description = "Base ability scores")
  private AbilityScoresRequest abilityScores;

  @Schema(description = "Maximum hit points", example = "20")
  private Integer maxHitPoints;

  @Schema(description = "Current hit points", example = "18")
  private Integer currentHitPoints;

  @Schema(description = "Temporary hit points", example = "5")
  private Integer temporaryHitPoints;

  @Schema(description = "Armor class", example = "14")
  private Integer armorClass;

  @Schema(description = "Movement speed in feet", example = "30")
  private Integer speed;

  @Schema(description = "URL to character portrait image", example = "https://example.com/portrait.jpg")
  private String portraitUrl;

  @Schema(description = "Detailed backstory text", example = "Born in the Shire...")
  private String backstory;

  @Schema(description = "Personality traits", example = "Brave, Curious")
  private String personalityTraits;

  @Schema(description = "Ideals", example = "Honor, Freedom")
  private String ideals;

  @Schema(description = "Bonds", example = "Protects my friends")
  private String bonds;

  @Schema(description = "Flaws", example = "Afraid of spiders")
  private String flaws;

  @Schema(description = "Additional notes", example = "Met a stranger at the inn.")
  private String notes;
}
