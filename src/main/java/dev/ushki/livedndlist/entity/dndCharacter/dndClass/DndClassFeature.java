package dev.ushki.livedndlist.entity.dndCharacter.dndClass;

import dev.ushki.livedndlist.enums.DndClassFeatureType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "dnd_class_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DndClassFeature {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(nullable = false, unique = true)
  private String key;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "feature_type")
  @Enumerated(EnumType.STRING)
  private DndClassFeatureType featureType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dnd_class_id", nullable = false)
  private DndClass dndClass;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "dnd_class_feature_gained_at",
      joinColumns = @JoinColumn(name = "feature_id")
  )
  private List<GainedAt> gainedAt = new ArrayList<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "dnd_class_table_data",
      joinColumns = @JoinColumn(name = "feature_id")
  )
  private List<DndClassTableData> dataForClassTable = new ArrayList<>();
}
