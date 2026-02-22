package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.entity.character.Spell;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.exceptions.UnauthorizedException;
import dev.ushki.livedndlist.mapper.CharacterMapper;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.SpellRepository;
import dev.ushki.livedndlist.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing D&D characters. Handles CRUD operations, equipment management, and
 * spell management.
 *
 * <p>All operations include ownership verification to ensure users can only
 * access and modify their own characters.
 *
 * <p>Key features:
 * <ul>
 *   <li>Character creation with initial class and ability scores</li>
 *   <li>Character updates with partial field updates</li>
 *   <li>Equipment management (add/remove items)</li>
 *   <li>Spell management (add/remove known spells)</li>
 *   <li>Ownership-based access control</li>
 * </ul>
 *
 * <p>All write operations are transactional and logged for audit purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CharacterService {

  private final CharacterRepository characterRepository;
  private final UserRepository userRepository;
  private final SpellRepository spellRepository;
  private final CharacterMapper characterMapper;
  private final EquipmentMapper equipmentMapper;

  /**
   * Retrieves all characters owned by a specific user. Characters are ordered by most recently
   * updated first.
   *
   * @param username the username of the character owner
   * @return list of character summaries for the user
   * @throws ResourceNotFoundException if the user is not found
   */
  @Transactional(readOnly = true)
  public List<CharacterSummaryResponse> getAllByUsername(String username) {
    User user = findUserByUsername(username);
    List<DndCharacter> characters = characterRepository.findAllByOwnerOrderByUpdatedAtDesc(user);
    return characterMapper.toSummaryResponseList(characters);
  }

  /**
   * Retrieves a specific character by ID with ownership verification.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return the character's full details
   * @throws ResourceNotFoundException if the character is not found
   * @throws UnauthorizedException     if the user doesn't own the character
   */
  @Transactional(readOnly = true)
  public CharacterResponse getById(Long id, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(id, username);
    return characterMapper.toResponse(character);
  }

  /**
   * Creates a new character for a user.
   *
   * <p>The character is initialized with:
   * <ul>
   *   <li>Starting class at level 1</li>
   *   <li>Ability scores (default or provided)</li>
   *   <li>All 18 skills with no proficiencies</li>
   *   <li>Default currency (0 in all denominations)</li>
   * </ul>
   *
   * @param request  the character creation request
   * @param username the username of the owner
   * @return the created character's details
   * @throws ResourceNotFoundException if the user is not found
   */
  public CharacterResponse create(CharacterCreateRequest request, String username) {
    User user = findUserByUsername(username);

    DndCharacter character = characterMapper.toEntity(request);
    character.setOwner(user);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Character '{}' created for user '{}'", savedCharacter.getName(), username);

    return characterMapper.toResponse(savedCharacter);
  }

  /**
   * Updates an existing character. Only provided fields are updated (partial update support).
   *
   * @param id       the character ID
   * @param request  the update request with fields to change
   * @param username the username of the owner
   * @return the updated character's details
   * @throws ResourceNotFoundException if the character is not found
   * @throws UnauthorizedException     if the user doesn't own the character
   */
  public CharacterResponse update(Long id, CharacterUpdateRequest request, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(id, username);

    characterMapper.updateEntity(character, request);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Character '{}' updated", savedCharacter.getName());

    return characterMapper.toResponse(savedCharacter);
  }

  /**
   * Deletes a character.
   *
   * @param id       the character ID
   * @param username the username of the owner
   * @throws ResourceNotFoundException if the character is not found
   * @throws UnauthorizedException     if the user doesn't own the character
   */
  public void delete(Long id, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(id, username);
    characterRepository.delete(character);
    log.info("Character '{}' deleted", character.getName());
  }

  /**
   * Adds equipment to a character's inventory. Equipment is created and added as a new item.
   *
   * @param characterId the character ID
   * @param request     the equipment details
   * @param username    the username of the owner
   * @return the updated character with the new equipment
   * @throws ResourceNotFoundException if the character is not found
   * @throws UnauthorizedException     if the user doesn't own the character
   */
  public CharacterResponse addEquipment(Long characterId, EquipmentRequest request,
      String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Equipment equipment = equipmentMapper.toEntity(request);
    character.addEquipment(equipment);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Equipment '{}' added to character '{}'", equipment.getName(), character.getName());

    return characterMapper.toResponse(savedCharacter);
  }

  /**
   * Removes equipment from a character's inventory.
   *
   * @param characterId the character ID
   * @param equipmentId the equipment ID to remove
   * @param username    the username of the owner
   * @return the updated character without the equipment
   * @throws ResourceNotFoundException if the character or equipment is not found
   * @throws UnauthorizedException     if the user doesn't own the character
   */
  public CharacterResponse removeEquipment(Long characterId, Long equipmentId, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Equipment equipment = character.getEquipment().stream()
        .filter(e -> e.getId().equals(equipmentId))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

    character.removeEquipment(equipment);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Equipment removed from character '{}'", character.getName());

    return characterMapper.toResponse(savedCharacter);
  }

  /**
   * Adds a spell to a character's known spells. The spell must already exist in the spell library.
   *
   * @param characterId the character ID
   * @param spellId     the spell ID to add
   * @param username    the username of the owner
   * @return the updated character with the new spell
   * @throws ResourceNotFoundException if the character or spell is not found
   * @throws UnauthorizedException     if the user doesn't own the character
   */
  public CharacterResponse addSpell(Long characterId, Long spellId, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Spell spell = spellRepository.findById(spellId)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", spellId));

    character.addSpell(spell);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Spell '{}' added to character '{}'", spell.getName(), character.getName());

    return characterMapper.toResponse(savedCharacter);
  }

  /**
   * Removes a spell from a character's known spells.
   *
   * @param characterId the character ID
   * @param spellId     the spell ID to remove
   * @param username    the username of the owner
   * @return the updated character without the spell
   * @throws ResourceNotFoundException if the character or spell is not found
   * @throws UnauthorizedException     if the user doesn't own the character
   */
  public CharacterResponse removeSpell(Long characterId, Long spellId, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Spell spell = spellRepository.findById(spellId)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", spellId));

    character.removeSpell(spell);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Spell '{}' removed from character '{}'", spell.getName(), character.getName());

    return characterMapper.toResponse(savedCharacter);
  }

  /**
   * Finds a user by username.
   *
   * @param username the username to search for
   * @return the user entity
   * @throws ResourceNotFoundException if the user is not found
   */
  private User findUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
  }

  /**
   * Finds a character by ID and verifies ownership. Ensures users can only access their own
   * characters.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return the character entity
   * @throws ResourceNotFoundException if the character is not found
   * @throws UnauthorizedException     if the user doesn't own the character
   */
  private DndCharacter findCharacterWithOwnershipCheck(Long id, String username) {
    DndCharacter character = characterRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Character", "id", id));

    if (!character.getOwner().getUsername().equals(username)) {
      throw new UnauthorizedException("You don't have access to this character");
    }

    return character;
  }
}
