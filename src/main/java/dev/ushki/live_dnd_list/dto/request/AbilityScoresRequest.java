package dev.ushki.live_dnd_list.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityScoresRequest {

    @Min(1)
    @Max(30)
    @Builder.Default
    private Integer strength = 10;

    @Min(1)
    @Max(30)
    @Builder.Default
    private Integer dexterity = 10;

    @Min(1)
    @Max(30)
    @Builder.Default
    private Integer constitution = 10;

    @Min(1)
    @Max(30)
    @Builder.Default
    private Integer intelligence = 10;

    @Min(1)
    @Max(30)
    @Builder.Default
    private Integer wisdom = 10;

    @Min(1)
    @Max(30)
    @Builder.Default
    private Integer charisma = 10;
}
