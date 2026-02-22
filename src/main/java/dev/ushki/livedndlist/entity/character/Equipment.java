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

/**
 * Entity representing an equipment item in D&D 5th Edition. Includes weapons, armor, shields, and
 * general adventuring gear.
 *
 * <p>Equipment can be:
 * <ul>
 *   <li>Equipped - actively worn or wielded</li>
 *   <li>Attuned - magically bonded (required for some magic items)</li>
 *   <li>Stacked - multiple items of the same type (e.g., arrows)</li>
 * </ul>
 *
 * <p>Weapon-specific fields ({@code damage}, {@code damageType}, {@code properties})
 * are used when the equipment type is WEAPON.
 */
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
