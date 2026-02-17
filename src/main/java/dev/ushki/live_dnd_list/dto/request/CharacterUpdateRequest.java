package dev.ushki.live_dnd_list.dto.request;

import dev.ushki.live_dnd_list.enums.CharacterAlignment;
import dev.ushki.live_dnd_list.enums.CharacterRace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterUpdateRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private CharacterRace race;

    private String subrace;

    private CharacterAlignment alignment;

    private String background;

    @Valid
    private AbilityScoresRequest abilityScores;

    private Integer maxHitPoints;

    private Integer currentHitPoints;

    private Integer temporaryHitPoints;

    private Integer armorClass;

    private Integer speed;

    private String portraitUrl;

    private String backstory;

    private String personalityTraits;

    private String ideals;

    private String bonds;

    private String flaws;

    private String notes;
}
