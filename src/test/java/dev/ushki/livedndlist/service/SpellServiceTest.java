package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.entity.character.Spell;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.SpellMapper;
import dev.ushki.livedndlist.repository.SpellRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

  @InjectMocks
  private SpellService spellService;

  private Spell fireball;
  private Spell magicMissile;
  private Spell shield;
  private Spell heal;
  private SpellResponse fireballResponse;
  private SpellResponse magicMissileResponse;
  private SpellResponse shieldResponse;
  private SpellResponse healResponse;
  private SpellRequest testSpellRequest;

  @BeforeEach
  void setUp() {
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
    @DisplayName("Should filter spells by ritual")
    void shouldFilterSpellsByRitual() {
      Spell ritualSpell = Spell.builder()
          .id(5L)
          .name("Detect Magic")
          .level(1)
          .school(SpellSchool.DIVINATION)
          .ritual(true)
          .concentration(false)
          .build();

      SpellResponse ritualResponse = SpellResponse.builder()
          .id(5L)
          .name("Detect Magic")
          .level(1)
          .school(SpellSchool.DIVINATION)
          .ritual(true)
          .concentration(false)
          .build();

      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, ritualSpell));
      when(spellMapper.toResponse(ritualSpell)).thenReturn(ritualResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, null, null, true, null, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().isRitual()).isTrue();
    }

    @Test
    @DisplayName("Should filter spells by concentration")
    void shouldFilterSpellsByConcentration() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, heal));
      when(spellMapper.toResponse(heal)).thenReturn(healResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, null, null, null, true, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().isConcentration()).isTrue();
    }

    @Test
    @DisplayName("Should filter non-concentration spells")
    void shouldFilterNonConcentrationSpells() {
      when(spellRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(fireball, heal));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      List<SpellResponse> result = spellService.getAllSpells(
          null, null, null, null, false, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().isConcentration()).isFalse();
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
      when(spellRepository.findByNameContainingIgnoreCase("fire", any(Pageable.class)))
          .thenReturn(List.of(fireball));
      when(spellMapper.toResponse(fireball)).thenReturn(fireballResponse);

      List<SpellResponse> result = spellService.searchByName("fire", null, null,
          any(Pageable.class));

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getName()).containsIgnoringCase("fire");
      verify(spellRepository).findByNameContainingIgnoreCase("fire", any(Pageable.class));
    }

    @Test
    @DisplayName("Should search spells by name and filter by school")
    void shouldSearchSpellsByNameAndFilterBySchool() {
      when(spellRepository.findByNameContainingIgnoreCase("magic", any(Pageable.class)))
          .thenReturn(List.of(magicMissile));
      when(spellMapper.toResponse(magicMissile)).thenReturn(magicMissileResponse);

      List<SpellResponse> result = spellService.searchByName(
          "magic", SpellSchool.EVOCATION, null, any(Pageable.class));

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getSchool()).isEqualTo(SpellSchool.EVOCATION);
    }

    @Test
    @DisplayName("Should search spells by name and filter by max level")
    void shouldSearchSpellsByNameAndFilterByMaxLevel() {
      when(spellRepository.findByNameContainingIgnoreCase("missile", any(Pageable.class)))
          .thenReturn(List.of(magicMissile, fireball));
      when(spellMapper.toResponse(magicMissile)).thenReturn(magicMissileResponse);

      List<SpellResponse> result = spellService.searchByName("missile", null, 2,
          any(Pageable.class));

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getLevel()).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should search with all filters applied")
    void shouldSearchWithAllFiltersApplied() {
      when(spellRepository.findByNameContainingIgnoreCase("magic", any(Pageable.class)))
          .thenReturn(List.of(magicMissile));
      when(spellMapper.toResponse(magicMissile)).thenReturn(magicMissileResponse);

      List<SpellResponse> result = spellService.searchByName(
          "magic", SpellSchool.EVOCATION, 1, any(Pageable.class));

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getSchool()).isEqualTo(SpellSchool.EVOCATION);
      assertThat(result.getFirst().getLevel()).isLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should return empty list when no matches found")
    void shouldReturnEmptyListWhenNoMatches() {
      when(spellRepository.findByNameContainingIgnoreCase("NonExistent", any(Pageable.class)))
          .thenReturn(List.of());

      List<SpellResponse> result = spellService.searchByName("NonExistent", null, null,
          any(Pageable.class));

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should filter out spells that don't match school in search")
    void shouldFilterOutNonMatchingSchoolInSearch() {
      when(spellRepository.findByNameContainingIgnoreCase("shield", any(Pageable.class)))
          .thenReturn(List.of(shield));
      when(spellMapper.toResponse(shield)).thenReturn(shieldResponse);

      List<SpellResponse> result = spellService.searchByName(
          "shield", SpellSchool.ABJURATION, null, any(Pageable.class));

      assertThat(result).hasSize(1).allMatch(s -> s.getSchool() == SpellSchool.ABJURATION);
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
          .isInstanceOf(ResourceNotFoundException.class);

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
          .isInstanceOf(ResourceNotFoundException.class);

      verify(spellRepository, never()).deleteById(anyLong());
    }
  }
}
