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
import dev.ushki.livedndlist.enums.CharacterRace;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.exceptions.ResourceSaveFailureException;
import dev.ushki.livedndlist.exceptions.UnauthorizedException;
import dev.ushki.livedndlist.mapper.CharacterMapper;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.SpellRepository;
import dev.ushki.livedndlist.repository.UserRepository;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing D&D characters. Handles CRUD operations, equipment management, and
 * spell management.
 *
 * <p>All operations include ownership verification to ensure users can only
 * access and modify their own characters.
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

  private static final String CHARACTER_RESOURCE = "Character";

  /**
   * Retrieves all characters owned by a specific user with optional filtering and sorting.
   *
   * @param username the username of the character owner
   * @param race     optional filter by character race
   * @param minLevel optional minimum level filter
   * @param maxLevel optional maximum level filter
   * @param sortBy   field to sort by
   * @param sortDir  sort direction (asc/desc)
   * @return list of character summaries matching the criteria
   */
  @Transactional(readOnly = true)
  public List<CharacterSummaryResponse> getAllByUsername(
      String username,
      CharacterRace race,
      Integer minLevel,
      Integer maxLevel,
      String sortBy,
      String sortDir) {

    User user = findUserByUsername(username);

    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();

    List<DndCharacter> characters = characterRepository.findAllByOwner(user, sort);

    // Apply filters using streams
    Stream<DndCharacter> stream = characters.stream();

    if (race != null) {
      stream = stream.filter(c -> c.getRace() == race);
    }
    if (minLevel != null) {
      stream = stream.filter(c -> c.getTotalLevel() >= minLevel);
    }
    if (maxLevel != null) {
      stream = stream.filter(c -> c.getTotalLevel() <= maxLevel);
    }

    return stream
        .map(characterMapper::toSummaryResponse)
        .toList();
  }

  /**
   * Searches characters by name for a specific user.
   *
   * @param username the username of the character owner
   * @param name     the name to search for (case-insensitive, partial match)
   * @return list of matching character summaries
   */
  @Transactional(readOnly = true)
  public List<CharacterSummaryResponse> searchByName(String username, String name) {
    User user = findUserByUsername(username);
    List<DndCharacter> characters =
        characterRepository.findByOwnerAndNameContainingIgnoreCase(user, name);
    return characterMapper.toSummaryResponseList(characters);
  }

  /**
   * Retrieves a specific character by ID with ownership verification.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return the character's full details
   */
  @Transactional(readOnly = true)
  public CharacterResponse getById(Long id, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(id, username);
    return characterMapper.toResponse(character);
  }

  /**
   * Creates a new character for a user.
   *
   * @param request  the character creation request
   * @param username the username of the owner
   * @return the created character's details
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
   * Updates an existing character.
   *
   * @param id       the character ID
   * @param request  the update request with fields to change
   * @param username the username of the owner
   * @return the updated character's details
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
   */
  public void delete(Long id, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(id, username);
    characterRepository.delete(character);
    log.info("Character '{}' deleted", character.getName());
  }

  /**
   * Adds equipment to a character's inventory.
   *
   * @param characterId the character ID
   * @param request     the equipment details
   * @param username    the username of the owner
   * @return the updated character with the new equipment
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
   * Adds a spell to a character's known spells.
   *
   * @param characterId the character ID
   * @param spellId     the spell ID to add
   * @param username    the username of the owner
   * @return the updated character with the new spell
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

  private User findUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
  }

  private DndCharacter findCharacterWithOwnershipCheck(Long id, String username) {
    DndCharacter character = characterRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    if (!character.getOwner().getUsername().equals(username)) {
      throw new UnauthorizedException("You don't have access to this character");
    }

    return character;
  }

  /**
   * Retrieves all characters owned by a user, ordered by most recently updated. Useful for
   * dashboard views showing recent activity.
   *
   * @param username the username of the character owner
   * @return list of character summaries ordered by updated date (newest first)
   */
  @Transactional(readOnly = true)
  public List<CharacterSummaryResponse> getRecentCharacters(String username) {
    User user = findUserByUsername(username);
    List<DndCharacter> characters = characterRepository.findAllByOwnerOrderByUpdatedAtDesc(user);
    log.info("Retrieved {} recent characters for user '{}'", characters.size(), username);
    return characterMapper.toSummaryResponseList(characters);
  }

  /**
   * Retrieves character with owner and class information loaded. Optimized for character
   * preview/summary displays.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return character response with owner and class data
   */
  @Transactional(readOnly = true)
  public CharacterResponse getCharacterSummary(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithOwnerAndClasses(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character summary for '{}'", character.getName());
    return characterMapper.toResponse(character);
  }

  /**
   * Retrieves character with skills loaded. Optimized for skill management and character sheet
   * skill display.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return character response with skills data
   */
  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithSkills(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithSkills(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' with skills", character.getName());
    return characterMapper.toResponse(character);
  }

  /**
   * Retrieves character with spells loaded. Optimized for spellcasting management and spell
   * selection.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return character response with spells data
   */
  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithSpells(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithSpells(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' with {} spells",
        character.getName(), character.getSpells().size());
    return characterMapper.toResponse(character);
  }

  /**
   * Retrieves character with equipment loaded. Optimized for inventory management.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return character response with equipment data
   */
  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithEquipment(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithEquipment(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' with {} equipment items",
        character.getName(), character.getEquipment().size());
    return characterMapper.toResponse(character);
  }

  /**
   * Retrieves character with saving throw proficiencies loaded. Optimized for saving throw displays
   * and combat.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return character response with saving throw data
   */
  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithSavingThrows(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithSavingThrows(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' with saving throws", character.getName());
    return characterMapper.toResponse(character);
  }

  /**
   * Retrieves character optimized for character sheet display. Loads owner, classes, and skills in
   * a single query.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return character response optimized for character sheet
   */
  @Transactional(readOnly = true)
  public CharacterResponse getCharacterSheet(Long id, String username) {
    DndCharacter character = characterRepository.findByIdForCharacterSheet(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character sheet for '{}'", character.getName());
    return characterMapper.toResponse(character);
  }

  /**
   * Retrieves character optimized for combat display. Loads equipment, saving throws, and class
   * information.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return character response optimized for combat
   */
  @Transactional(readOnly = true)
  public CharacterResponse getCharacterForCombat(Long id, String username) {
    DndCharacter character = characterRepository.findByIdForCombat(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' for combat", character.getName());
    return characterMapper.toResponse(character);
  }

  /**
   * Retrieves character optimized for spellcasting display. Loads spells and class information for
   * spell slot calculation.
   *
   * @param id       the character ID
   * @param username the username of the requesting user
   * @return character response optimized for spellcasting
   */
  @Transactional(readOnly = true)
  public CharacterResponse getCharacterForSpellcasting(Long id, String username) {
    DndCharacter character = characterRepository.findByIdForSpellcasting(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' for spellcasting with {} spells",
        character.getName(), character.getSpells().size());
    return characterMapper.toResponse(character);
  }

  /**
   * Helper method to verify character ownership. Extracted for reuse across methods.
   *
   * @param character the character to verify
   * @param username  the expected owner's username
   * @throws UnauthorizedException if the user doesn't own the character
   */
  private void verifyOwnership(DndCharacter character, String username) {
    if (!character.getOwner().getUsername().equals(username)) {
      throw new UnauthorizedException("You don't have access to this character");
    }
  }

  /**
   * Creates a character with a complete starter equipment pack. Demonstrates transactional behavior
   * - all or nothing.
   *
   * <p>This method performs multiple database operations:
   * <ol>
   *   <li>Creates and saves the character</li>
   *   <li>Adds starter weapon based on class</li>
   *   <li>Adds starter armor based on class</li>
   *   <li>Adds adventuring gear pack</li>
   *   <li>Adds starter gold</li>
   *   <li>Adds cantrips for spellcasters</li>
   * </ol>
   *
   * <p>Thanks to @Transactional on this class, if any step fails,
   * all previous steps are rolled back automatically.
   *
   * @param request  the character creation request
   * @param username the username of the owner
   * @return the created character with all starter equipment
   * @throws ResourceNotFoundException if user not found
   * @throws IllegalArgumentException  if validation fails
   */
  public CharacterResponse createWithStarterPack(CharacterCreateRequest request, String username) {
    User user = findUserByUsername(username);

    log.info("Step 1: Creating character '{}'", request.getName());

    DndCharacter character = characterMapper.toEntity(request);
    character.setOwner(user);

    log.info("Step 2: Adding starter equipment");
    addStarterWeapon(character);
    addStarterArmor(character);
    addStarterPack(character);
    setStarterGold(character);

    if (request.getSpellcastingAbility() != null) {
      log.info("Step 3: Adding starter spells");
      addStarterSpells(character);
    }

    // Simulate failure for testing transaction rollback
    if (request.getName().contains("FAIL")) {
      log.error("Step 4: FAILURE! But nothing saved yet - transaction will rollback");
      throw new ResourceSaveFailureException("Simulated failure during starter pack creation");
    }

    log.info("Step 5: Saving character to database");
    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Step 6: Character '{}' saved with ID {}", savedCharacter.getName(),
        savedCharacter.getId());

    return characterMapper.toResponse(savedCharacter);
  }

  private void addStarterWeapon(DndCharacter character) {
    Equipment weapon = Equipment.builder()
        .name("Longsword")
        .type(EquipmentType.WEAPON)
        .damage("1d8")
        .damageType("slashing")
        .properties("Versatile (1d10)")
        .weight(3.0)
        .equipped(true)
        .build();
    character.addEquipment(weapon);
  }

  private void addStarterArmor(DndCharacter character) {
    Equipment armor = Equipment.builder()
        .name("Leather Armor")
        .type(EquipmentType.ARMOR)
        .description("AC 11 + Dex modifier")
        .weight(10.0)
        .equipped(true)
        .build();
    character.addEquipment(armor);
  }

  private void addStarterPack(DndCharacter character) {
    List.of(
        Equipment.builder()
            .name("Backpack")
            .type(EquipmentType.GEAR)
            .weight(5.0)
            .build(),
        Equipment.builder()
            .name("Bedroll")
            .type(EquipmentType.GEAR)
            .weight(7.0)
            .build(),
        Equipment.builder()
            .name("Rations")
            .type(EquipmentType.CONSUMABLE)
            .quantity(10)
            .weight(2.0)
            .build(),
        Equipment.builder()
            .name("Torch")
            .type(EquipmentType.GEAR)
            .quantity(5)
            .weight(1.0)
            .build()
    ).forEach(character::addEquipment);
  }

  private void setStarterGold(DndCharacter character) {
    character.getCurrency().setGold(15);
    character.getCurrency().setSilver(10);
  }

  private void addStarterSpells(DndCharacter character) {
    List<Spell> cantrips = spellRepository.findByLevel(0);
    cantrips.stream()
        .limit(2)
        .forEach(character::addSpell);
  }
}
