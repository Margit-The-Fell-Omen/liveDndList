package dev.ushki.livedndlist.entity.dndCharacter.dndClass;

import dev.ushki.livedndlist.entity.dndCharacter.document.Document;
import dev.ushki.livedndlist.enums.AbilityType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

@Entity
@Table(name = "dnd_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DndClass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(unique = true, nullable = false)
  private String key;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String parentDndClassName;

  private String parentDndClassKey;

  private String hitDice;

  @Column(name = "hit_dice_name")
  private String hitDiceName;

  @Column(name = "hit_points_on_1st_level")
  private String hitPointsOn1stLevel;

  @Column(name = "hit_points_on_higher_levels")
  private String hitPointsOnHigherLevels;

  @Enumerated(EnumType.STRING)
  private List<AbilityType> savingThrows;

  @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "document_id")
  private Document document;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subclass_of_id")
  private DndClass subclassOf;

  @OneToMany(mappedBy = "subclassOf", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<DndClass> subclasses = new ArrayList<>();

  @OneToMany(
      mappedBy = "dndClass",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private List<DndClassFeature> features = new ArrayList<>();
}