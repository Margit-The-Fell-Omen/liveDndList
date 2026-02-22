package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.EquipmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing equipment items. Handles CRUD operations and search functionality for
 * the equipment library.
 *
 * <p>The equipment library is a shared resource containing all available
 * equipment items that characters can use. This includes:
 * <ul>
 *   <li>Weapons (swords, bows, etc.)</li>
 *   <li>Armor (leather, chain mail, plate, etc.)</li>
 *   <li>Shields</li>
 *   <li>Adventuring gear (rope, torches, etc.)</li>
 *   <li>Magic items</li>
 * </ul>
 *
 * <p>Equipment can be searched by type or name for easy discovery.
 * All write operations are transactional and logged for audit purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipmentService {

  private final EquipmentRepository equipmentRepository;
  private final EquipmentMapper equipmentMapper;

  /**
   * Retrieves all equipment items from the library.
   *
   * @return list of all equipment items
   */
  @Transactional(readOnly = true)
  public List<EquipmentResponse> getAll() {
    return equipmentMapper.toResponseList(equipmentRepository.findAll());
  }

  /**
   * Retrieves a specific equipment item by ID.
   *
   * @param id the equipment ID
   * @return the equipment details
   * @throws ResourceNotFoundException if the equipment is not found
   */
  @Transactional(readOnly = true)
  public EquipmentResponse getById(Long id) {
    Equipment equipment = equipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id));
    return equipmentMapper.toResponse(equipment);
  }

  /**
   * Retrieves all equipment items of a specific type. Useful for filtering equipment by category.
   *
   * <p>Examples:
   * <ul>
   *   <li>Get all weapons: {@code getByType(EquipmentType.WEAPON)}</li>
   *   <li>Get all armor: {@code getByType(EquipmentType.ARMOR)}</li>
   *   <li>Get all magic items: {@code getByType(EquipmentType.MAGIC_ITEM)}</li>
   * </ul>
   *
   * @param type the equipment type to filter by
   * @return list of equipment items of the specified type
   */
  @Transactional(readOnly = true)
  public List<EquipmentResponse> getByType(EquipmentType type) {
    return equipmentMapper.toResponseList(equipmentRepository.findByType(type));
  }

  /**
   * Searches for equipment by name using case-insensitive partial matching.
   *
   * <p>Example:
   * <pre>{@code
   * // Finds "Longsword", "Shortsword", "Greatsword", etc.
   * searchByName("sword");
   * }</pre>
   *
   * @param name the search term (case-insensitive, partial match)
   * @return list of equipment items with names containing the search term
   */
  @Transactional(readOnly = true)
  public List<EquipmentResponse> searchByName(String name) {
    return equipmentMapper.toResponseList(
        equipmentRepository.findByNameContainingIgnoreCase(name));
  }

  /**
   * Creates a new equipment item in the library. Typically used by administrators to populate the
   * equipment database.
   *
   * @param request the equipment creation request
   * @return the created equipment details
   */
  public EquipmentResponse create(EquipmentRequest request) {
    Equipment equipment = equipmentMapper.toEntity(request);
    Equipment savedEquipment = equipmentRepository.save(equipment);
    log.info("Equipment '{}' created", savedEquipment.getName());

    return equipmentMapper.toResponse(savedEquipment);
  }

  /**
   * Updates an existing equipment item. Only provided fields are updated (partial update support).
   *
   * <p>Note: This updates the equipment template in the library.
   * It does not affect equipment already added to characters' inventories.
   *
   * @param id      the equipment ID
   * @param request the update request with fields to change
   * @return the updated equipment details
   * @throws ResourceNotFoundException if the equipment is not found
   */
  public EquipmentResponse update(Long id, EquipmentRequest request) {
    Equipment equipment = equipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id));

    equipmentMapper.updateEntity(equipment, request);
    Equipment savedEquipment = equipmentRepository.save(equipment);
    log.info("Equipment '{}' updated", savedEquipment.getName());

    return equipmentMapper.toResponse(savedEquipment);
  }

  /**
   * Deletes an equipment item from the library.
   *
   * <p>Warning: This removes the equipment template from the library.
   * Equipment already in character inventories may still reference this item.
   *
   * @param id the equipment ID to delete
   * @throws ResourceNotFoundException if the equipment is not found
   */
  public void delete(Long id) {
    if (!equipmentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Equipment", "id", id);
    }
    equipmentRepository.deleteById(id);
    log.info("Equipment deleted: {}", id);
  }
}
