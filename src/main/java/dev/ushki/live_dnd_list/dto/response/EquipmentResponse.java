package dev.ushki.live_dnd_list.dto.response;

import dev.ushki.live_dnd_list.enums.EquipmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
