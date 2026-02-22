package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.EquipmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing equipment item information. Supports various equipment types including
 * weapons, armor, and general items.
 *
 * <p>Weapon-specific fields ({@code damage}, {@code damageType}, {@code properties})
 * are populated when the equipment type is WEAPON.
 *
 * <p>Some magic items require attunement to use their magical properties.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponse {

  private Long id;
  private String name;
  private String description;
  private Integer quantity;
  private Double weight;
  private boolean equipped;
  private boolean attuned;
  private EquipmentType type;
  private String damage;
  private String damageType;
  private String properties;
}
