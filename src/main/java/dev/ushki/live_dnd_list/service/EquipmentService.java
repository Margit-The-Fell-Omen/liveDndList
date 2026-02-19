package dev.ushki.live_dnd_list.service;

import dev.ushki.live_dnd_list.dto.request.EquipmentRequest;
import dev.ushki.live_dnd_list.dto.response.EquipmentResponse;
import dev.ushki.live_dnd_list.entity.character.Equipment;
import dev.ushki.live_dnd_list.enums.EquipmentType;
import dev.ushki.live_dnd_list.exceptions.ResourceNotFoundException;
import dev.ushki.live_dnd_list.mapper.EquipmentMapper;
import dev.ushki.live_dnd_list.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAll() {
        return equipmentMapper.toResponseList(equipmentRepository.findAll());
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id));
        return equipmentMapper.toResponse(equipment);
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getByType(EquipmentType type) {
        return equipmentMapper.toResponseList(equipmentRepository.findByType(type));
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> searchByName(String name) {
        return equipmentMapper.toResponseList(equipmentRepository.findByNameContainingIgnoreCase(name));
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
