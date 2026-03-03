package dev.ushki.livedndlist.entity.character;

import dev.ushki.livedndlist.enums.EquipmentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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
  private EquipmentType type;

  private String damage;
  private String damageType;
  private String properties;
}
