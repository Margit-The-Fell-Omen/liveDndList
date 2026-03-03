package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.exceptions.ResourceSaveFailureException;
import dev.ushki.livedndlist.mapper.CharacterMapper;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
// NOTE: Intentionally NO @Transactional here
public class NonTransactionalCharacterService {

  private final CharacterRepository characterRepository;
  private final UserRepository userRepository;
  private final CharacterMapper characterMapper;

  public void createWithStarterPackNoTransaction(
      CharacterCreateRequest request,
      String username) {

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

    // Step 1: Create and save character
    DndCharacter character = characterMapper.toEntity(request);
    character.setOwner(user);
    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Step 1: Character '{}' saved with ID {}",
        savedCharacter.getName(), savedCharacter.getId());

    // Step 2: Add and save weapon
    Equipment weapon = Equipment.builder()
        .name("Longsword")
        .type(EquipmentType.WEAPON)
        .damage("1d8")
        .damageType("slashing")
        .weight(3.0)
        .equipped(true)
        .build();
    savedCharacter.addEquipment(weapon);
    characterRepository.save(savedCharacter);
    log.info("Step 2: Weapon saved");

    // Step 3: Simulate failure AFTER some data is saved
    if (request.getName().contains("FAIL")) {
      log.error("Step 3: FAILURE! Character and weapon already in DB!");
      throw new ResourceSaveFailureException("Simulated failure - "
          + "partial data remains in database!");
    }

    // Step 4: Add armor (never reached if FAIL)
    Equipment armor = Equipment.builder()
        .name("Chain Mail")
        .type(EquipmentType.ARMOR)
        .weight(55.0)
        .equipped(true)
        .build();
    savedCharacter.addEquipment(armor);
    characterRepository.save(savedCharacter);
    log.info("Step 4: Armor saved");

    characterMapper.toResponse(savedCharacter);
  }
}
