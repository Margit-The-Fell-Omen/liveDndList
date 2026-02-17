package dev.ushki.live_dnd_list.dto.response;


import dev.ushki.live_dnd_list.enums.SpellSchool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpellResponse {

    private Long id;
    private String name;
    private Integer level;
    private SpellSchool school;
    private String castingTime;
    private String range;
    private String components;
    private String duration;
    private boolean concentration;
    private boolean ritual;
    private String description;
    private String higherLevels;
}
