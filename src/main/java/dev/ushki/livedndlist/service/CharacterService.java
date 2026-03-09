package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.cache.CharacterQueryIndex;
import dev.ushki.livedndlist.cache.CharacterQueryKey;
import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.entity.character.Spell;
import dev.ushki.livedndlist.enums.CharacterRace;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.exceptions.ResourceSaveFailureException;
import dev.ushki.livedndlist.exceptions.UnauthorizedException;
import dev.ushki.livedndlist.mapper.CharacterMapper;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.SpellRepository;
import dev.ushki.livedndlist.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  private final CharacterQueryIndex queryIndex;  // Added

  private static final String CHARACTER_RESOURCE = "Character";

  @Transactional(readOnly = true)
  public PageResponse<CharacterSummaryResponse> getAllByUsername(
      String username,
      CharacterRace race,
      Integer minLevel,
      Integer maxLevel,
      Pageable pageable) {

    User user = findUserByUsername(username);

    CharacterQueryKey cacheKey = CharacterQueryKey.builder()
        .userId(user.getId())
        .race(race)
        .minLevel(minLevel)
        .maxLevel(maxLevel)
        .pageNumber(pageable.getPageNumber())
        .pageSize(pageable.getPageSize())
        .sortBy(pageable.getSort().toString())
        .sortDirection(pageable.getSort().isSorted() ? "sorted" : "unsorted")
        .build();

    Optional<PageResponse<CharacterSummaryResponse>> cached = queryIndex.get(cacheKey);
    if (cached.isPresent()) {
      log.debug("Returning cached result for user '{}'", username);
      return cached.get();
    }

    Page<DndCharacter> characterPage;

    if (race != null) {
      characterPage = characterRepository.findByOwnerAndRace(user, race, pageable);
    } else {
      characterPage = characterRepository.findAllByOwner(user, pageable);
    }

    Page<CharacterSummaryResponse> responsePage = characterPage.map(character -> {
      int totalLevel = character.getTotalLevel();
      if (minLevel != null && totalLevel < minLevel) {
        return null;
      }
      if (maxLevel != null && totalLevel > maxLevel) {
        return null;
      }
      return characterMapper.toSummaryResponse(character);
    });

    List<CharacterSummaryResponse> filteredContent = responsePage.getContent().stream()
        .filter(Objects::nonNull)
        .toList();

    PageResponse<CharacterSummaryResponse> result = PageResponse.<CharacterSummaryResponse>builder()
        .content(filteredContent)
        .pageNumber(characterPage.getNumber())
        .pageSize(characterPage.getSize())
        .totalElements(characterPage.getTotalElements())
        .totalPages(characterPage.getTotalPages())
        .first(characterPage.isFirst())
        .last(characterPage.isLast())
        .empty(filteredContent.isEmpty())
        .build();

    queryIndex.put(cacheKey, result);

    return result;
  }

  @Transactional(readOnly = true)
  public PageResponse<CharacterSummaryResponse> searchByName(
      String username,
      String name,
      Pageable pageable) {

    User user = findUserByUsername(username);
    Page<DndCharacter> characterPage =
        characterRepository.findByOwnerAndNameContainingIgnoreCase(user, name, pageable);

    Page<CharacterSummaryResponse> responsePage =
        characterPage.map(characterMapper::toSummaryResponse);

    return PageResponse.of(responsePage);
  }

  @Transactional(readOnly = true)
  public List<CharacterSummaryResponse> getRecentCharacters(String username) {
    User user = findUserByUsername(username);
    List<DndCharacter> characters = characterRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user);
    log.info("Retrieved {} recent characters for user '{}'", characters.size(), username);
    return characterMapper.toSummaryResponseList(characters);
  }

  @Transactional(readOnly = true)
  public CharacterResponse getById(Long id, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(id, username);
    return characterMapper.toResponse(character);
  }

  public CharacterResponse create(CharacterCreateRequest request, String username) {
    User user = findUserByUsername(username);

    DndCharacter character = characterMapper.toEntity(request);
    character.setOwner(user);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Character '{}' created for user '{}'", savedCharacter.getName(), username);

    invalidateUserCache(user.getId());

    return characterMapper.toResponse(savedCharacter);
  }

  public CharacterResponse update(Long id, CharacterUpdateRequest request, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(id, username);

    characterMapper.updateEntity(character, request);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Character '{}' updated", savedCharacter.getName());

    invalidateUserCache(character.getOwner().getId());

    return characterMapper.toResponse(savedCharacter);
  }

  public void delete(Long id, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(id, username);
    Long userId = character.getOwner().getId();

    characterRepository.delete(character);
    log.info("Character '{}' deleted", character.getName());

    invalidateUserCache(userId);
  }

  public CharacterResponse addEquipment(Long characterId, EquipmentRequest request,
      String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Equipment equipment = equipmentMapper.toEntity(request);
    character.addEquipment(equipment);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Equipment '{}' added to character '{}'", equipment.getName(), character.getName());

    invalidateUserCache(character.getOwner().getId());

    return characterMapper.toResponse(savedCharacter);
  }

  public CharacterResponse removeEquipment(Long characterId, Long equipmentId, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Equipment equipment = character.getEquipment().stream()
        .filter(e -> e.getId().equals(equipmentId))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

    character.removeEquipment(equipment);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Equipment removed from character '{}'", character.getName());

    invalidateUserCache(character.getOwner().getId());

    return characterMapper.toResponse(savedCharacter);
  }

  public CharacterResponse addSpell(Long characterId, Long spellId, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Spell spell = spellRepository.findById(spellId)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", spellId));

    character.addSpell(spell);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Spell '{}' added to character '{}'", spell.getName(), character.getName());

    invalidateUserCache(character.getOwner().getId());

    return characterMapper.toResponse(savedCharacter);
  }

  public CharacterResponse removeSpell(Long characterId, Long spellId, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Spell spell = spellRepository.findById(spellId)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", spellId));

    character.removeSpell(spell);

    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Spell '{}' removed from character '{}'", spell.getName(), character.getName());

    invalidateUserCache(character.getOwner().getId());

    return characterMapper.toResponse(savedCharacter);
  }

  public int restoreAllCharactersHitPoints(String username) {
    User user = findUserByUsername(username);

    int updatedCount = characterRepository.restoreAllCharactersHitPointsNative(user.getId());

    log.info("Restored hit points for {} characters belonging to user '{}'",
        updatedCount, username);

    invalidateUserCache(user.getId());

    return updatedCount;
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterSummary(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithOwnerAndClasses(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character summary for '{}'", character.getName());
    return characterMapper.toResponse(character);
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithSkills(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithSkills(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' with skills", character.getName());
    return characterMapper.toResponse(character);
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithSpells(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithSpells(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' with {} spells",
        character.getName(), character.getSpells().size());
    return characterMapper.toResponse(character);
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithEquipment(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithEquipment(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' with {} equipment items",
        character.getName(), character.getEquipment().size());
    return characterMapper.toResponse(character);
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithSavingThrows(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithSavingThrows(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' with saving throws", character.getName());
    return characterMapper.toResponse(character);
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterSheet(Long id, String username) {
    DndCharacter character = characterRepository.findByIdForCharacterSheet(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character sheet for '{}'", character.getName());
    return characterMapper.toResponse(character);
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterForCombat(Long id, String username) {
    DndCharacter character = characterRepository.findByIdForCombat(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' for combat", character.getName());
    return characterMapper.toResponse(character);
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterForSpellcasting(Long id, String username) {
    DndCharacter character = characterRepository.findByIdForSpellcasting(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    log.info("Retrieved character '{}' for spellcasting with {} spells",
        character.getName(), character.getSpells().size());
    return characterMapper.toResponse(character);
  }

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

    if (request.getName().contains("FAIL")) {
      log.error("Step 4: FAILURE! But nothing saved yet - transaction will rollback");
      throw new ResourceSaveFailureException("Simulated failure during starter pack creation");
    }

    log.info("Step 5: Saving character to database");
    DndCharacter savedCharacter = characterRepository.save(character);
    log.info("Step 6: Character '{}' saved with ID {}", savedCharacter.getName(),
        savedCharacter.getId());

    invalidateUserCache(user.getId());

    return characterMapper.toResponse(savedCharacter);
  }

  // ==================== Private Helper Methods ====================

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

  private void verifyOwnership(DndCharacter character, String username) {
    if (!character.getOwner().getUsername().equals(username)) {
      throw new UnauthorizedException("You don't have access to this character");
    }
  }

  private void invalidateUserCache(Long userId) {
    queryIndex.invalidateByUser(userId);
    log.debug("Cache invalidated for user {}", userId);
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

  @Transactional(readOnly = true)
  public PageResponse<CharacterSummaryResponse> findByComplexCriteria(
      String username,
      String className,
      Integer minClassLevel,
      SpellSchool spellSchool,
      Pageable pageable) {

    User user = findUserByUsername(username);

    Page<DndCharacter> characterPage = characterRepository.findByComplexCriteria(
        user.getId(),
        className,
        minClassLevel,
        spellSchool.name(),
        pageable
    );

    log.info("Found {} characters (page {}/{}) for complex criteria search",
        characterPage.getTotalElements(),
        characterPage.getNumber(),
        characterPage.getTotalPages());

    Page<CharacterSummaryResponse> responsePage =
        characterPage.map(characterMapper::toSummaryResponse);

    return PageResponse.of(responsePage);
  }
}
