package dev.ushki.live_dnd_list.dto.request;

import dev.ushki.live_dnd_list.enums.SpellSchool;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpellRequest {

    @NotBlank(message = "Spell name is required")
    private String name;

    @NotNull(message = "Spell level is required")
    @Min(0)
    @Max(9)
    private Integer level;

    @NotNull(message = "Spell school is required")
    private SpellSchool school;

    private String castingTime;

    private String range;

    private String components;

    private String duration;

    @Builder.Default
    private boolean concentration = false;

    @Builder.Default
    private boolean ritual = false;

    private String description;

    private String higherLevels;
}
