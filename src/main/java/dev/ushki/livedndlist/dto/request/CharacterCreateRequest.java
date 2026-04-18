package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.enums.CharacterAlignment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
@Schema(
    description = "Request object to create a new character",
    example =
        """
        {
          "name": "Arthas",
          "raceSlug": "human",
          "alignment": "NEUTRAL_GOOD",
          "background": "Sage",
          "classSlug": "cleric",
          "archetypeSlug": "life_domain",
          "abilityScores": {
            "strength": 10,
            "dexterity": 14,
            "constitution": 12,
            "intelligence": 18,
            "wisdom": 16,
            "charisma": 14
          },
          "maxHitPoints": 12,
          "portraitUrl": "https://example.com/gandalf.jpg",
          "spellcastingAbility": "INTELLIGENCE"
        }
        """
)
public class CharacterCreateRequest {

  private static final int NAME_MIN_LENGTH = 2;
  private static final int NAME_MAX_LENGTH = 100;

  @NotBlank(message = "Character name is required")
  @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH,
        message = "Name must be between 2 and 100 characters")
  @Schema(description = "Name of the character", example = "Gandalf the Grey")
  private String name;

  @NotNull(message = "Race slug is required")
  @Schema(description = "Slug of the character race", example = "human")
  private String raceSlug;

  @Schema(
      description = "Character alignment",
      example = "NEUTRAL_GOOD",
      allowableValues = {
          "LAWFUL_GOOD", "NEUTRAL_GOOD", "CHAOTIC_GOOD",
          "LAWFUL_NEUTRAL", "TRUE_NEUTRAL", "CHAOTIC_NEUTRAL",
          "LAWFUL_EVIL", "NEUTRAL_EVIL", "CHAOTIC_EVIL"
      }
  )
  private CharacterAlignment alignment;

  @Schema(description = "Character background story", example = "Sage")
  private String background;

  @NotNull(message = "Class slug is required")
  @Schema(description = "Slug of the character class", example = "cleric")
  private String classSlug;

  @Schema(description = "Slug of the character class archetype", example = "life_domain")
  private String archetypeSlug;

  @Valid
  @Schema(description = "Base ability scores for the character")
  private AbilityScoresRequest abilityScores;

  @Min(value = 1, message = "Maximum hit points must be at least 1")
  @Schema(description = "Maximum hit points", example = "12", minimum = "1")
  private Integer maxHitPoints;

  @Schema(
      description = "URL to character portrait image",
      example = "https://example.com/gandalf.jpg"
  )
  private String portraitUrl;

  @Schema(
      description = "Primary ability score used for spellcasting",
      example = "INTELLIGENCE",
      allowableValues = {
          "STRENGTH", "DEXTERITY", "CONSTITUTION",
          "INTELLIGENCE", "WISDOM", "CHARISMA"
      }
  )
  private String spellcastingAbility;
}
