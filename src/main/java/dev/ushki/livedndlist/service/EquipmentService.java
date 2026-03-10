package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.EquipmentRepository;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipmentService {

  private final EquipmentRepository equipmentRepository;
  private final EquipmentMapper equipmentMapper;
  private final CacheManager cacheManager;

  private static final String EQUIPMENT_STRING = "Equipment";

  public List<EquipmentResponse> getAll(
      EquipmentType type,
      Double minWeight,
      Double maxWeight,
      String sortBy,
      String sortDir) {

    CompositeKey key = new CompositeKey("all", type, minWeight, maxWeight, sortBy, sortDir);

    return cacheManager.get(EQUIPMENT_STRING, key, () -> {
      Sort sort = sortDir.equalsIgnoreCase("asc")
          ? Sort.by(sortBy).ascending()
          : Sort.by(sortBy).descending();

      List<Equipment> equipment = equipmentRepository.findAll(sort);

      Stream<Equipment> stream = equipment.stream();

      if (type != null) {
        stream = stream.filter(e -> e.getType() == type);
      }
      if (minWeight != null) {
        stream = stream.filter(e -> e.getWeight() != null && e.getWeight() >= minWeight);
      }
      if (maxWeight != null) {
        stream = stream.filter(e -> e.getWeight() != null && e.getWeight() <= maxWeight);
      }

      return stream
          .map(equipmentMapper::toResponse)
          .toList();
    });
  }

  public EquipmentResponse getById(Long id) {
    CompositeKey key = new CompositeKey("byId", id);

    return cacheManager.get(EQUIPMENT_STRING, key, () -> {
      Equipment equipment = equipmentRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException(EQUIPMENT_STRING, "id", id));
      return equipmentMapper.toResponse(equipment);
    });
  }

  public List<EquipmentResponse> searchByName(String name, EquipmentType type) {
    CompositeKey key = new CompositeKey("search", name, type);

    return cacheManager.get(EQUIPMENT_STRING, key, () -> {
      List<Equipment> equipment = equipmentRepository.findByNameContainingIgnoreCase(name);

      Stream<Equipment> stream = equipment.stream();

      if (type != null) {
        stream = stream.filter(e -> e.getType() == type);
      }

      return stream
          .map(equipmentMapper::toResponse)
          .toList();
    });
  }

  @Transactional
  public EquipmentResponse create(EquipmentRequest request) {
    Equipment equipment = equipmentMapper.toEntity(request);
    Equipment savedEquipment = equipmentRepository.save(equipment);
    log.info("Equipment '{}' created", savedEquipment.getName());

    cacheManager.invalidateByPrefix(EQUIPMENT_STRING);

    return equipmentMapper.toResponse(savedEquipment);
  }

  @Transactional
  public EquipmentResponse update(Long id, EquipmentRequest request) {
    Equipment equipment = equipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(EQUIPMENT_STRING, "id", id));

    equipmentMapper.updateEntity(equipment, request);
    Equipment savedEquipment = equipmentRepository.save(equipment);
    log.info("Equipment '{}' updated", savedEquipment.getName());

    cacheManager.invalidateByPrefix(EQUIPMENT_STRING);

    return equipmentMapper.toResponse(savedEquipment);
  }

  @Transactional
  public void delete(Long id) {
    if (!equipmentRepository.existsById(id)) {
      throw new ResourceNotFoundException(EQUIPMENT_STRING, "id", id);
    }
    equipmentRepository.deleteById(id);
    log.info("Equipment deleted: {}", id);

    cacheManager.invalidateByPrefix(EQUIPMENT_STRING);
  }
}
