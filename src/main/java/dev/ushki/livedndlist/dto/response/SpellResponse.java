package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.SpellSchool;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for a spell")
public class SpellResponse {

  @Schema(description = "Spell ID", example = "15")
  private Long id;

  @Schema(description = "Name of the spell", example = "Fireball")
  private String name;

  @Schema(description = "Spell level (0-9)", example = "3")
  private Integer level;

  @Schema(description = "School of magic", example = "EVOCATION")
  private SpellSchool school;

  @Schema(description = "Casting time", example = "1 action")
  private String castingTime;

  @Schema(description = "Range", example = "150 feet")
  private String range;

  @Schema(description = "Components", example = "V, S, M")
  private String components;

  @Schema(description = "Duration", example = "Instantaneous")
  private String duration;

  @Schema(description = "Requires concentration", example = "false")
  private boolean concentration;

  @Schema(description = "Is a ritual spell", example = "false")
  private boolean ritual;

  @Schema(description = "Spell description",
      example = "A bright streak flashes from your pointing finger...")
  private String description;

  @Schema(description = "Higher level effects",
      example = "Damage increases by 1d6 per slot level above 3rd.")
  private String higherLevels;
}
