package dev.ushki.livedndlist.entity.dndCharacter.feature;

import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.DndFeat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
    name = "character_feats",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_character_feats_char_feat",
        columnNames = {"character_id", "feat_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class CharacterFeat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "character_id", nullable = false)
  private DndCharacter character;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "feat_id", nullable = false)
  private DndFeat feat;

  @Column(name = "acquired_at_total_level")
  private Integer acquiredAtTotalLevel;

  @Column(name = "asi_slot_class_key")
  private String asiSlotClassKey;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CharacterFeat other)) {
      return false;
    }
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(CharacterFeat.class);
  }
}
