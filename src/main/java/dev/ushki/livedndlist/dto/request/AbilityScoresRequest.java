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

  /**
   * Minimum allowed ability score value.
   */
  private static final int MIN_SCORE = 1;

  /**
   * Maximum allowed ability score value.
   */
  private static final int MAX_SCORE = 30;

  /**
   * Default ability score value (average human).
   */
  private static final int DEFAULT_SCORE = 10;

  /**
   * Strength score - measures physical power. Affects melee attack rolls, damage, carrying
   * capacity, and Athletics checks.
   */
  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer strength = DEFAULT_SCORE;

  /**
   * Dexterity score - measures agility and reflexes. Affects AC, initiative, ranged attacks, and
   * Acrobatics/Stealth checks.
   */
  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer dexterity = DEFAULT_SCORE;

  /**
   * Constitution score - measures endurance and vitality. Affects hit points and Constitution
   * saving throws.
   */
  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer constitution = DEFAULT_SCORE;

  /**
   * Intelligence score - measures reasoning and memory. Affects wizard spellcasting and
   * Investigation/Arcana checks.
   */
  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer intelligence = DEFAULT_SCORE;

  /**
   * Wisdom score - measures perception and insight. Affects cleric/druid spellcasting and
   * Perception/Insight checks.
   */
  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer wisdom = DEFAULT_SCORE;

  /**
   * Charisma score - measures force of personality. Affects bard/sorcerer/warlock spellcasting and
   * Persuasion/Deception checks.
   */
  @Min(MIN_SCORE)
  @Max(MAX_SCORE)
  @Builder.Default
  private Integer charisma = DEFAULT_SCORE;
}
