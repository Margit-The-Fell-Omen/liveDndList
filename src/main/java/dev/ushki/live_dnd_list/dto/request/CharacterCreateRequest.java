package dev.ushki.live_dnd_list.dto.request;

import dev.ushki.live_dnd_list.enums.CharacterAlignment;
import dev.ushki.live_dnd_list.enums.CharacterRace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterCreateRequest {

    @NotBlank(message = "Character name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Race is required")
    private CharacterRace race;

    private String subrace;

    private CharacterAlignment alignment;

    private String background;

    @NotBlank(message = "Class name is required")
    private String className;

    private String subclass;

    @Valid
    private AbilityScoresRequest abilityScores;

    private Integer maxHitPoints;

    private String portraitUrl;
}
