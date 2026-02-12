package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.enums.AbilityType;
import dev.ushki.live_dnd_list.enums.CharacterAlignment;
import dev.ushki.live_dnd_list.enums.CharacterRace;
import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "owner")  // Avoid circular reference
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // Only use id for equals
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
    private CharacterRace race;

    private String subrace;

    @Enumerated(EnumType.STRING)
    private CharacterAlignment alignment;

    private String background;

    private String portraitUrl;

    @Builder.Default
    private Integer level = 1;

    @OneToMany
    private List<CharacterClass> classes = new ArrayList<>();

    @Embedded
    private AbilityScores abilityScores;

    // Combat stats
    @Builder.Default
    private Integer maxHitPoints = 10;
    @Builder.Default
    private Integer currentHitPoints = 10;

    private Integer temporaryHitPoints = 0;

    private Integer armorClass;

    private Integer initiative;

    private Integer speed;

    private Integer proficiencyBonus;

    private String hitDice;

    private Integer deathSaveSuccesses = 0;
    private Integer deathSaveFailures = 0;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "character_id")
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Set<AbilityType> savingThrowProficiencies = new HashSet<>();

    private Set<String> otherProficiencies = new HashSet<>(); // languages, tools, etc.

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "character_id")
    @Builder.Default
    private List<Equipment> equipment = new ArrayList<>();

    private DndCurrency currency;

    private Set<Spell> spells = new HashSet<>();

    // FIXME: why the hell does this emit an error?
    //private Map<Integer, Integer> spellSlots = new HashMap<>();

    @Enumerated(EnumType.STRING)
    private AbilityType spellcastingAbility;

    // feats, traits & notes
    private String featuresAndTraits;

    @Column(columnDefinition = "TEXT")
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


    //TODO: carefully adjust database annotations
}
