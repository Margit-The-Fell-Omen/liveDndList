package dev.ushki.livedndlist.entity.character;

import dev.ushki.livedndlist.enums.SkillType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a character's skill proficiency. D&D 5th Edition has 18 skills, each tied to
 * one of the six ability scores.
 *
 * <p>Skill check bonus calculation:
 * <ul>
 *   <li>Base: ability modifier</li>
 *   <li>Proficient: + proficiency bonus</li>
 *   <li>Expertise: + proficiency bonus × 2 (replaces single proficiency)</li>
 *   <li>Additional bonus: any magic items, class features, etc.</li>
 * </ul>
 *
 * <p>Expertise requires proficiency first and is typically available
 * only to Rogues and Bards (or via certain feats).
 */
@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private DndCharacter character;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SkillType skillType;

  @Builder.Default
  private boolean proficiency = false;

  @Builder.Default
  private boolean expertise = false;

  private Integer bonus = 0;

  /**
   * Checks if the character is proficient in this skill. Convenience method matching JavaBean
   * naming convention.
   *
   * @return true if proficient, false otherwise
   */
  public boolean isProficient() {
    return proficiency;
  }
}
