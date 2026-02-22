package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.enums.SpellSchool;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a spell. Contains all spell properties as defined in D&D 5th
 * Edition.
 *
 * <p>Required fields:
 * <ul>
 *   <li>{@code name} - The spell's name</li>
 *   <li>{@code level} - The spell level (0-9)</li>
 *   <li>{@code school} - The school of magic</li>
 * </ul>
 *
 * <p>Level 0 spells are cantrips, which can be cast without expending spell slots.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpellRequest {

  @NotBlank(message = "Spell name is required")
  private String name;

  @NotNull(message = "Spell level is required")
  @Min(0)
  @Max(9)
  private Integer level;

  @NotNull(message = "Spell school is required")
  private SpellSchool school;

  private String castingTime;

  private String range;

  private String components;

  private String duration;

  @Builder.Default
  private boolean concentration = false;

  @Builder.Default
  private boolean ritual = false;

  private String description;

  private String higherLevels;
}
