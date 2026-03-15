package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.enums.EquipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object to add or update equipment")
public class EquipmentRequest {

  @NotBlank(message = "Equipment name is required")
  @Schema(description = "Name of the equipment item", example = "Longsword")
  private String name;

  @Schema(description = "Description of the item", example = "A versatile sword")
  private String description;

  @Positive
  @Builder.Default
  @Schema(description = "Quantity of the item", example = "1")
  private Integer quantity = 1;

  @Schema(description = "Weight of the item in pounds", example = "3.0")
  private Double weight;

  @Schema(description = "Type of equipment", example = "WEAPON")
  private EquipmentType type;

  @Schema(description = "Damage dice (e.g., 1d8)", example = "1d8")
  private String damage;

  @Schema(description = "Damage type", example = "Slashing")
  private String damageType;

  @Schema(description = "Weapon properties (e.g., Versatile, Finesse)",
      example = "Versatile (1d10)")
  private String properties;
}
