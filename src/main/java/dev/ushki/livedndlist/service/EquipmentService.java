package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.Equipment;
import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.ArmorCategory;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.EquipmentRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipmentService {

  private final EquipmentRepository equipmentRepository;
  private final CharacterRepository characterRepository;
  private final EquipmentMapper equipmentMapper;
  private final CacheManager cacheManager;

  private static final String EQUIPMENT_STRING = "Equipment";
  private static final String USER_RESOURCE = "User:";
  private static final String CHARACTER_ID_SUFFIX = ":Character:";

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

      boolean hasWeightFilter = minWeight != null || maxWeight != null;
      if (hasWeightFilter) {
        stream = stream.filter(e -> Objects.nonNull(e.getWeight()));
      }

      if (minWeight != null) {
        stream = stream.filter(e -> e.getWeight() >= minWeight);
      }

      if (maxWeight != null) {
        stream = stream.filter(e -> e.getWeight() <= maxWeight);
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

  public List<EquipmentResponse> searchByName(String name, EquipmentType type, Pageable pageable) {
    CompositeKey key = new CompositeKey("search", name, type);

    return cacheManager.get(EQUIPMENT_STRING, key, () -> {
      List<Equipment> equipment = equipmentRepository.findByNameContainingIgnoreCase(name,
          pageable);

      Stream<Equipment> stream = equipment.stream();

      if (type != null) {
        stream = stream.filter(e -> e.getType() == type);
      }

      return stream
          .map(equipmentMapper::toResponse)
          .toList();
    });
  }

  private Equipment extraEquippedCheckAndSave(Equipment equipment) {
    if (equipment.getType() == EquipmentType.ARMOR
        && equipment.isEquipped()
        && equipment.getArmorCategory() != ArmorCategory.SHIELD) {
      equipmentRepository.findByCharacterAndTypeAndEquippedTrue(
              equipment.getCharacter(), EquipmentType.ARMOR)
          .stream()
          .filter(e -> !Objects.equals(e.getId(), equipment.getId()))
          .filter(e -> e.getArmorCategory() != ArmorCategory.SHIELD)
          .forEach(e -> {
            e.setEquipped(false);
            equipmentRepository.save(e);
          });
    }

    Equipment saved = equipmentRepository.save(equipment);

    DndCharacter character = saved.getCharacter();
    character.setArmorClass(recalculateArmorClass(character));
    characterRepository.save(character);

    return saved;
  }

  public EquipmentResponse create(EquipmentRequest request) {
    Equipment equipment = equipmentMapper.toEntity(request);

    Equipment savedEquipment = extraEquippedCheckAndSave(equipment);
    log.info("Equipment '{}' created", savedEquipment.getName());

    cacheManager.invalidateByPrefix(EQUIPMENT_STRING);

    return equipmentMapper.toResponse(savedEquipment);
  }

  public int recalculateArmorClass(DndCharacter character) {
    int dexMod = character.getAbilityScores().getModifier(AbilityType.DEXTERITY);

    Equipment armor = character.getEquipment().stream()
        .filter(e -> e.getType() == EquipmentType.ARMOR
            && e.getArmorCategory() != null
            && e.getArmorCategory() != ArmorCategory.SHIELD
            && e.isEquipped())
        .findFirst()
        .orElse(null);

    boolean hasShield = character.getEquipment().stream()
        .anyMatch(e -> e.getType() == EquipmentType.ARMOR
            && e.getArmorCategory() == ArmorCategory.SHIELD
            && e.isEquipped());

    int base;
    if (armor == null) {
      base = 10 + dexMod;
    } else {
      int armorAc = armor.getArmorClass() != null ? armor.getArmorClass() : 10;
      base = switch (armor.getArmorCategory()) {
        case LIGHT -> armorAc + dexMod;
        case MEDIUM -> armorAc + Math.min(dexMod, 2);
        case HEAVY -> armorAc;
        case SHIELD -> armorAc;
      };
    }

    if (hasShield) {
      base += 2;
    }

    int bonus = character.getArmorClassBonus() != null ? character.getArmorClassBonus() : 0;
    int total = base + bonus;
    return Math.max(1, total);
  }

  public EquipmentResponse update(Long id, EquipmentRequest request) {
    Equipment equipment = equipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(EQUIPMENT_STRING, "id", id));

    String username = equipment.getCharacter().getOwner().getUsername();
    Long characterId = equipment.getCharacter().getId();
    String characterCacheNamespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + characterId;

    equipmentMapper.updateEntity(equipment, request);

    Equipment savedEquipment = extraEquippedCheckAndSave(equipment);
    log.info("Equipment '{}' updated", savedEquipment.getName());

    cacheManager.invalidateByPrefix(EQUIPMENT_STRING);
    cacheManager.invalidate(characterCacheNamespace);

    return equipmentMapper.toResponse(savedEquipment);
  }

  public void delete(Long id) {
    Equipment equipment = equipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(EQUIPMENT_STRING, "id", id));
    DndCharacter character = equipment.getCharacter();
    boolean wasImpactful = equipment.getType() == EquipmentType.ARMOR && equipment.isEquipped();

    equipmentRepository.deleteById(id);

    if (wasImpactful) {
      character.getEquipment().removeIf(e -> Objects.equals(e.getId(), id));
      character.setArmorClass(recalculateArmorClass(character));
      characterRepository.save(character);
    }

    log.info("Equipment deleted: {}", id);
    cacheManager.invalidateByPrefix(EQUIPMENT_STRING);
  }
}
