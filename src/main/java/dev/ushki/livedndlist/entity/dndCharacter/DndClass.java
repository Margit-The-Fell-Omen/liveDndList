package dev.ushki.livedndlist.entity.dndCharacter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "dnd_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"archetypes"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DndClass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(unique = true, nullable = false)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "hit_dice")
  private String hitDice;

  @Column(name = "hp_at_1st_level")
  private String hpAt1stLevel;

  @Column(name = "hp_at_higher_levels", columnDefinition = "TEXT")
  private String hpAtHigherLevels;

  @Column(name = "prof_armor")
  private String profArmor;

  @Column(name = "prof_weapons")
  private String profWeapons;

  @Column(name = "prof_tools")
  private String profTools;

  @Column(name = "prof_saving_throws")
  private String profSavingThrows;

  @Column(name = "prof_skills", columnDefinition = "TEXT")
  private String profSkills;

  @Column(columnDefinition = "TEXT")
  private String equipment;

  @Column(name = "level_table", columnDefinition = "TEXT")
  private String levelTable;

  @Column(name = "spellcasting_ability")
  private String spellcastingAbility;

  @Column(name = "subtypes_name")
  private String subtypesName;

  @Column(name = "document_slug")
  private String documentSlug;

  @Column(name = "document_title")
  private String documentTitle;

  @Column(name = "document_license_url")
  private String documentLicenseUrl;

  @Column(name = "document_url")
  private String documentUrl;

  @OneToMany(mappedBy = "dndClass", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Archetype> archetypes = new ArrayList<>();

  public void addArchetype(Archetype archetype) {
    archetypes.add(archetype);
    archetype.setDndClass(this);
  }

  public void removeArchetype(Archetype archetype) {
    archetypes.remove(archetype);
    archetype.setDndClass(null);
  }
}
