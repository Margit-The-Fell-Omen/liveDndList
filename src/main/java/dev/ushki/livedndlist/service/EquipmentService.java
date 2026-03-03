package dev.ushki.livedndlist.service;

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

  @Transactional(readOnly = true)
  public List<EquipmentResponse> getAll(
      EquipmentType type,
      Double minWeight,
      Double maxWeight,
      String sortBy,
      String sortDir) {

    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();

    List<Equipment> equipment = equipmentRepository.findAll(sort);

    // Apply filters using streams
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
  }

  @Transactional(readOnly = true)
  public EquipmentResponse getById(Long id) {
    Equipment equipment = equipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id));
    return equipmentMapper.toResponse(equipment);
  }

  @Transactional(readOnly = true)
  public List<EquipmentResponse> searchByName(String name, EquipmentType type) {
    List<Equipment> equipment = equipmentRepository.findByNameContainingIgnoreCase(name);

    Stream<Equipment> stream = equipment.stream();

    if (type != null) {
      stream = stream.filter(e -> e.getType() == type);
    }

    return stream
        .map(equipmentMapper::toResponse)
        .toList();
  }

  public EquipmentResponse create(EquipmentRequest request) {
    Equipment equipment = equipmentMapper.toEntity(request);
    Equipment savedEquipment = equipmentRepository.save(equipment);
    log.info("Equipment '{}' created", savedEquipment.getName());

    return equipmentMapper.toResponse(savedEquipment);
  }

  public EquipmentResponse update(Long id, EquipmentRequest request) {
    Equipment equipment = equipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id));

    equipmentMapper.updateEntity(equipment, request);
    Equipment savedEquipment = equipmentRepository.save(equipment);
    log.info("Equipment '{}' updated", savedEquipment.getName());

    return equipmentMapper.toResponse(savedEquipment);
  }

  public void delete(Long id) {
    if (!equipmentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Equipment", "id", id);
    }
    equipmentRepository.deleteById(id);
    log.info("Equipment deleted: {}", id);
  }
}
