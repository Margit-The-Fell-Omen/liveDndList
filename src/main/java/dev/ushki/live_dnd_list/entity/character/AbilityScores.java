package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.enums.AbilityType;
import jakarta.persistence.Embeddable;

@Embeddable
public class AbilityScores {
    private Integer strength;
    private Integer dexterity;
    private Integer constitution;
    private Integer intelligence;
    private Integer wisdom;
    private Integer charisma;

    public int GetModifier(AbilityType type) {
        int score = switch (type) {
            case STRENGTH -> strength;
            case DEXTERITY -> dexterity;
            case CONSTITUTION -> constitution;
            case INTELLIGENCE -> intelligence;
            case WISDOM -> wisdom;
            case CHARISMA -> charisma;
        };
        return (score - 10) / 2;
    }
}
