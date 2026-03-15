package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.SkillType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for a character skill")
public class SkillResponse {

  @Schema(description = "Skill ID", example = "5")
  private Long id;

  @Schema(description = "Type of skill", example = "STEALTH")
  private SkillType skillType;

  @Schema(description = "Associated ability", example = "DEXTERITY")
  private AbilityType abilityType;

  @Schema(description = "Is the character proficient in this skill?", example = "true")
  private boolean proficient;

  @Schema(description = "Does the character have expertise in this skill?", example = "false")
  private boolean expertise;

  @Schema(description = "Total bonus modifier", example = "7")
  private Integer totalBonus;
}
