package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.SkillType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing skill information for a character. Represents one of the 18 skills in D&D
 * 5th Edition.
 *
 * <p>Total bonus calculation:
 * <ul>
 *   <li>Base: ability modifier</li>
 *   <li>Proficient: + proficiency bonus</li>
 *   <li>Expertise: + proficiency bonus × 2 (instead of × 1)</li>
 * </ul>
 *
 * <p>Example: A level 5 character with 16 DEX (+3) and Stealth expertise
 * would have: +3 (DEX) + 6 (proficiency × 2) = +9 Stealth.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

  private Long id;
  private SkillType skillType;
  private AbilityType abilityType;
  private boolean proficient;
  private boolean expertise;
  private Integer totalBonus;
}
