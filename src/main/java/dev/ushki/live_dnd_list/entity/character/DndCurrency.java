package dev.ushki.live_dnd_list.entity.character;

import jakarta.persistence.Embeddable;

@Embeddable
public class DndCurrency {
    private Integer copper = 0;
    private Integer silver = 0;
    private Integer electrum = 0;
    private Integer gold = 0;
    private Integer platinum = 0;
}
