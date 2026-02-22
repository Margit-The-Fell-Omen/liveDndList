package dev.ushki.livedndlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for character ability scores. Contains the six core ability scores and their
 * calculated modifiers.
 *
 * <p>Each ability includes both the raw score (1-30) and its modifier.
 * Modifiers are calculated as: {@code (score - 10) / 2}
 *
 * <p>Example modifiers:
 * <ul>
 *   <li>Score 10-11 → Modifier +0</li>
 *   <li>Score 14-15 → Modifier +2</li>
 *   <li>Score 18-19 → Modifier +4</li>
 *   <li>Score 8-9 → Modifier -1</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityScoresResponse {

  private Integer strength;
  private Integer strengthModifier;

  private Integer dexterity;
  private Integer dexterityModifier;

  private Integer constitution;
  private Integer constitutionModifier;

  private Integer intelligence;
  private Integer intelligenceModifier;

  private Integer wisdom;
  private Integer wisdomModifier;

  private Integer charisma;
  private Integer charismaModifier;
}
