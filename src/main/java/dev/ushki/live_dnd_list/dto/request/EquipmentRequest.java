package dev.ushki.live_dnd_list.dto.request;

import dev.ushki.live_dnd_list.enums.EquipmentType;
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
