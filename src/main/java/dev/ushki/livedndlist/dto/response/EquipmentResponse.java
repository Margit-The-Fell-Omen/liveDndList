package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.ArmorCategory;
import dev.ushki.livedndlist.enums.EquipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for equipment")
public class EquipmentResponse {

  @Schema(description = "Equipment ID", example = "20")
  private Long id;

  @Schema(description = "Item name", example = "Longsword")
  private String name;

  @Schema(description = "Item description", example = "A versatile sword")
  private String description;

  @Schema(description = "Quantity owned", example = "1")
  private Integer quantity;

  @Schema(description = "Weight in pounds", example = "3.0")
  private Double weight;

  @Schema(description = "Is the item currently equipped?", example = "true")
  private boolean equipped;

  @Schema(description = "Is the item currently attuned?", example = "false")
  private boolean attuned;

  @Schema(description = "Equipment type", example = "WEAPON")
  private EquipmentType type;

  @Schema(description = "Damage string", example = "1d8")
  private String damage;

  @Schema(description = "Armor class", example = "16")
  private Integer armorClass;

  @Schema(description = "Armor category", example = "SHIELD")
  private ArmorCategory armorCategory;

  @Schema(description = "Damage type", example = "Slashing")
  private String damageType;

  @Schema(description = "Properties", example = "Versatile (1d10)")
  private String properties;
}
