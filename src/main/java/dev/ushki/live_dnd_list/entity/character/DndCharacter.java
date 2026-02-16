package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.entity.User;
import dev.ushki.live_dnd_list.enums.AbilityType;
import dev.ushki.live_dnd_list.enums.CharacterAlignment;
import dev.ushki.live_dnd_list.enums.CharacterRace;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DndCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CharacterRace race;

    private String subrace;

    @Enumerated(EnumType.STRING)
    private CharacterAlignment alignment;

    private String background;

    @Builder.Default
    private Integer experiencePoints = 0;

    private String portraitUrl;

    @Builder.Default
    private Integer level = 1;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "character_id")
    @Builder.Default
    private List<CharacterClass> classes = new ArrayList<>();

    @Embedded
    @Builder.Default
    private AbilityScores abilityScores = new AbilityScores();

    // Combat stats
    @Builder.Default
    private Integer maxHitPoints = 10;

    @Builder.Default
    private Integer currentHitPoints = 10;

    @Builder.Default
    private Integer temporaryHitPoints = 0;

    @Builder.Default
    private Integer armorClass = 10;

    @Builder.Default
    private Integer initiative = 0;

    @Builder.Default
    private Integer speed = 30;

    @Builder.Default
    private Integer proficiencyBonus = 2;

    private String hitDice;

    @Builder.Default
    private Integer deathSaveSuccesses = 0;

    @Builder.Default
    private Integer deathSaveFailures = 0;

    // Skills
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "character_id")
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "character_saving_throws")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<AbilityType> savingThrowProficiencies = new HashSet<>();

    // NOTE: needs implementing, but not now
    // private Set<String> otherProficiencies = new HashSet<>(); // languages, tools, etc.

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "character_id")
    @Builder.Default
    private List<Equipment> equipment = new ArrayList<>();

    @Embedded
    @Builder.Default
    private DndCurrency currency = new DndCurrency();

    @ManyToMany
    @JoinTable(
            name = "character_spells",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "spell_id")
    )
    @Builder.Default
    private Set<Spell> spells = new HashSet<>();

    // NOTE: implement in future versions
    // FIXME: why the hell does this emit an error?
    //private Map<Integer, Integer> spellSlots = new HashMap<>();

    @Enumerated(EnumType.STRING)
    private AbilityType spellcastingAbility;

    // feats, traits & notes
    @Column(columnDefinition = "TEXT")
    private String featuresAndTraits;

    @Column(columnDefinition = "TEXT")
    private String backstory;

    @Column(columnDefinition = "TEXT")
    private String personalityTraits;

    @Column(columnDefinition = "TEXT")
    private String ideals;

    @Column(columnDefinition = "TEXT")
    private String bonds;

    @Column(columnDefinition = "TEXT")
    private String flaws;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Metadata
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private boolean isPublic = false;

    // FIXME: this error with @Pre..

    @PreUpdate
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Utility methods
    public int getTotalLevel() {
        return classes.stream()
                .mapToInt(CharacterClass::getLevel)
                .sum();
    }

    public void addEquipment(Equipment item) {
        equipment.add(item);
    }

    public void removeEquipment(Equipment item) {
        equipment.remove(item);
    }

    public void addSpell(Spell spell) {
        spells.add(spell);
    }

    public void removeSpell(Spell spell) {
        spells.remove(spell);
    }
}
