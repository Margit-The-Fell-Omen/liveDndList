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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "character_seq")
  @SequenceGenerator(name = "character_seq", sequenceName = "character_sequence")
  @EqualsAndHashCode.Include
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User owner;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "character_race_type")
  private CharacterRace race;

  private String subrace;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(columnDefinition = "character_alignment_type")
  private CharacterAlignment alignment;

  private String background;

  @Builder.Default
  private Integer experiencePoints = DEFAULT_XP;

  private String portraitUrl;

  @Builder.Default
  private Integer level = DEFAULT_LEVEL;

  @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<CharacterClass> classes = new HashSet<>();

  @Embedded
  @Builder.Default
  private AbilityScores abilityScores = new AbilityScores();

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

  @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<Skill> skills = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "character_saving_throws")
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "saving_throw_proficiencies", columnDefinition = "ability_type")
  @Builder.Default
  private Set<AbilityType> savingThrowProficiencies = new HashSet<>();

  @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<Equipment> equipment = new HashSet<>();

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

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(columnDefinition = "ability_type")
  private AbilityType spellcastingAbility;

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

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  private boolean isPublic = false;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public int getTotalLevel() {
    return classes.stream()
        .mapToInt(CharacterClass::getLevel)
        .sum();
  }

  public void addClass(CharacterClass clazz) {
    classes.add(clazz);
    clazz.setCharacter(this);
  }

  public void addEquipment(Equipment item) {
    equipment.add(item);
    item.setCharacter(this);
  }

  public void removeEquipment(Equipment item) {
    equipment.remove(item);
    item.setCharacter(null);
  }

  public void addSpell(Spell spell) {
    spells.add(spell);
  }

  public void removeSpell(Spell spell) {
    spells.remove(spell);
  }
}
