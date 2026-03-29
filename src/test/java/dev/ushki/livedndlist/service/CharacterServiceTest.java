package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.cache.CacheManager;
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
import dev.ushki.livedndlist.enums.Role;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.exceptions.UnauthorizedException;
import dev.ushki.livedndlist.mapper.CharacterMapper;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.SpellRepository;
import dev.ushki.livedndlist.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

  @Mock
  private CharacterRepository characterRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private SpellRepository spellRepository;

  @Mock
  private CharacterMapper characterMapper;

  @Mock
  private EquipmentMapper equipmentMapper;

  private CharacterService characterService;

  private User testUser;
  private User otherUser;
  private DndCharacter testCharacter;
  private DndCharacter elfCharacter;
  private CharacterResponse testCharacterResponse;
  private CharacterSummaryResponse testCharacterSummary;
  private CharacterSummaryResponse elfCharacterSummary;
  private Pageable defaultPageable;

  @BeforeEach
  void setUp() {
    CacheManager cacheManager = new CacheManager();

    characterService = new CharacterService(characterRepository, userRepository, spellRepository,
        characterMapper, equipmentMapper, cacheManager);

    testUser = User.builder()
        .id(1L)
        .username("testuser")
        .email("test@test.com")
        .password("password")
        .role(Role.ROLE_USER)
        .enabled(true)
        .build();

    otherUser = User.builder()
        .id(2L)
        .username("otheruser")
        .email("other@test.com")
        .password("password")
        .role(Role.ROLE_USER)
        .enabled(true)
        .build();

    testCharacter = DndCharacter.builder()
        .id(1L)
        .owner(testUser)
        .name("Gandalf")
        //.race(CharacterRace.HUMAN)
        .maxHitPoints(45)
        .currentHitPoints(45)
        .classes(new HashSet<>())
        .skills(new HashSet<>())
        .equipment(new HashSet<>())
        .spells(new HashSet<>())
        .build();

    elfCharacter = DndCharacter.builder()
        .id(2L)
        .owner(testUser)
        .name("Legolas")
        //.race(CharacterRace.ELF)
        .maxHitPoints(30)
        .currentHitPoints(30)
        .classes(new HashSet<>())
        .skills(new HashSet<>())
        .equipment(new HashSet<>())
        .spells(new HashSet<>())
        .build();

    testCharacterResponse = CharacterResponse.builder()
        .id(1L)
        .name("Gandalf")
        //.race(CharacterRace.HUMAN)
        .maxHitPoints(45)
        .currentHitPoints(45)
        .build();

    testCharacterSummary = CharacterSummaryResponse.builder()
        .id(1L)
        .name("Gandalf")
        //.race(CharacterRace.HUMAN)
        .totalLevel(5)
        .build();

    elfCharacterSummary = CharacterSummaryResponse.builder()
        .id(2L)
        .name("Legolas")
        //.race(CharacterRace.ELF)
        .totalLevel(3)
        .build();

    defaultPageable = PageRequest.of(0, 20, Sort.by("updatedAt").descending());
  }

  @Nested
  @DisplayName("Caching Behavior Tests")
  class CachingTests {

    @Test
    @DisplayName("Should return from cache on second call for getById")
    void shouldReturnFromCacheOnSecondCallGetById() {
      when(characterRepository.findByIdFull(1L)).thenReturn(Optional.of(testCharacter));
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      characterService.getById(1L, "testuser");
      characterService.getById(1L, "testuser");

      verify(characterRepository, times(1)).findByIdFull(1L);
    }

    @Test
    @DisplayName("Should return from cache on second call for getAllByUsername")
    void shouldReturnFromCacheOnSecondCallGetAll() {
      Page<DndCharacter> characterPage = new PageImpl<>(List.of(testCharacter), defaultPageable, 1);

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterRepository.findAllByOwner(eq(testUser), any(Pageable.class))).thenReturn(
          characterPage);
      when(characterMapper.toSummaryResponse(testCharacter)).thenReturn(testCharacterSummary);

      characterService.getAllByUsername("testuser", null, null, null, defaultPageable);
      characterService.getAllByUsername("testuser", null, null, null, defaultPageable);

      verify(characterRepository, times(1)).findAllByOwner(eq(testUser), any(Pageable.class));
    }

    @Test
    @DisplayName("Should invalidate cache on create")
    void shouldInvalidateCacheOnCreate() {
      when(characterRepository.findByIdFull(1L)).thenReturn(Optional.of(testCharacter));
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      characterService.getById(1L, "testuser");

      CharacterCreateRequest request = CharacterCreateRequest.builder().name("New").build();
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterMapper.toEntity(request)).thenReturn(testCharacter);
      when(characterRepository.save(any(DndCharacter.class))).thenReturn(testCharacter);

      characterService.create(request, "testuser");
      characterService.getById(1L, "testuser");

      verify(characterRepository, times(2)).findByIdFull(1L);
    }

    @Test
    @DisplayName("Should invalidate cache on update")
    void shouldInvalidateCacheOnUpdate() {
      when(characterRepository.findByIdFull(1L)).thenReturn(Optional.of(testCharacter));
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      characterService.getById(1L, "testuser");

      CharacterUpdateRequest request = CharacterUpdateRequest.builder().name("Updated").build();
      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));
      when(characterRepository.save(testCharacter)).thenReturn(testCharacter);

      characterService.update(1L, request, "testuser");
      characterService.getById(1L, "testuser");

      verify(characterRepository, times(2)).findByIdFull(1L);
    }

    @Test
    @DisplayName("Should invalidate cache on delete")
    void shouldInvalidateCacheOnDelete() {
      when(characterRepository.findByIdFull(1L)).thenReturn(Optional.of(testCharacter));
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      characterService.getById(1L, "testuser");
      characterService.delete(1L, "testuser");
      characterService.getById(1L, "testuser");

      verify(characterRepository, times(2)).findByIdFull(1L);
    }

    @Test
    @DisplayName("Should invalidate cache when adding equipment")
    void shouldInvalidateCacheOnAddEquipment() {
      when(characterRepository.findByIdFull(1L)).thenReturn(Optional.of(testCharacter));
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      characterService.getById(1L, "testuser");

      EquipmentRequest request = EquipmentRequest.builder().name("Sword").build();
      Equipment equipment = Equipment.builder().name("Sword").build();
      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));
      when(equipmentMapper.toEntity(request)).thenReturn(equipment);
      when(characterRepository.save(testCharacter)).thenReturn(testCharacter);

      characterService.addEquipment(1L, request, "testuser");
      characterService.getById(1L, "testuser");

      verify(characterRepository, times(2)).findByIdFull(1L);
    }
  }

  @Nested
  @DisplayName("Get Operations")
  class GetOperations {

    @Test
    @DisplayName("Should get all characters by username with pagination")
    void shouldGetAllCharactersByUsernameWithPagination() {
      Page<DndCharacter> characterPage = new PageImpl<>(
          List.of(testCharacter),
          defaultPageable,
          1
      );

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterRepository.findAllByOwner(eq(testUser), any(Pageable.class)))
          .thenReturn(characterPage);
      when(characterMapper.toSummaryResponse(testCharacter))
          .thenReturn(testCharacterSummary);

      PageResponse<CharacterSummaryResponse> result = characterService.getAllByUsername(
          "testuser", null, null, null, defaultPageable);

      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().getName()).isEqualTo("Gandalf");
      assertThat(result.getPageNumber()).isZero();
      assertThat(result.getTotalElements()).isEqualTo(1);
      verify(characterRepository).findAllByOwner(eq(testUser), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter characters by race with pagination")
    void shouldFilterCharactersByRaceWithPagination() {
      Page<DndCharacter> characterPage = new PageImpl<>(
          List.of(elfCharacter),
          defaultPageable,
          1
      );

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterRepository.findByOwnerAndRace(eq(testUser), eq(CharacterRace.ELF),
          any(Pageable.class)))
          .thenReturn(characterPage);
      when(characterMapper.toSummaryResponse(elfCharacter))
          .thenReturn(elfCharacterSummary);

      PageResponse<CharacterSummaryResponse> result = characterService.getAllByUsername(
          "testuser", CharacterRace.ELF, null, null, defaultPageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().getRace()).isEqualTo(CharacterRace.ELF);
    }

    @Test
    @DisplayName("Should search characters by name with pagination")
    void shouldSearchCharactersByNameWithPagination() {
      Page<DndCharacter> characterPage = new PageImpl<>(
          List.of(testCharacter),
          defaultPageable,
          1
      );

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterRepository.findByOwnerAndNameContainingIgnoreCase(
          eq(testUser), eq("Gandalf"), any(Pageable.class)))
          .thenReturn(characterPage);
      when(characterMapper.toSummaryResponse(testCharacter))
          .thenReturn(testCharacterSummary);

      PageResponse<CharacterSummaryResponse> result = characterService.searchByName(
          "testuser", "Gandalf", defaultPageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().getName()).isEqualTo("Gandalf");
      verify(characterRepository).findByOwnerAndNameContainingIgnoreCase(
          eq(testUser), eq("Gandalf"), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when no matches found")
    void shouldReturnEmptyPageWhenNoMatches() {
      Page<DndCharacter> emptyPage = new PageImpl<>(
          List.of(),
          defaultPageable,
          0
      );

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterRepository.findByOwnerAndNameContainingIgnoreCase(
          eq(testUser), eq("NonExistent"), any(Pageable.class)))
          .thenReturn(emptyPage);

      PageResponse<CharacterSummaryResponse> result = characterService.searchByName(
          "testuser", "NonExistent", defaultPageable);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.isEmpty()).isTrue();
      assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should get recent characters (top 5)")
    void shouldGetRecentCharacters() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterRepository.findTop5ByOwnerOrderByUpdatedAtDesc(testUser))
          .thenReturn(List.of(testCharacter));
      when(characterMapper.toSummaryResponseList(anyList()))
          .thenReturn(List.of(testCharacterSummary));

      List<CharacterSummaryResponse> result = characterService.getRecentCharacters("testuser");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getName()).isEqualTo("Gandalf");
      verify(characterRepository).findTop5ByOwnerOrderByUpdatedAtDesc(testUser);
    }

    @Test
    @DisplayName("Should get character by ID")
    void shouldGetCharacterById() {
      when(characterRepository.findByIdFull(1L)).thenReturn(Optional.of(testCharacter));
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      CharacterResponse result = characterService.getById(1L, "testuser");

      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("Gandalf");
      verify(characterMapper).toResponse(testCharacter);
    }

    @Test
    @DisplayName("Should throw exception when character not found")
    void shouldThrowExceptionWhenCharacterNotFound() {
      when(characterRepository.findByIdFull(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> characterService.getById(999L, "testuser"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Character")
          .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should throw exception when user doesn't own character")
    void shouldThrowExceptionWhenUserDoesntOwnCharacter() {
      DndCharacter otherCharacter = DndCharacter.builder()
          .id(2L)
          .owner(otherUser)
          .name("Other")
          //.race(CharacterRace.ELF)
          .build();

      when(characterRepository.findByIdFull(2L)).thenReturn(Optional.of(otherCharacter));

      assertThatThrownBy(() -> characterService.getById(2L, "testuser"))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessageContaining("don't have access");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> characterService.getAllByUsername(
          "nonexistent", null, null, null, defaultPageable))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User")
          .hasMessageContaining("nonexistent");
    }
  }

  @Nested
  @DisplayName("Create and Update Operations")
  class CreateUpdateOperations {

    @Test
    @DisplayName("Should create character successfully")
    void shouldCreateCharacterSuccessfully() {
      CharacterCreateRequest request = CharacterCreateRequest.builder()
          .name("Legolas")
          //.race(CharacterRace.ELF)
          //.className("Ranger")
          .build();

      DndCharacter newCharacter = DndCharacter.builder()
          .name("Legolas")
          //.race(CharacterRace.ELF)
          .build();

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterMapper.toEntity(request)).thenReturn(newCharacter);
      when(characterRepository.save(any(DndCharacter.class))).thenReturn(newCharacter);
      when(characterMapper.toResponse(newCharacter)).thenReturn(testCharacterResponse);

      CharacterResponse result = characterService.create(request, "testuser");

      assertThat(result).isNotNull();
      verify(characterRepository).save(any(DndCharacter.class));
    }

    @Test
    @DisplayName("Should update character successfully")
    void shouldUpdateCharacterSuccessfully() {
      CharacterUpdateRequest request = CharacterUpdateRequest.builder()
          .name("Gandalf the White")
          .maxHitPoints(50)
          .build();

      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));
      when(characterRepository.save(testCharacter)).thenReturn(testCharacter);
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      CharacterResponse result = characterService.update(1L, request, "testuser");

      assertThat(result).isNotNull();
      verify(characterMapper).updateEntity(testCharacter, request);
      verify(characterRepository).save(testCharacter);
    }

    @Test
    @DisplayName("Should delete character successfully")
    void shouldDeleteCharacterSuccessfully() {
      characterService.delete(1L, "testuser");

      verify(characterRepository).deleteAllSkillsByCharacterId(1L);
      verify(characterRepository).deleteAllClassesByCharacterId(1L);
      verify(characterRepository).deleteAllEquipmentByCharacterId(1L);
      verify(characterRepository).deleteAllSavingThrowsByCharacterId(1L);
      verify(characterRepository).deleteAllSpellsByCharacterId(1L);
      verify(characterRepository).deleteCharacterById(1L);
    }

    @Test
    @DisplayName("Should not allow other user to update character")
    void shouldNotAllowOtherUserToUpdateCharacter() {
      CharacterUpdateRequest request = CharacterUpdateRequest.builder()
          .name("Hacked Name")
          .build();

      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));

      assertThatThrownBy(() -> characterService.update(1L, request, "otheruser"))
          .isInstanceOf(UnauthorizedException.class);

      verify(characterRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Equipment Operations")
  class EquipmentOperations {

    @Test
    @DisplayName("Should add equipment to character")
    void shouldAddEquipmentToCharacter() {
      EquipmentRequest request = EquipmentRequest.builder()
          .name("Longsword")
          .type(EquipmentType.WEAPON)
          .build();

      Equipment equipment = Equipment.builder()
          .name("Longsword")
          .type(EquipmentType.WEAPON)
          .build();

      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));
      when(equipmentMapper.toEntity(request)).thenReturn(equipment);
      when(characterRepository.save(testCharacter)).thenReturn(testCharacter);
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      CharacterResponse result = characterService.addEquipment(1L, request, "testuser");

      assertThat(result).isNotNull();
      verify(characterRepository).save(testCharacter);
    }

    @Test
    @DisplayName("Should remove equipment from character")
    void shouldRemoveEquipmentFromCharacter() {
      Equipment equipment = Equipment.builder()
          .id(1L)
          .name("Longsword")
          .build();

      testCharacter.getEquipment().add(equipment);

      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));
      when(characterRepository.save(testCharacter)).thenReturn(testCharacter);
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      CharacterResponse result = characterService.removeEquipment(1L, 1L, "testuser");

      assertThat(result).isNotNull();
      verify(characterRepository).save(testCharacter);
    }

    @Test
    @DisplayName("Should throw exception when equipment not found")
    void shouldThrowExceptionWhenEquipmentNotFound() {
      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));

      assertThatThrownBy(() -> characterService.removeEquipment(1L, 999L, "testuser"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Equipment");
    }
  }

  @Nested
  @DisplayName("Spell Operations")
  class SpellOperations {

    @Test
    @DisplayName("Should add spell to character")
    void shouldAddSpellToCharacter() {
      Spell spell = Spell.builder()
          .id(1L)
          .name("Fireball")
          .level(3)
          .school(SpellSchool.EVOCATION)
          .build();

      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));
      when(spellRepository.findById(1L)).thenReturn(Optional.of(spell));
      when(characterRepository.save(testCharacter)).thenReturn(testCharacter);
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      CharacterResponse result = characterService.addSpell(1L, 1L, "testuser");

      assertThat(result).isNotNull();
      verify(characterRepository).save(testCharacter);
    }

    @Test
    @DisplayName("Should remove spell from character")
    void shouldRemoveSpellFromCharacter() {
      Spell spell = Spell.builder()
          .id(1L)
          .name("Fireball")
          .level(3)
          .build();

      testCharacter.getSpells().add(spell);

      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));
      when(spellRepository.findById(1L)).thenReturn(Optional.of(spell));
      when(characterRepository.save(testCharacter)).thenReturn(testCharacter);
      when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

      CharacterResponse result = characterService.removeSpell(1L, 1L, "testuser");

      assertThat(result).isNotNull();
      verify(characterRepository).save(testCharacter);
    }

    @Test
    @DisplayName("Should throw exception when spell not found")
    void shouldThrowExceptionWhenSpellNotFound() {
      when(characterRepository.findByIdWithOwnerAndClasses(1L)).thenReturn(
          Optional.of(testCharacter));
      when(spellRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> characterService.addSpell(1L, 999L, "testuser"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Spell");
    }
  }

  @Nested
  @DisplayName("Pagination Edge Cases")
  class PaginationEdgeCases {

    @Test
    @DisplayName("Should handle empty page correctly")
    void shouldHandleEmptyPage() {
      Page<DndCharacter> emptyPage = Page.empty(defaultPageable);

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterRepository.findAllByOwner(eq(testUser), any(Pageable.class)))
          .thenReturn(emptyPage);

      PageResponse<CharacterSummaryResponse> result = characterService.getAllByUsername(
          "testuser", null, null, null, defaultPageable);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.isEmpty()).isTrue();
      assertThat(result.getTotalElements()).isZero();
      assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("Should handle multiple pages correctly")
    void shouldHandleMultiplePages() {
      Pageable pageRequest = PageRequest.of(1, 10, Sort.by("updatedAt").descending());
      Page<DndCharacter> secondPage = new PageImpl<>(
          List.of(testCharacter),
          pageRequest,
          25
      );

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(characterRepository.findAllByOwner(eq(testUser), any(Pageable.class)))
          .thenReturn(secondPage);
      when(characterMapper.toSummaryResponse(testCharacter))
          .thenReturn(testCharacterSummary);

      PageResponse<CharacterSummaryResponse> result = characterService.getAllByUsername(
          "testuser", null, null, null, pageRequest);

      assertThat(result.getPageNumber()).isEqualTo(1);
      assertThat(result.getPageSize()).isEqualTo(10);
      assertThat(result.getTotalElements()).isEqualTo(25);
      assertThat(result.getTotalPages()).isEqualTo(3);
      assertThat(result.isFirst()).isFalse();
      assertThat(result.isLast()).isFalse();
    }
  }
}
