package dev.ushki.live_dnd_list.entity.character;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DndCurrency {

    @Builder.Default
    private Integer copper = 0;

    @Builder.Default
    private Integer silver = 0;

    @Builder.Default
    private Integer electrum = 0;

    @Builder.Default
    private Integer gold = 0;

    @Builder.Default
    private Integer platinum = 0;

    public int getTotalInCopper() {
        // magic numbers are from PHB
        return copper + (silver * 10) + (electrum * 50) + (gold * 100) + (platinum * 1000);
    }
}
