package dev.ushki.livedndlist.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
