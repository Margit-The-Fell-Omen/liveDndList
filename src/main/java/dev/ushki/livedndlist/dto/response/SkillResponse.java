package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.SkillType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
