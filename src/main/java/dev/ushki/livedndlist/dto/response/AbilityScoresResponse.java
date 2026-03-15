package dev.ushki.livedndlist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing ability scores and their modifiers")
public class AbilityScoresResponse {

  @Schema(description = "Strength score value", example = "16")
  private Integer strength;

  @Schema(description = "Strength modifier", example = "3")
  private Integer strengthModifier;

  @Schema(description = "Dexterity score value", example = "14")
  private Integer dexterity;

  @Schema(description = "Dexterity modifier", example = "2")
  private Integer dexterityModifier;

  @Schema(description = "Constitution score value", example = "14")
  private Integer constitution;

  @Schema(description = "Constitution modifier", example = "2")
  private Integer constitutionModifier;

  @Schema(description = "Intelligence score value", example = "10")
  private Integer intelligence;

  @Schema(description = "Intelligence modifier", example = "0")
  private Integer intelligenceModifier;

  @Schema(description = "Wisdom score value", example = "12")
  private Integer wisdom;

  @Schema(description = "Wisdom modifier", example = "1")
  private Integer wisdomModifier;

  @Schema(description = "Charisma score value", example = "8")
  private Integer charisma;

  @Schema(description = "Charisma modifier", example = "-1")
  private Integer charismaModifier;
}
