package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.entity.dndCharacter.Spell;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.SpellMapper;
import dev.ushki.livedndlist.repository.SpellRepository;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class SpellServiceTest {

  @Mock
  private SpellRepository spellRepository;

  @Mock
  private SpellMapper spellMapper;

  @Mock
  private Open5eApiClient open5eApiClient;

  @Mock
  private SyncMetrics syncMetrics;

  private SpellService spellService;

  private Spell fireball;
  private Spell magicMissile;
  private Spell shield;
  private Spell heal;
  private Spell detectMagic;
  private SpellResponse fireballResponse;
  private SpellResponse magicMissileResponse;
  private SpellResponse shieldResponse;
  private SpellResponse healResponse;
  private SpellResponse detectMagicResponse;
  private SpellRequest testSpellRequest;

  @BeforeEach
  void setUp() {
    CacheManager cacheManager = new CacheManager();

    spellService = new SpellService(spellRepository, spellMapper, cacheManager, open5eApiClient,
        syncMetrics);

    fireball = Spell.builder()
        .id(1L)
        .name("Fireball")
        .level(3)
        .school(SpellSchool.EVOCATION)
        .castingTime("1 action")
        .range("150 feet")
        .components("V, S, M")
        .duration("Instantaneous")
        .description("A bright streak flashes...")
        .ritual(false)
        .concentration(false)
        .build();

    magicMissile = Spell.builder()
        .id(2L)
        .name("Magic Missile")
        .level(1)
        .school(SpellSchool.EVOCATION)
        .castingTime("1 action")
        .range("120 feet")
        .components("V, S")
        .duration("Instantaneous")
        .ritual(false)
        .concentration(false)
        .build();

    shield = Spell.builder()
        .id(3L)
        .name("Shield")
        .level(1)
        .school(SpellSchool.ABJURATION)
        .castingTime("1 reaction")
        .range("Self")
        .components("V, S")
        .duration("1 round")
        .ritual(false)
        .concentration(false)
        .build();

    heal = Spell.builder()
        .id(4L)
        .name("Heal")
        .level(6)
        .school(SpellSchool.EVOCATION)
        .castingTime("1 action")
        .range("60 feet")
        .components("V, S")
        .duration("Instantaneous")
        .ritual(false)
        .concentration(true)
        .build();

    detectMagic = Spell.builder()
        .id(5L)
        .name("Detect Magic")
        .level(1)
        .school(SpellSchool.DIVINATION)
        .castingTime("1 action")
        .range("Self")
        .components("V, S")
        .duration("Concentration, up to 10 minutes")
        .ritual(true)
        .concentration(true)
        .build();

    fireballResponse = SpellResponse.builder()
        .id(1L)
        .name("Fireball")
        .level(3)
        .school(SpellSchool.EVOCATION)
        .castingTime("1 action")
        .range("150 feet")
        .ritual(false)
        .concentration(false)
        .build();

    magicMissileResponse = SpellResponse.builder()
        .id(2L)
        .name("Magic Missile")
        .level(1)
        .school(SpellSchool.EVOCATION)
        .castingTime("1 action")
        .range("120 feet")
        .ritual(false)
        .concentration(false)
        .build();

    shieldResponse = SpellResponse.builder()
        .id(3L)
        .name("Shield")
        .level(1)
        .school(SpellSchool.ABJURATION)
        .castingTime("1 reaction")
        .range("Self")
        .ritual(false)
        .concentration(false)
        .build();

    healResponse = SpellResponse.builder()
        .id(4L)
        .name("Heal")
        .level(6)
        .school(SpellSchool.EVOCATION)
        .castingTime("1 action")
        .range("60 feet")
        .ritual(false)
        .concentration(true)
        .build();

    detectMagicResponse = SpellResponse.builder()
        .id(5L)
        .name("Detect Magic")
        .level(1)
        .school(SpellSchool.DIVINATION)
        .castingTime("1 action")
        .range("Self")
        .ritual(true)
        .concentration(true)
        .build();

    testSpellRequest = SpellRequest.builder()
        .name("Fireball")
        .level(3)
        .school(SpellSchool.EVOCATION)
        .castingTime("1 action")
        .range("150 feet")
        .components("V, S, M")
        .duration("Instantaneous")
        .build();
  }

  @Nested
  @DisplayName("Caching Behavior Tests")
  class CachingTests {

    @Test
    @DisplayName("Should return from cache on second call for getAllSpells")
    void shouldReturnFromCacheOnSecondCallGetAllSpells() {
      when(spellRepository.findAll(any(Sort.class))).thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.getAllSpells(null, null, null, null, null, "name", "asc");
      spellService.getAllSpells(null, null, null, null, null, "name", "asc");

      verify(spellRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Should return from cache on second call for getById")
    void shouldReturnFromCacheOnSecondCallGetById() {
      when(spellRepository.findById(1L)).thenReturn(Optional.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.getById(1L);
      spellService.getById(1L);

      verify(spellRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return from cache on second call for searchByName")
    void shouldReturnFromCacheOnSecondCallSearchByName() {
      Pageable pageable = Pageable.unpaged();
      when(spellRepository.findByNameContainingIgnoreCase("fire", pageable))
          .thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.searchByName("fire", null, null, pageable);
      spellService.searchByName("fire", null, null, pageable);

      verify(spellRepository, times(1)).findByNameContainingIgnoreCase("fire", pageable);
    }

    @Test
    @DisplayName("Should invalidate cache on create")
    void shouldInvalidateCacheOnCreate() {
      when(spellRepository.findAll(any(Sort.class))).thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.getAllSpells(null, null, null, null, null, "name", "asc");

      when(spellRepository.existsByName("New Spell")).thenReturn(false);
      SpellRequest newRequest = SpellRequest.builder().name("New Spell").build();
      Spell newSpell = Spell.builder().name("New Spell").build();
      when(spellMapper.toEntity(newRequest)).thenReturn(newSpell);
      when(spellRepository.save(newSpell)).thenReturn(newSpell);
      when(spellMapper.toResponse(newSpell)).thenReturn(
          SpellResponse.builder().name("New Spell").build());

      spellService.create(newRequest);
      spellService.getAllSpells(null, null, null, null, null, "name", "asc");

      verify(spellRepository, times(2)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Should invalidate cache on update")
    void shouldInvalidateCacheOnUpdate() {
      when(spellRepository.findById(1L)).thenReturn(Optional.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.getById(1L);

      when(spellRepository.save(fireball)).thenReturn(fireball);
      spellService.update(1L, testSpellRequest);

      spellService.getById(1L);

      verify(spellRepository, times(3)).findById(1L);
    }

    @Test
    @DisplayName("Should invalidate cache on delete")
    void shouldInvalidateCacheOnDelete() {
      when(spellRepository.findById(1L)).thenReturn(Optional.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.getById(1L);

      when(spellRepository.existsById(1L)).thenReturn(true);
      spellService.delete(1L);

      spellService.getById(1L);

      verify(spellRepository, times(2)).findById(1L);
    }

    @Test
    @DisplayName("Should invalidate cache on bulk create")
    void shouldInvalidateCacheOnBulkCreate() {
      when(spellRepository.findAll(any(Sort.class))).thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.getAllSpells(null, null, null, null, null, "name", "asc");

      SpellRequest newRequest = SpellRequest.builder().name("New Spell").build();
      Spell newSpell = Spell.builder().name("New Spell").build();
      when(spellRepository.existsByName("New Spell")).thenReturn(false);
      when(spellMapper.toEntity(newRequest)).thenReturn(newSpell);
      when(spellRepository.saveAll(List.of(newSpell))).thenReturn(List.of(newSpell));
      when(spellMapper.toResponse(newSpell)).thenReturn(
          SpellResponse.builder().name("New Spell").build());

      spellService.createBulk(List.of(newRequest));
      spellService.getAllSpells(null, null, null, null, null, "name", "asc");

      verify(spellRepository, times(2)).findAll(any(Sort.class));
    }
  }

  @Nested
  @DisplayName("Get All Spells")
  class GetAllSpellsTests {

    @Test
    @DisplayName("Should get all spells with default sorting")
    void shouldGetAllSpells() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, magicMissile, shield));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);
      when(spellMapper.toResponse(magicMissile)).thenReturn(magicMissileResponse);
      when(spellMapper.toResponse(shield)).thenReturn(shieldResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, null, null, null, null, "name", "asc");

      assertThat(result).hasSize(3);
      verify(spellRepository).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Should filter spells by school")
    void shouldFilterSpellsBySchool() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, magicMissile, shield));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);
      when(spellMapper.toResponse(magicMissile)).thenReturn(magicMissileResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          SpellSchool.EVOCATION, null, null, null, null, "name", "asc");

      assertThat(result).hasSize(2).allMatch(s -> s.getSchool() == SpellSchool.EVOCATION);
    }

    @Test
    @DisplayName("Should filter spells by minimum level")
    void shouldFilterSpellsByMinLevel() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, magicMissile, shield));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, 3, null, null, null, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getLevel()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should filter spells by maximum level")
    void shouldFilterSpellsByMaxLevel() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, magicMissile, shield));
      when(spellMapper.toResponse(magicMissile)).thenReturn(magicMissileResponse);
      when(spellMapper.toResponse(shield)).thenReturn(shieldResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, null, 1, null, null, "name", "asc");

      assertThat(result).hasSize(2).allMatch(s -> s.getLevel() <= 1);
    }

    @Test
    @DisplayName("Should filter spells by level range")
    void shouldFilterSpellsByLevelRange() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, magicMissile, shield, heal));
      when(spellMapper.toResponse(magicMissile)).thenReturn(magicMissileResponse);
      when(spellMapper.toResponse(shield)).thenReturn(shieldResponse);
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, 1, 3, null, null, "name", "asc");

      assertThat(result).hasSize(3).allMatch(s -> s.getLevel() >= 1 && s.getLevel() <= 3);
    }

    @Test
    @DisplayName("Should filter spells by ritual true")
    void shouldFilterSpellsByRitualTrue() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, detectMagic));
      when(spellMapper.toResponse(detectMagic)).thenReturn(detectMagicResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, null, null, true, null, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().isRitual()).isTrue();
    }

    @Test
    @DisplayName("Should filter spells by ritual false")
    void shouldFilterSpellsByRitualFalse() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, detectMagic));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, null, null, false, null, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().isRitual()).isFalse();
    }

    @Test
    @DisplayName("Should filter spells by concentration true")
    void shouldFilterSpellsByConcentrationTrue() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, heal));
      when(spellMapper.toResponse(heal)).thenReturn(healResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, null, null, null, true, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().isConcentration()).isTrue();
    }

    @Test
    @DisplayName("Should filter spells by concentration false")
    void shouldFilterSpellsByConcentrationFalse() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, heal));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, null, null, null, false, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().isConcentration()).isFalse();
    }

    @Test
    @DisplayName("Should apply all filters together")
    void shouldApplyAllFiltersTogether() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, magicMissile, shield, heal, detectMagic));
      when(spellMapper.toResponse(magicMissile)).thenReturn(magicMissileResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          SpellSchool.EVOCATION, 1, 2, false, false, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getName()).isEqualTo("Magic Missile");
    }

    @Test
    @DisplayName("Should sort ascending when specified")
    void shouldSortAscending() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.getAllSpells(
          null, null, null, null, null, "level", "asc");

      verify(spellRepository).findAll(Sort.by("level").ascending());
    }

    @Test
    @DisplayName("Should sort descending when specified")
    void shouldSortDescending() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.getAllSpells(
          null, null, null, null, null, "level", "desc");

      verify(spellRepository).findAll(Sort.by("level").descending());
    }

    @Test
    @DisplayName("Should handle uppercase sort direction")
    void shouldHandleUppercaseSortDirection() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      spellService.getAllSpells(
          null, null, null, null, null, "level", "ASC");

      verify(spellRepository).findAll(Sort.by("level").ascending());
    }

    @Test
    @DisplayName("Should return empty list when no spells match filters")
    void shouldReturnEmptyListWhenNoSpellsMatchFilters() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball));

      List<SpellResponse> result = spellService.getAllSpells(
          SpellSchool.NECROMANCY, null, null, null, null, "name", "asc");

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("Get Spell By ID")
  class GetSpellByIdTests {

    @Test
    @DisplayName("Should get spell by ID")
    void shouldGetSpellById() {
      when(spellRepository.findById(1L)).thenReturn(Optional.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      SpellResponse result = spellService.getById(1L);

      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("Fireball");
      verify(spellRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when spell not found by ID")
    void shouldThrowExceptionWhenSpellNotFoundById() {
      when(spellRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> spellService.getById(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Spell")
          .hasMessageContaining("999");
    }
  }

  @Nested
  @DisplayName("Search Spells")
  class SearchSpellsTests {

    @Test
    @DisplayName("Should search spells by name")
    void shouldSearchSpellsByName() {
      Pageable pageable = Pageable.unpaged();

      when(spellRepository.findByNameContainingIgnoreCase("fire", pageable))
          .thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      List<SpellResponse> result = spellService.searchByName("fire", null, null, pageable);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getName()).isEqualTo("Fireball");
      verify(spellRepository).findByNameContainingIgnoreCase("fire", pageable);
    }

    @Test
    @DisplayName("Should search spells by name and filter by school")
    void shouldSearchSpellsByNameAndFilterBySchool() {
      Pageable pageable = Pageable.unpaged();

      when(spellRepository.findByNameContainingIgnoreCase("fire", pageable))
          .thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      List<SpellResponse> result = spellService.searchByName("fire", SpellSchool.EVOCATION, null,
          pageable);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getSchool()).isEqualTo(SpellSchool.EVOCATION);
    }

    @Test
    @DisplayName("Should filter out non-matching school in search")
    void shouldFilterOutNonMatchingSchoolInSearch() {
      Pageable pageable = Pageable.unpaged();

      when(spellRepository.findByNameContainingIgnoreCase("fire", pageable))
          .thenReturn(List.of(fireball));

      List<SpellResponse> result = spellService.searchByName("fire", SpellSchool.ABJURATION, null,
          pageable);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should search spells by name and filter by max level")
    void shouldSearchSpellsByNameAndFilterByMaxLevel() {
      Pageable pageable = Pageable.unpaged();
      Spell fireBolt = Spell.builder()
          .id(6L)
          .name("Fire Bolt")
          .school(SpellSchool.EVOCATION)
          .level(0)
          .ritual(false)
          .concentration(false)
          .build();
      SpellResponse fireBoltResponse = SpellResponse.builder()
          .id(6L)
          .name("Fire Bolt")
          .level(0)
          .school(SpellSchool.EVOCATION)
          .build();

      when(spellRepository.findByNameContainingIgnoreCase("fire", pageable))
          .thenReturn(List.of(fireBolt, fireball));
      when(spellMapper.toResponse(fireBolt)).thenReturn(fireBoltResponse);

      List<SpellResponse> result = spellService.searchByName("fire", null, 2, pageable);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getLevel()).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should search with all filters applied")
    void shouldSearchWithAllFiltersApplied() {
      Pageable pageable = Pageable.unpaged();
      Spell fireBolt = Spell.builder()
          .id(6L)
          .name("Fire Bolt")
          .school(SpellSchool.EVOCATION)
          .level(0)
          .ritual(false)
          .concentration(false)
          .build();
      SpellResponse fireBoltResponse = SpellResponse.builder()
          .id(6L)
          .name("Fire Bolt")
          .school(SpellSchool.EVOCATION)
          .level(0)
          .build();

      when(spellRepository.findByNameContainingIgnoreCase("fire", pageable))
          .thenReturn(List.of(fireBolt, fireball));
      when(spellMapper.toResponse(fireBolt)).thenReturn(fireBoltResponse);

      List<SpellResponse> result = spellService.searchByName("fire", SpellSchool.EVOCATION, 2,
          pageable);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getName()).isEqualTo("Fire Bolt");
    }

    @Test
    @DisplayName("Should return empty list when no matches found")
    void shouldReturnEmptyListWhenNoMatches() {
      Pageable pageable = Pageable.unpaged();

      when(spellRepository.findByNameContainingIgnoreCase("nonexistent", pageable))
          .thenReturn(List.of());

      List<SpellResponse> result = spellService.searchByName("nonexistent", null, null, pageable);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("Create Spell")
  class CreateSpellTests {

    @Test
    @DisplayName("Should create spell successfully")
    void shouldCreateSpellSuccessfully() {
      when(spellRepository.existsByName("Fireball")).thenReturn(false);
      when(spellMapper.toEntity(testSpellRequest)).thenReturn(fireball);
      when(spellRepository.save(fireball)).thenReturn(fireball);
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      SpellResponse result = spellService.create(testSpellRequest);

      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("Fireball");
      verify(spellRepository).save(fireball);
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate spell")
    void shouldThrowExceptionWhenCreatingDuplicateSpell() {
      when(spellRepository.existsByName("Fireball")).thenReturn(true);

      assertThatThrownBy(() -> spellService.create(testSpellRequest))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("Spell with this name already exists");

      verify(spellRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Update Spell")
  class UpdateSpellTests {

    @Test
    @DisplayName("Should update spell successfully")
    void shouldUpdateSpellSuccessfully() {
      when(spellRepository.findById(1L)).thenReturn(Optional.of(fireball));
      when(spellRepository.save(fireball)).thenReturn(fireball);
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      SpellResponse result = spellService.update(1L, testSpellRequest);

      assertThat(result).isNotNull();
      verify(spellMapper).updateEntity(fireball, testSpellRequest);
      verify(spellRepository).save(fireball);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent spell")
    void shouldThrowExceptionWhenUpdatingNonExistentSpell() {
      when(spellRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> spellService.update(999L, testSpellRequest))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Spell")
          .hasMessageContaining("999");

      verify(spellRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Delete Spell")
  class DeleteSpellTests {

    @Test
    @DisplayName("Should delete spell successfully")
    void shouldDeleteSpellSuccessfully() {
      when(spellRepository.existsById(1L)).thenReturn(true);

      spellService.delete(1L);

      verify(spellRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent spell")
    void shouldThrowExceptionWhenDeletingNonExistentSpell() {
      when(spellRepository.existsById(999L)).thenReturn(false);

      assertThatThrownBy(() -> spellService.delete(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Spell")
          .hasMessageContaining("999");

      verify(spellRepository, never()).deleteById(anyLong());
    }
  }

  @Nested
  @DisplayName("Create Bulk Spells")
  class CreateBulkSpellsTests {

    @Test
    @DisplayName("Should create multiple spells successfully")
    void shouldCreateMultipleSpellsSuccessfully() {
      SpellRequest request1 = SpellRequest.builder()
          .name("Fireball")
          .level(3)
          .school(SpellSchool.EVOCATION)
          .build();
      SpellRequest request2 = SpellRequest.builder()
          .name("Magic Missile")
          .level(1)
          .school(SpellSchool.EVOCATION)
          .build();
      List<SpellRequest> requests = List.of(request1, request2);

      when(spellRepository.existsByName("Fireball")).thenReturn(false);
      when(spellRepository.existsByName("Magic Missile")).thenReturn(false);
      when(spellMapper.toEntity(request1)).thenReturn(fireball);
      when(spellMapper.toEntity(request2)).thenReturn(magicMissile);
      when(spellRepository.saveAll(List.of(fireball, magicMissile)))
          .thenReturn(List.of(fireball, magicMissile));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);
      when(spellMapper.toResponse(magicMissile)).thenReturn(magicMissileResponse);

      List<SpellResponse> result = spellService.createBulk(requests);

      assertThat(result).hasSize(2);
      assertThat(result.get(0).getName()).isEqualTo("Fireball");
      assertThat(result.get(1).getName()).isEqualTo("Magic Missile");
      verify(spellRepository).saveAll(List.of(fireball, magicMissile));
    }

    @Test
    @DisplayName("Should throw exception when bulk create contains duplicate name")
    void shouldThrowExceptionWhenBulkCreateContainsDuplicate() {
      SpellRequest request1 = SpellRequest.builder()
          .name("Existing Spell")
          .level(1)
          .school(SpellSchool.EVOCATION)
          .build();
      List<SpellRequest> requests = List.of(request1);

      when(spellRepository.existsByName("Existing Spell")).thenReturn(true);

      assertThatThrownBy(() -> spellService.createBulk(requests))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("Existing Spell")
          .hasMessageContaining("already exists");

      verify(spellRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should throw exception when second spell in bulk is duplicate")
    void shouldThrowExceptionWhenSecondSpellInBulkIsDuplicate() {
      SpellRequest request1 = SpellRequest.builder()
          .name("New Spell")
          .level(1)
          .school(SpellSchool.EVOCATION)
          .build();
      SpellRequest request2 = SpellRequest.builder()
          .name("Existing Spell")
          .level(2)
          .school(SpellSchool.ABJURATION)
          .build();
      List<SpellRequest> requests = List.of(request1, request2);

      when(spellRepository.existsByName("New Spell")).thenReturn(false);
      when(spellRepository.existsByName("Existing Spell")).thenReturn(true);

      assertThatThrownBy(() -> spellService.createBulk(requests))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("Existing Spell");

      verify(spellRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should create single spell in bulk")
    void shouldCreateSingleSpellInBulk() {
      SpellRequest request = SpellRequest.builder()
          .name("Fireball")
          .level(3)
          .school(SpellSchool.EVOCATION)
          .build();
      List<SpellRequest> requests = List.of(request);

      when(spellRepository.existsByName("Fireball")).thenReturn(false);
      when(spellMapper.toEntity(request)).thenReturn(fireball);
      when(spellRepository.saveAll(List.of(fireball))).thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      List<SpellResponse> result = spellService.createBulk(requests);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getName()).isEqualTo("Fireball");
    }

    @Test
    @DisplayName("Should return empty list when bulk create with empty list")
    void shouldReturnEmptyListWhenBulkCreateWithEmptyList() {
      List<SpellRequest> requests = List.of();

      when(spellRepository.saveAll(List.of())).thenReturn(List.of());

      List<SpellResponse> result = spellService.createBulk(requests);

      assertThat(result).isEmpty();
      verify(spellRepository).saveAll(List.of());
    }
  }
}
