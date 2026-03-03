package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.entity.character.Equipment;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class EquipmentMapper {

  public EquipmentResponse toResponse(Equipment equipment) {
    if (equipment == null) {
      return null;
    }

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
    if (request == null) {
      return null;
    }

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
    updateIfPresent(request.getName(), equipment::setName);
    updateIfPresent(request.getDescription(), equipment::setDescription);
    updateIfPresent(request.getQuantity(), equipment::setQuantity);
    updateIfPresent(request.getWeight(), equipment::setWeight);
    updateIfPresent(request.getType(), equipment::setType);
    updateIfPresent(request.getDamage(), equipment::setDamage);
    updateIfPresent(request.getDamageType(), equipment::setDamageType);
    updateIfPresent(request.getProperties(), equipment::setProperties);
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }
}
