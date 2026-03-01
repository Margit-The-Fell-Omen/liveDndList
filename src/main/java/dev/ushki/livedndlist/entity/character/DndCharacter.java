package dev.ushki.livedndlist.entity.character;

import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import dev.ushki.livedndlist.enums.CharacterRace;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a D&D 5th Edition character. Contains all character information including
 * stats, skills, equipment, and spells.
 *
 * <p>This is the core entity for the character management system,
 * supporting features such as:
 * <ul>
 *   <li>Multiclassing (multiple character classes)</li>
 *   <li>Equipment and currency management</li>
 *   <li>Spell tracking for casters</li>
 *   <li>Skills and proficiencies</li>
 *   <li>Combat statistics (HP, AC, initiative)</li>
 *   <li>Roleplay elements (backstory, personality traits)</li>
 * </ul>
 *
 * <p>Characters are owned by a user and can optionally be made public
 * for sharing with others.
 */
@Entity
@Table(name = "characters")
@NamedEntityGraph(
    name = "Character.summary",
    attributeNodes = {
        @NamedAttributeNode("owner"),
        @NamedAttributeNode("classes")
    }
)
@NamedEntityGraph(
    name = "Character.full",
    attributeNodes = {
        @NamedAttributeNode("owner"),
        @NamedAttributeNode("classes"),
        @NamedAttributeNode("skills"),
        @NamedAttributeNode("savingThrowProficiencies")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DndCharacter {

  private static final int DEFAULT_LEVEL = 1;

  private static final int DEFAULT_XP = 0;

  private static final int DEFAULT_HP = 10;

  private static final int DEFAULT_AC = 10;

  private static final int DEFAULT_SPEED = 30;

  private static final int DEFAULT_PROF_BONUS = 2;

  // ==================== Identity ====================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User owner;

  // ==================== Basic Information ====================

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
  private Integer experiencePoints = DEFAULT_XP;

  private String portraitUrl;

  // ==================== Classes & Level ====================

  @Builder.Default
  private Integer level = DEFAULT_LEVEL;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "character_id")
  @Builder.Default
  private List<CharacterClass> classes = new ArrayList<>();

  // ==================== Ability Scores ====================

  @Embedded
  @Builder.Default
  private AbilityScores abilityScores = new AbilityScores();

  // ==================== Combat Statistics ====================

  @Builder.Default
  private Integer maxHitPoints = DEFAULT_HP;

  @Builder.Default
  private Integer currentHitPoints = DEFAULT_HP;

  @Builder.Default
  private Integer temporaryHitPoints = 0;

  @Builder.Default
  private Integer armorClass = DEFAULT_AC;

  @Builder.Default
  private Integer initiative = 0;

  @Builder.Default
  private Integer speed = DEFAULT_SPEED;

  @Builder.Default
  private Integer proficiencyBonus = DEFAULT_PROF_BONUS;

  private String hitDice;

  @Builder.Default
  private Integer deathSaveSuccesses = 0;

  @Builder.Default
  private Integer deathSaveFailures = 0;

  // ==================== Skills & Proficiencies ====================

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "character_id")
  @Builder.Default
  private List<Skill> skills = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "character_saving_throws")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Set<AbilityType> savingThrowProficiencies = new HashSet<>();

  // ==================== Equipment & Currency ====================

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "character_id")
  @Builder.Default
  private List<Equipment> equipment = new ArrayList<>();

  @Embedded
  @Builder.Default
  private DndCurrency currency = new DndCurrency();

  // ==================== Spellcasting ====================

  @ManyToMany
  @JoinTable(
      name = "character_spells",
      joinColumns = @JoinColumn(name = "character_id"),
      inverseJoinColumns = @JoinColumn(name = "spell_id")
  )
  @Builder.Default
  private Set<Spell> spells = new HashSet<>();

  @Enumerated(EnumType.STRING)
  private AbilityType spellcastingAbility;

  // ==================== Roleplay Elements ====================

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

  // ==================== Metadata ====================

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  private boolean isPublic = false;

  /**
   * Sets creation and update timestamps when entity is first persisted.
   */
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  /**
   * Updates the modification timestamp whenever the entity is updated.
   */
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // ==================== Utility Methods ====================

  /**
   * Calculates the total character level across all classes.
   *
   * @return sum of all class levels
   */
  public int getTotalLevel() {
    return classes.stream()
        .mapToInt(CharacterClass::getLevel)
        .sum();
  }

  /**
   * Adds an equipment item to the character's inventory.
   *
   * @param item the equipment to add
   */
  public void addEquipment(Equipment item) {
    equipment.add(item);
  }

  /**
   * Removes an equipment item from the character's inventory.
   *
   * @param item the equipment to remove
   */
  public void removeEquipment(Equipment item) {
    equipment.remove(item);
  }

  /**
   * Adds a spell to the character's known or prepared spells.
   *
   * @param spell the spell to add
   */
  public void addSpell(Spell spell) {
    spells.add(spell);
  }

  /**
   * Removes a spell from the character's known or prepared spells.
   *
   * @param spell the spell to remove
   */
  public void removeSpell(Spell spell) {
    spells.remove(spell);
  }
}
