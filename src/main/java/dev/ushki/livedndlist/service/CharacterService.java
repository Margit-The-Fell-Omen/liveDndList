package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.entity.character.Race;
import dev.ushki.livedndlist.entity.character.Spell;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
  private final CacheManager cacheManager;

  private static final String CHARACTER_RESOURCE = "Character";
  private static final String USER_RESOURCE = "User:";
  private static final String CHARACTERS_SUFFIX = ":Characters";
  private static final String CHARACTER_ID_SUFFIX = ":Character:";

  @Transactional(readOnly = true)
  public PageResponse<CharacterSummaryResponse> getAllByUsername(
      String username,
      Integer minLevel,
      Integer maxLevel,
      Pageable pageable) {

    CompositeKey key = new CompositeKey(minLevel, maxLevel, pageable.getPageNumber(),
        pageable.getPageSize(), pageable.getSort().toString());
    String namespace = USER_RESOURCE + username + CHARACTERS_SUFFIX;

    return cacheManager.get(namespace, key, () -> {
      User user = findUserByUsername(username);

      Page<DndCharacter> characterPage;

      characterPage = characterRepository.findAllByOwner(user, pageable);

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

      return PageResponse.<CharacterSummaryResponse>builder()
          .content(filteredContent)
          .pageNumber(characterPage.getNumber())
          .pageSize(characterPage.getSize())
          .totalElements(characterPage.getTotalElements())
          .totalPages(characterPage.getTotalPages())
          .first(characterPage.isFirst())
          .last(characterPage.isLast())
          .empty(filteredContent.isEmpty())
          .build();
    });
  }

  @Transactional(readOnly = true)
  public PageResponse<CharacterSummaryResponse> searchByName(
      String username,
      String name,
      Pageable pageable) {

    CompositeKey key = new CompositeKey(name, pageable.getPageNumber(), pageable.getPageSize(),
        pageable.getSort().toString());
    String namespace = USER_RESOURCE + username + CHARACTERS_SUFFIX;

    return cacheManager.get(namespace, key, () -> {
      User user = findUserByUsername(username);
      Page<DndCharacter> characterPage =
          characterRepository.findByOwnerAndNameContainingIgnoreCase(user, name, pageable);

      Page<CharacterSummaryResponse> responsePage =
          characterPage.map(characterMapper::toSummaryResponse);

      return PageResponse.of(responsePage);
    });
  }

  @Transactional(readOnly = true)
  public List<CharacterSummaryResponse> getRecentCharacters(String username) {
    CompositeKey key = new CompositeKey("recent");
    String namespace = USER_RESOURCE + username + CHARACTERS_SUFFIX;

    return cacheManager.get(namespace, key, () -> {
      User user = findUserByUsername(username);
      List<DndCharacter> characters = characterRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user);
      return characterMapper.toSummaryResponseList(characters);
    });
  }

  @Transactional(readOnly = true)
  public CharacterResponse getById(Long id, String username) {
    CompositeKey key = new CompositeKey("byId", id);
    String namespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + id;

    return cacheManager.get(namespace, key, () -> {
      DndCharacter character = characterRepository.findByIdFull(id)
          .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));
      verifyOwnership(character, username);
      return characterMapper.toResponse(character);
    });
  }

  public CharacterResponse create(CharacterCreateRequest request, String username) {
    User user = findUserByUsername(username);

    DndCharacter character = characterMapper.toEntity(request);
    character.setOwner(user);

    DndCharacter savedCharacter = characterRepository.save(character);

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

    return characterMapper.toResponse(savedCharacter);
  }

  public CharacterResponse update(Long id, CharacterUpdateRequest request, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(id, username);

    characterMapper.updateEntity(character, request);

    DndCharacter savedCharacter = characterRepository.save(character);

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

    return characterMapper.toResponse(savedCharacter);
  }

  public void delete(Long id, String username) {

    characterRepository.deleteAllSkillsByCharacterId(id);
    characterRepository.deleteAllClassesByCharacterId(id);
    characterRepository.deleteAllEquipmentByCharacterId(id);
    characterRepository.deleteAllSavingThrowsByCharacterId(id);
    characterRepository.deleteAllSpellsByCharacterId(id);

    characterRepository.deleteCharacterById(id);

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);
  }

  public CharacterResponse addEquipment(Long characterId, EquipmentRequest request,
      String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Equipment equipment = equipmentMapper.toEntity(request);
    character.addEquipment(equipment);

    DndCharacter savedCharacter = characterRepository.save(character);

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

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

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

    return characterMapper.toResponse(savedCharacter);
  }

  public CharacterResponse addSpell(Long characterId, Long spellId, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Spell spell = spellRepository.findById(spellId)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", spellId));

    character.addSpell(spell);

    DndCharacter savedCharacter = characterRepository.save(character);

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

    return characterMapper.toResponse(savedCharacter);
  }

  public CharacterResponse removeSpell(Long characterId, Long spellId, String username) {
    DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

    Spell spell = spellRepository.findById(spellId)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", spellId));

    character.removeSpell(spell);

    DndCharacter savedCharacter = characterRepository.save(character);

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

    return characterMapper.toResponse(savedCharacter);
  }

  public int restoreAllCharactersHitPoints(String username) {
    User user = findUserByUsername(username);

    int updatedCount = characterRepository.restoreAllCharactersHitPointsNative(user.getId());

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

    return updatedCount;
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterSummary(Long id, String username) {
    CompositeKey key = new CompositeKey("summary");
    String namespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + id;

    return cacheManager.get(namespace, key, () -> {
      DndCharacter character = characterRepository.findByIdWithOwnerAndClasses(id)
          .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

      verifyOwnership(character, username);
      return characterMapper.toResponse(character);
    });
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithSkills(Long id, String username) {
    CompositeKey key = new CompositeKey("skills");
    String namespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + id;

    return cacheManager.get(namespace, key, () -> {
      DndCharacter character = characterRepository.findByIdWithSkills(id)
          .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

      verifyOwnership(character, username);
      return characterMapper.toResponse(character);
    });
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithSpells(Long id, String username) {
    CompositeKey key = new CompositeKey("spells");
    String namespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + id;

    return cacheManager.get(namespace, key, () -> {
      DndCharacter character = characterRepository.findByIdWithSpells(id)
          .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

      verifyOwnership(character, username);
      return characterMapper.toResponse(character);
    });
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithEquipment(Long id, String username) {
    CompositeKey key = new CompositeKey("equipment");
    String namespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + id;

    return cacheManager.get(namespace, key, () -> {
      DndCharacter character = characterRepository.findByIdWithEquipment(id)
          .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

      verifyOwnership(character, username);
      return characterMapper.toResponse(character);
    });
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterWithSavingThrows(Long id, String username) {
    CompositeKey key = new CompositeKey("savingThrows");
    String namespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + id;

    return cacheManager.get(namespace, key, () -> {
      DndCharacter character = characterRepository.findByIdWithSavingThrows(id)
          .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

      verifyOwnership(character, username);
      return characterMapper.toResponse(character);
    });
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterSheet(Long id, String username) {
    CompositeKey key = new CompositeKey("sheet");
    String namespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + id;

    return cacheManager.get(namespace, key, () -> {
      DndCharacter character = characterRepository.findByIdForCharacterSheet(id)
          .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

      verifyOwnership(character, username);
      return characterMapper.toResponse(character);
    });
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterForCombat(Long id, String username) {
    CompositeKey key = new CompositeKey("combat");
    String namespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + id;

    return cacheManager.get(namespace, key, () -> {
      DndCharacter character = characterRepository.findByIdForCombat(id)
          .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

      verifyOwnership(character, username);
      return characterMapper.toResponse(character);
    });
  }

  @Transactional(readOnly = true)
  public CharacterResponse getCharacterForSpellcasting(Long id, String username) {
    CompositeKey key = new CompositeKey("spellcasting");
    String namespace = USER_RESOURCE + username + CHARACTER_ID_SUFFIX + id;

    return cacheManager.get(namespace, key, () -> {
      DndCharacter character = characterRepository.findByIdForSpellcasting(id)
          .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

      verifyOwnership(character, username);
      return characterMapper.toResponse(character);
    });
  }

  public CharacterResponse createWithStarterPack(CharacterCreateRequest request, String username,
      Pageable pageable) {
    User user = findUserByUsername(username);

    DndCharacter character = characterMapper.toEntity(request);
    character.setOwner(user);

    addStarterWeapon(character);
    addStarterArmor(character);
    addStarterPack(character);
    setStarterGold(character);

    if (request.getSpellcastingAbility() != null) {
      addStarterSpells(character, pageable);
    }

    if (request.getName().contains("FAIL")) {
      throw new ResourceSaveFailureException("Simulated failure during starter pack creation");
    }

    DndCharacter savedCharacter = characterRepository.save(character);

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

    return characterMapper.toResponse(savedCharacter);
  }

  private User findUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
  }

  private DndCharacter findCharacterWithOwnershipCheck(Long id, String username) {
    DndCharacter character = characterRepository.findByIdWithOwnerAndClasses(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));

    verifyOwnership(character, username);
    return character;
  }

  private void verifyOwnership(DndCharacter character, String username) {
    if (!character.getOwner().getUsername().equals(username)) {
      throw new UnauthorizedException("You don't have access to this character");
    }
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

  private void addStarterSpells(DndCharacter character, Pageable pageable) {
    List<Spell> cantrips = spellRepository.findByLevel(0, pageable);
    cantrips.stream()
        .limit(2)
        .forEach(character::addSpell);
  }

  @Transactional
  public CharacterResponse addEquipmentBulkWithTransaction(Long characterId,
      List<EquipmentRequest> requests, String username) {
    DndCharacter character = characterRepository.findByIdWithEquipment(characterId)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", characterId));
    verifyOwnership(character, username);

    for (EquipmentRequest request : requests) {
      if (request.getName().contains("FAIL")) {
        throw new ResourceSaveFailureException(
            "Simulated failure WITH transaction! Everything should roll back.");
      }
      character.addEquipment(equipmentMapper.toEntity(request));
      characterRepository.save(character);
    }

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);
    return characterMapper.toResponse(character);
  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public CharacterResponse addEquipmentBulkNoTransaction(Long characterId,
      List<EquipmentRequest> requests, String username) {
    DndCharacter character = characterRepository.findByIdWithEquipment(characterId)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", characterId));
    verifyOwnership(character, username);

    for (EquipmentRequest request : requests) {
      if (request.getName().contains("FAIL")) {
        throw new ResourceSaveFailureException(
            "Simulated failure WITHOUT transaction! Previous items remain in DB.");
      }
      character.addEquipment(equipmentMapper.toEntity(request));
      characterRepository.save(character);
    }

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);
    return characterMapper.toResponse(character);
  }
}
