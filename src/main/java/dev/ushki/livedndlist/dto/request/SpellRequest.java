package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.enums.SpellSchool;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object to create a new spell")
public class SpellRequest {

  @NotBlank(message = "Spell name is required")
  @Schema(description = "Name of the spell", example = "Fireball")
  private String name;

  @NotNull(message = "Spell level is required")
  @Min(0)
  @Max(9)
  @Schema(description = "Spell level (0-9)", example = "3")
  private Integer level;

  @NotNull(message = "Spell school is required")
  @Schema(description = "School of magic", example = "EVOCATION")
  private SpellSchool school;

  @Schema(description = "Casting time", example = "1 action")
  private String castingTime;

  @Schema(description = "Range", example = "150 feet")
  private String range;

  @Schema(description = "Components (V, S, M)",
      example = "V, S, M (a ball of bat guano and sulfur)")
  private String components;

  @Schema(description = "Duration", example = "Instantaneous")
  private String duration;

  @Builder.Default
  @Schema(description = "Requires concentration", example = "false")
  private boolean concentration = false;

  @Builder.Default
  @Schema(description = "Can be cast as a ritual", example = "false")
  private boolean ritual = false;

  @NotNull(message = "Description is required")
  @Schema(description = "Description of the spell effect",
      example = "A bright streak flashes from your pointing finger...")
  private String description;

  @Schema(description = "Effect at higher levels",
      example = "When you cast this spell using a spell slot of 4th level or higher...")
  private String higherLevels;
}
