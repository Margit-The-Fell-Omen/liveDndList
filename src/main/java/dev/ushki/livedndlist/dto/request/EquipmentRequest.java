package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.enums.EquipmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating equipment items. Supports various equipment types including
 * weapons, armor, and general items.
 *
 * <p>Required fields:
 * <ul>
 *   <li>{@code name} - The equipment's name</li>
 * </ul>
 *
 * <p>Weapon-specific fields ({@code damage}, {@code damageType}, {@code properties})
 * should be provided when the equipment type is WEAPON.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequest {

  @NotBlank(message = "Equipment name is required")
  private String name;

  private String description;

  @Positive
  @Builder.Default
  private Integer quantity = 1;

  private Double weight;

  private EquipmentType type;

  // Weapon specific
  private String damage;
  private String damageType;
  private String properties;
}
