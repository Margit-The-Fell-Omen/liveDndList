package dev.ushki.livedndlist.entity.dndCharacter.feature;

import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.dndCharacter.document.Document;
import dev.ushki.livedndlist.enums.AnnotationSource;
import dev.ushki.livedndlist.enums.FeatureSourceType;
import dev.ushki.livedndlist.enums.VisibilityScope;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Feature {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include
  private Long id;

  @Column(unique = true, nullable = false)
  @ToString.Include
  private String key;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "source_type", nullable = false, columnDefinition = "feature_source_type")
  private FeatureSourceType sourceType;

  @Column(name = "source_key", nullable = false)
  private String sourceKey;

  @Column(name = "gained_at_level")
  private Integer gainedAtLevel;

  @Column(columnDefinition = "TEXT")
  private String prerequisite;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "visibility_scope")
  @Builder.Default
  private VisibilityScope visibility = VisibilityScope.PUBLIC;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private Integer displayOrder = 0;

  @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "document_id")
  private Document document;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "effects_annotated_by", nullable = false, columnDefinition = "annotation_source")
  @Builder.Default
  private AnnotationSource effectsAnnotatedBy = AnnotationSource.NONE;

  @Column(name = "effects_annotated_at")
  private OffsetDateTime effectsAnnotatedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("displayOrder ASC, id ASC")
  @Builder.Default
  private List<FeatureEffect> effects = new ArrayList<>();

  @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("displayOrder ASC, id ASC")
  @Builder.Default
  private List<FeatureChoice> choices = new ArrayList<>();

  @PrePersist
  void onCreate() {
    OffsetDateTime now = OffsetDateTime.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public void addEffect(FeatureEffect effect) {
    effects.add(effect);
    effect.setFeature(this);
  }

  public void removeEffect(FeatureEffect effect) {
    effects.remove(effect);
    effect.setFeature(null);
  }

  public void addChoice(FeatureChoice choice) {
    choices.add(choice);
    choice.setFeature(this);
  }

  public void removeChoice(FeatureChoice choice) {
    choices.remove(choice);
    choice.setFeature(null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Feature other)) {
      return false;
    }
    if (id != null && other.id != null) {
      return id.equals(other.id);
    }
    return Objects.equals(key, other.key);
  }

  @Override
  public int hashCode() {
    return Feature.class.hashCode();
  }
}
