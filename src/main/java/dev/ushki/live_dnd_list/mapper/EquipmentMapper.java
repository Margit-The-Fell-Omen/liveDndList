package dev.ushki.live_dnd_list.mapper;

import dev.ushki.live_dnd_list.dto.request.EquipmentRequest;
import dev.ushki.live_dnd_list.dto.response.EquipmentResponse;
import dev.ushki.live_dnd_list.entity.character.Equipment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EquipmentMapper {

    public EquipmentResponse toResponse(Equipment equipment) {
        if (equipment == null) return null;

        return EquipmentResponse.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .quantity(equipment.getQuantity())
                .weight(equipment.getWeight())
                .equipped(equipment.isEquipped())
                .attuned(equipment.isAttuned())
                .type(equipment.getType())
                .damage(equipment.getDamage())
                .damageType(equipment.getDamageType())
                .properties(equipment.getProperties())
                .build();
    }

    public List<EquipmentResponse> toResponseList(List<Equipment> equipmentList) {
        return equipmentList.stream()
                .map(this::toResponse)
                .toList();
    }


    public Equipment toEntity(EquipmentRequest request) {
        if (request == null) return null;

        return Equipment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .weight(request.getWeight())
                .type(request.getType())
                .damage(request.getDamage())
                .damageType(request.getDamageType())
                .properties(request.getProperties())
                .build();
    }

    public void updateEntity(Equipment equipment, EquipmentRequest request) {
        if (request.getName() != null) equipment.setName(request.getName());
        if (request.getDescription() != null) equipment.setDescription(request.getDescription());
        if (request.getQuantity() != null) equipment.setQuantity(request.getQuantity());
        if (request.getWeight() != null) equipment.setWeight(request.getWeight());
        if (request.getType() != null) equipment.setType(request.getType());
        if (request.getDamage() != null) equipment.setDamage(request.getDamage());
        if (request.getDamageType() != null) equipment.setDamageType(request.getDamageType());
        if (request.getProperties() != null) equipment.setProperties(request.getProperties());
    }
}
