package dev.ushki.live_dnd_list.dto.response;

import dev.ushki.live_dnd_list.enums.AbilityType;
import dev.ushki.live_dnd_list.enums.SkillType;
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
