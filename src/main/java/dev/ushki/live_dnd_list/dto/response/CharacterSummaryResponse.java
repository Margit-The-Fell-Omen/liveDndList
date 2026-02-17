package dev.ushki.live_dnd_list.dto.response;

import dev.ushki.live_dnd_list.enums.CharacterRace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterSummaryResponse {

    private Long id;
    private String name;
    private CharacterRace race;
    private String classDisplay;
    private Integer totalLevel;
    private Integer currentHitPoints;
    private Integer maxHitPoints;
    private String portraitUrl;
    private LocalDateTime updatedAt;
}
