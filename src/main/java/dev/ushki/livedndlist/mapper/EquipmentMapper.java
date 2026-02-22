package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.entity.character.Equipment;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Equipment entities and DTOs. Handles mapping for equipment
 * creation, updates, and responses.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Converting entities to response DTOs</li>
 *   <li>Converting creation requests to entities</li>
 *   <li>Updating existing entities from update requests</li>
 *   <li>Batch conversion for lists of equipment</li>
 * </ul>
 */
@Component
public class EquipmentMapper {

  /**
   * Converts an Equipment entity to an EquipmentResponse DTO.
   *
   * @param equipment the equipment entity to convert
   * @return the equipment response DTO, or null if equipment is null
   */
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

  /**
   * Converts a list of Equipment entities to EquipmentResponse DTOs.
   *
   * @param equipmentList the list of equipment entities
   * @return list of equipment response DTOs
   */
  public List<EquipmentResponse> toResponseList(List<Equipment> equipmentList) {
    return equipmentList.stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * Converts an EquipmentRequest to a new Equipment entity. The entity is created with default
   * values for equipped (false) and attuned (false).
   *
   * @param request the equipment creation request
   * @return the new equipment entity, or null if request is null
   */
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

  /**
   * Updates an existing Equipment entity from an EquipmentRequest. Only updates fields that are
   * present (non-null) in the request.
   *
   * <p>Note: This method does not update equipped or attuned status,
   * as those are typically managed separately through character actions.
   *
   * @param equipment the equipment entity to update
   * @param request   the update request containing new values
   */
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

  /**
   * Helper method for partial updates. Only applies the setter if the value is not null.
   *
   * @param value  the value to set (if not null)
   * @param setter the setter method to call
   * @param <T>    the type of the value
   */
  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }
}
