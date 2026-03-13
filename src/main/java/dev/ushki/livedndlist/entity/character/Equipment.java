package dev.ushki.livedndlist.entity.character;

import dev.ushki.livedndlist.enums.EquipmentType;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "equipment_seq")
  @SequenceGenerator(name = "equipment_seq", sequenceName = "equipment_sequence", allocationSize = 50)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "character_id", nullable = false)
  private DndCharacter character;

  @Column(nullable = false)
  private String name;

  private String description;

  @Builder.Default
  private Integer quantity = 1;

  private Double weight;

  @Builder.Default
  private boolean equipped = false;

  @Builder.Default
  private boolean attuned = false;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(columnDefinition = "equipment_type")
  private EquipmentType type;

  private String damage;
  private String damageType;
  private String properties;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Equipment equipment = (Equipment) o;
    if (id != null && equipment.id != null) {
      return id.equals(equipment.id);
    }
    return Objects.equals(name, equipment.name) && type == equipment.type;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
