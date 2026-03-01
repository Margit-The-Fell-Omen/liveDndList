package dev.ushki.livedndlist.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for character ability scores. Represents the six core ability scores used in D&D 5th
 * Edition.
 *
 * <p>Each ability score must be between 1 and 30 (inclusive).
 * Default value for all scores is 10 (average human capability).
 *
 * <p>Ability score modifiers are calculated as: {@code (score - 10) / 2}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityScoresRequest {

  private static final int MIN_SCORE = 1;

  private static final int MAX_SCORE = 30;

  private static final int DEFAULT_SCORE = 10;

  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer strength = DEFAULT_SCORE;

  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer dexterity = DEFAULT_SCORE;

  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer constitution = DEFAULT_SCORE;

  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer intelligence = DEFAULT_SCORE;

  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer wisdom = DEFAULT_SCORE;

  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer charisma = DEFAULT_SCORE;
}
