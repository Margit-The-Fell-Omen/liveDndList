package dev.ushki.live_dnd_list.dto.response;

import dev.ushki.live_dnd_list.enums.AbilityType;
import dev.ushki.live_dnd_list.enums.CharacterAlignment;
import dev.ushki.live_dnd_list.enums.CharacterRace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterResponse {

    private Long id;
    private String name;
    private CharacterRace race;
    private String subrace;
    private CharacterAlignment alignment;
    private String background;
    private Integer experiencePoints;
    private String portraitUrl;

    // Classes
    private List<CharacterClassResponse> classes;
    private Integer totalLevel;

    // Ability Scores
    private AbilityScoresResponse abilityScores;

    // Combat
    private Integer maxHitPoints;
    private Integer currentHitPoints;
    private Integer temporaryHitPoints;
    private Integer armorClass;
    private Integer initiative;
    private Integer speed;
    private Integer proficiencyBonus;
    private String hitDice;
    private Integer deathSaveSuccesses;
    private Integer deathSaveFailures;

    // Skills
    private List<SkillResponse> skills;
    private Set<AbilityType> savingThrowProficiencies;

    // Equipment
    private List<EquipmentResponse> equipment;
    private DndCurrencyResponse currency;

    // Spells
    private Set<SpellResponse> spells;
    private AbilityType spellcastingAbility;

    // Character details
    private String featuresAndTraits;
    private String backstory;
    private String personalityTraits;
    private String ideals;
    private String bonds;
    private String flaws;
    private String notes;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterClassResponse {
        private Long id;
        private String className;
        private String subclass;
        private Integer level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DndCurrencyResponse {
        private Integer copper;
        private Integer silver;
        private Integer electrum;
        private Integer gold;
        private Integer platinum;
    }

}
