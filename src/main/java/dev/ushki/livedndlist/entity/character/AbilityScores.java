package dev.ushki.livedndlist.entity.character;

import dev.ushki.livedndlist.enums.AbilityType;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Embeddable entity representing a character's six ability scores. These are the core attributes
 * that define a character's capabilities in D&D 5th Edition.
 *
 * <p>Each ability score ranges from 1 to 30, with 10-11 representing average human capability.
 * Default value for all scores is 10.
 *
 * <p>Ability modifiers are calculated as: {@code (score - 10) / 2}
 *
 * <p>Modifier examples:
 * <ul>
 *   <li>Score 1 → Modifier -5</li>
 *   <li>Score 10-11 → Modifier +0</li>
 *   <li>Score 14-15 → Modifier +2</li>
 *   <li>Score 20 → Modifier +5</li>
 *   <li>Score 30 → Modifier +10</li>
 * </ul>
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AbilityScores {

  @Builder.Default
  private Integer strength = 10;

  @Builder.Default
  private Integer dexterity = 10;

  @Builder.Default
  private Integer constitution = 10;

  @Builder.Default
  private Integer intelligence = 10;

  @Builder.Default
  private Integer wisdom = 10;

  @Builder.Default
  private Integer charisma = 10;

  /**
   * Calculates the modifier for a given ability type. Modifier formula: {@code (score - 10) / 2}
   * (rounded down).
   *
   * @param type the ability type to get the modifier for
   * @return the calculated modifier
   */
  public int getModifier(AbilityType type) {
    int score = switch (type) {
      case STRENGTH -> strength;
      case DEXTERITY -> dexterity;
      case CONSTITUTION -> constitution;
      case INTELLIGENCE -> intelligence;
      case WISDOM -> wisdom;
      case CHARISMA -> charisma;
    };
    return (score - 10) / 2;
  }
}
