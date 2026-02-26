package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.CharacterMapper;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Non-transactional service demonstrating partial save problem.
 *
 * <p><strong>WARNING:</strong> This service intentionally lacks @Transactional
 * to demonstrate what happens when transactions are not used properly. DO NOT use this pattern in
 * production code.
 *
 * <p>When an error occurs mid-operation:
 * <ul>
 *   <li>Already executed saves remain in database</li>
 *   <li>Database ends up in inconsistent state</li>
 *   <li>Manual cleanup may be required</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
// NOTE: Intentionally NO @Transactional here
public class NonTransactionalCharacterService {

  private final CharacterRepository characterRepository;
  private final UserRepository userRepository;
  private final CharacterMapper characterMapper;

  /**
   * Creates a character with starter equipment WITHOUT transaction.
   *
   * <p><strong>DEMONSTRATION ONLY:</strong> Shows partial save problem.
   * If this method fails after step 2, character and weapon remain in DB but armor and other items
   * are not added.
   *
   * @param request  the character creation request
   * @param username the username of the owner
   * @return the created character (if no errors occur)
   */
  public CharacterResponse createWithStarterPackNoTransaction(
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
      throw new RuntimeException("Simulated failure - partial data remains in database!");
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

    return characterMapper.toResponse(savedCharacter);
  }
}
