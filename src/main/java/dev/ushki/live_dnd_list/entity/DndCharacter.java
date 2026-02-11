package dev.ushki.live_dnd_list.entity;

import dev.ushki.live_dnd_list.enums.AbilityType;
import dev.ushki.live_dnd_list.enums.CharacterAlignment;
import dev.ushki.live_dnd_list.enums.CharacterRace;
import jakarta.persistence.*;
import org.apache.catalina.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.*;

public class DndCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String name;

    @Enumerated(EnumType.STRING)
    private CharacterRace race;

    private String subrace;

    @Enumerated(EnumType.STRING)
    private CharacterAlignment alignment;

    private String background;

    private String portraitUrl;

    private List<CharacterClass> classes = new ArrayList<>();

    @Embedded
    private AbilityScores abilityScores;

    // Combat stats
    private Integer maxHitPoints;
    private Integer currentHitPoints;
    private Integer temporaryHitPoints = 0;
    private Integer armorClass;
    private Integer initiative;
    private Integer speed;
    private Integer proficiencyBonus;

    private String hitDice;

    private Integer deathSaveSuccesses = 0;
    private Integer deathSaveFailures = 0;

    private List<Skill> skills = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Set<AbilityType> savingThrowProficiencies = new HashSet<>();

    private Set<String> otherProficiencies = new HashSet<>(); // languages, tools, etc.

    private List<Equipment> equipment = new ArrayList<>();

    private DndCurrency currency;

    private Set<Spell> spells = new HashSet<>();

    private Map<Integer, Integer> spellSlots = new HashMap<>();

    @Enumerated(EnumType.STRING)
    private AbilityType spellcastingAbility;

    // feats, traits & notes
    private String featuresAndTraits;
    private String backstory;
    private String personalityTraits;
    private String ideals;
    private String bonds;
    private String flaws;
    private String notes;

    // Metadata
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private boolean isPublic = false;
}
