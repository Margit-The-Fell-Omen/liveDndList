package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.enums.AbilityType;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AbilityScores {

    @Builder.Default
    private Integer strength = 10;

    @Builder.Default
    private Integer dexterity = 10;

    @Builder.Default
    private Integer constitution = 10;

    @Builder.Default
    private Integer intelligence = 10;

    @Builder.Default
    private Integer wisdom = 10;

    @Builder.Default
    private Integer charisma = 10;

    public int getModifier(AbilityType type) {
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

    public int getScore(AbilityType type) {
        return switch (type) {
            case STRENGTH -> strength;
            case DEXTERITY -> dexterity;
            case CONSTITUTION -> constitution;
            case INTELLIGENCE -> intelligence;
            case WISDOM -> wisdom;
            case CHARISMA -> charisma;
        };
    }
}
