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
import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.Equipment;
import dev.ushki.livedndlist.entity.dndCharacter.Spell;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.exceptions.UnauthorizedException;
import dev.ushki.livedndlist.mapper.CharacterMapper;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.EquipmentRepository;
import dev.ushki.livedndlist.repository.SpellRepository;
import dev.ushki.livedndlist.repository.UserRepository;
import dev.ushki.livedndlist.service.features.CharacterFeatureMaterializer;
import dev.ushki.livedndlist.service.features.CharacterPipelineService;
import dev.ushki.livedndlist.service.features.pipeline.ComputedCharacterState;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterService {

  private final CharacterRepository characterRepository;
  private final UserRepository userRepository;
  private final SpellRepository spellRepository;
  private final CharacterMapper characterMapper;
  private final EquipmentMapper equipmentMapper;
  private final CacheManager cacheManager;
  private final EquipmentRepository equipmentRepository;

  private static final String CHARACTER_RESOURCE = "Character";
  private static final String USER_RESOURCE = "User:";
  private static final String CHARACTERS_SUFFIX = ":Characters";
  private static final String CHARACTER_ID_SUFFIX = ":Character:";
  private final CharacterPipelineService characterPipelineService;
  private final CharacterFeatureMaterializer characterFeatureMaterializer;

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
      ComputedCharacterState state = characterPipelineService.compute(id);
      return characterMapper.toResponse(character, state);
    });
  }

  public CharacterResponse create(CharacterCreateRequest request, String username) {
    User user = findUserByUsername(username);

    DndCharacter character = characterMapper.toEntity(request);
    character.setOwner(user);

    DndCharacter savedCharacter = characterRepository.save(character);

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

    characterFeatureMaterializer.syncFeatures(savedCharacter.getId());

    ComputedCharacterState state = characterPipelineService.compute(savedCharacter.getId());
    return characterMapper.toResponse(savedCharacter, state);
  }

  public CharacterResponse update(Long id, CharacterUpdateRequest request, String username) {
    DndCharacter character = characterRepository.findByIdFull(id)
        .orElseThrow(() -> new ResourceNotFoundException(CHARACTER_RESOURCE, "id", id));
    verifyOwnership(character, username);
    log.info("Found character: {}", character);
    characterMapper.updateEntity(character, request);
    log.info("Found request: {}", request);

    DndCharacter savedCharacter = characterRepository.save(character);

    cacheManager.invalidateByPrefix(USER_RESOURCE + username);

    if (request.getRaceKey() != null
        || request.getDndClassLevels() != null
        || request.getBackgroundKey() != null
        || request.getExperiencePoints() != null) {
      characterFeatureMaterializer.syncFeatures(savedCharacter.getId());
    }

    ComputedCharacterState state = characterPipelineService.compute(savedCharacter.getId());
    return characterMapper.toResponse(savedCharacter, state);
  }

  @Transactional
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
      ComputedCharacterState state = characterPipelineService.compute(id);
      return characterMapper.toResponse(character, state);
    });
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

}
