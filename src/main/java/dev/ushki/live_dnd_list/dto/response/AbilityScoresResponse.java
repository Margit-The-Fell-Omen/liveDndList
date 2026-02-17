package dev.ushki.live_dnd_list.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
