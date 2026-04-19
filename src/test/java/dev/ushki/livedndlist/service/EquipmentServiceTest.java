package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import dev.ushki.livedndlist.entity.character.DndCurrency;
import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.enums.Role;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.EquipmentRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

  @Mock
  private EquipmentRepository equipmentRepository;

  @Mock
  private EquipmentMapper equipmentMapper;

  private EquipmentService equipmentService;

  private User testUser;
  private DndCharacter testCharacter;
  private Equipment testWeapon;
  private Equipment testArmor;
  private Equipment heavyWeapon;
  private EquipmentResponse testWeaponResponse;
  private EquipmentResponse testArmorResponse;
  private EquipmentResponse heavyWeaponResponse;
  private EquipmentRequest testEquipmentRequest;

  @BeforeEach
  void setUp() {
    CacheManager cacheManager = new CacheManager();
    equipmentService = new EquipmentService(equipmentRepository, equipmentMapper, cacheManager);

    testUser = User.builder()
        .id(1L)
        .username("testuser")
        .email("test@test.com")
        .password("encoded_password")
        .role(Role.ROLE_USER)
        .enabled(true)
        .build();

    testCharacter = DndCharacter.builder()
        .id(1L)
        .owner(testUser)
        .name("Gandalf")
        .maxHitPoints(45)
        .currentHitPoints(45)
        .classes(new HashSet<>())
        .skills(new HashSet<>())
        .equipment(new HashSet<>())
        .spells(new HashSet<>())
        .currency(new DndCurrency())
        .build();

    testWeapon = Equipment.builder()
        .id(1L)
        .name("Longsword")
        .character(testCharacter)
        .type(EquipmentType.WEAPON)
        .damage("1d8")
        .damageType("slashing")
        .quantity(1)
        .weight(3.0)
        .build();

    testArmor = Equipment.builder()
        .id(2L)
        .name("Chainmail")
        .type(EquipmentType.ARMOR)
        .quantity(1)
        .weight(55.0)
        .build();

    heavyWeapon = Equipment.builder()
        .id(3L)
        .name("Greatsword")
        .type(EquipmentType.WEAPON)
        .damage("2d6")
        .damageType("slashing")
        .quantity(1)
        .weight(6.0)
        .build();

    testWeaponResponse = EquipmentResponse.builder()
        .id(1L)
        .name("Longsword")
        .type(EquipmentType.WEAPON)
        .damage("1d8")
        .damageType("slashing")
        .weight(3.0)
        .build();

    testArmorResponse = EquipmentResponse.builder()
        .id(2L)
        .name("Chainmail")
        .type(EquipmentType.ARMOR)
        .weight(55.0)
        .build();

    heavyWeaponResponse = EquipmentResponse.builder()
        .id(3L)
        .name("Greatsword")
        .type(EquipmentType.WEAPON)
        .damage("2d6")
        .damageType("slashing")
        .weight(6.0)
        .build();

    testEquipmentRequest = EquipmentRequest.builder()
        .name("Longsword")
        .type(EquipmentType.WEAPON)
        .damage("1d8")
        .damageType("slashing")
        .quantity(1)
        .build();
  }

  @Nested
  @DisplayName("Caching Behavior Tests")
  class CachingTests {

    @Test
    @DisplayName("Should return from cache on second call (getById)")
    void shouldReturnFromCacheOnSecondCall() {
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);

      equipmentService.getById(1L);
      equipmentService.getById(1L);

      verify(equipmentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return from cache on second call with complex keys (getAll)")
    void shouldReturnFromCacheForComplexKeys() {
      when(equipmentRepository.findAll(any(Sort.class))).thenReturn(List.of(testWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);

      equipmentService.getAll(EquipmentType.WEAPON, 1.0, 5.0, "name", "asc");
      equipmentService.getAll(EquipmentType.WEAPON, 1.0, 5.0, "name", "asc");

      verify(equipmentRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Should invalidate cache on create")
    void shouldInvalidateCacheOnCreate() {
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      equipmentService.getById(1L);

      when(equipmentMapper.toEntity(testEquipmentRequest)).thenReturn(testWeapon);
      when(equipmentRepository.save(testWeapon)).thenReturn(testWeapon);
      equipmentService.create(testEquipmentRequest);

      equipmentService.getById(1L);

      verify(equipmentRepository, times(2)).findById(1L);
    }

    @Test
    @DisplayName("Should invalidate cache on update")
    void shouldInvalidateCacheOnUpdate() {
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      equipmentService.getById(1L);

      when(equipmentRepository.save(testWeapon)).thenReturn(testWeapon);
      equipmentService.update(1L, testEquipmentRequest);

      equipmentService.getById(1L);

      verify(equipmentRepository, times(3)).findById(1L);
    }

    @Test
    @DisplayName("Should invalidate cache on delete")
    void shouldInvalidateCacheOnDelete() {
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      equipmentService.getById(1L);

      when(equipmentRepository.existsById(1L)).thenReturn(true);
      equipmentService.delete(1L);

      equipmentService.getById(1L);

      verify(equipmentRepository, times(2)).findById(1L);
    }
  }

  @Nested
  @DisplayName("Get All Equipment")
  class GetAllEquipmentTests {

    @Test
    @DisplayName("Should get all equipment with default sorting")
    void shouldGetAllEquipment() {
      when(equipmentRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(testWeapon, testArmor));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      when(equipmentMapper.toResponse(testArmor)).thenReturn(testArmorResponse);

      List<EquipmentResponse> result = equipmentService.getAll(
          null, null, null, "name", "asc");

      assertThat(result).hasSize(2);
      verify(equipmentRepository).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Should filter equipment by type")
    void shouldFilterEquipmentByType() {
      when(equipmentRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(testWeapon, testArmor, heavyWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      when(equipmentMapper.toResponse(heavyWeapon)).thenReturn(heavyWeaponResponse);

      List<EquipmentResponse> result = equipmentService.getAll(
          EquipmentType.WEAPON, null, null, "name", "asc");

      assertThat(result).hasSize(2).allMatch(e -> e.getType() == EquipmentType.WEAPON);
    }

    @Test
    @DisplayName("Should filter equipment by minimum weight")
    void shouldFilterEquipmentByMinWeight() {
      when(equipmentRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(testWeapon, testArmor, heavyWeapon));
      when(equipmentMapper.toResponse(testArmor)).thenReturn(testArmorResponse);

      List<EquipmentResponse> result = equipmentService.getAll(
          null, 50.0, null, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getWeight()).isGreaterThanOrEqualTo(50.0);
    }

    @Test
    @DisplayName("Should filter equipment by maximum weight")
    void shouldFilterEquipmentByMaxWeight() {
      when(equipmentRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(testWeapon, testArmor, heavyWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);

      List<EquipmentResponse> result = equipmentService.getAll(
          null, null, 5.0, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getWeight()).isLessThanOrEqualTo(5.0);
    }

    @Test
    @DisplayName("Should filter equipment by weight range")
    void shouldFilterEquipmentByWeightRange() {
      when(equipmentRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(testWeapon, testArmor, heavyWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      when(equipmentMapper.toResponse(heavyWeapon)).thenReturn(heavyWeaponResponse);

      List<EquipmentResponse> result = equipmentService.getAll(
          null, 2.0, 10.0, "name", "asc");

      assertThat(result).hasSize(2).allMatch(e -> e.getWeight() >= 2.0 && e.getWeight() <= 10.0);
    }

    @Test
    @DisplayName("Should apply all filters together")
    void shouldApplyAllFiltersTogether() {
      when(equipmentRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(testWeapon, testArmor, heavyWeapon));
      when(equipmentMapper.toResponse(heavyWeapon)).thenReturn(heavyWeaponResponse);

      List<EquipmentResponse> result = equipmentService.getAll(
          EquipmentType.WEAPON, 5.0, 10.0, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getType()).isEqualTo(EquipmentType.WEAPON);
      assertThat(result.getFirst().getWeight()).isBetween(5.0, 10.0);
    }

    @Test
    @DisplayName("Should sort ascending when specified")
    void shouldSortAscending() {
      when(equipmentRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(testWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);

      equipmentService.getAll(null, null, null, "name", "asc");

      verify(equipmentRepository).findAll(Sort.by("name").ascending());
    }

    @Test
    @DisplayName("Should sort descending when specified")
    void shouldSortDescending() {
      when(equipmentRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(testWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);

      equipmentService.getAll(null, null, null, "weight", "desc");

      verify(equipmentRepository).findAll(Sort.by("weight").descending());
    }

    @Test
    @DisplayName("Should handle equipment with null weight in filters")
    void shouldHandleNullWeightInFilters() {
      Equipment noWeightItem = Equipment.builder()
          .id(4L)
          .name("Spell Focus")
          .type(EquipmentType.TOOL)
          .weight(null)
          .build();

      when(equipmentRepository.findAll(any(Sort.class)))
          .thenReturn(List.of(testWeapon, noWeightItem));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);

      List<EquipmentResponse> result = equipmentService.getAll(
          null, 1.0, null, "name", "asc");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getWeight()).isNotNull();
    }
  }

  @Nested
  @DisplayName("Get Equipment By ID")
  class GetEquipmentByIdTests {

    @Test
    @DisplayName("Should get equipment by ID")
    void shouldGetEquipmentById() {
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);

      EquipmentResponse result = equipmentService.getById(1L);

      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("Longsword");
      verify(equipmentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when equipment not found")
    void shouldThrowExceptionWhenEquipmentNotFound() {
      when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> equipmentService.getById(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Equipment")
          .hasMessageContaining("999");
    }
  }

  @Nested
  @DisplayName("Search Equipment")
  class SearchEquipmentTests {

    @Test
    @DisplayName("Should search equipment by name")
    void shouldSearchEquipmentByName() {
      Pageable pageable = Pageable.unpaged();

      when(equipmentRepository.findByNameContainingIgnoreCase(eq("sword"), any(Pageable.class)))
          .thenReturn(List.of(testWeapon, heavyWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      when(equipmentMapper.toResponse(heavyWeapon)).thenReturn(heavyWeaponResponse);

      List<EquipmentResponse> result = equipmentService.searchByName("sword", null, pageable);

      assertThat(result).hasSize(2).allMatch(e -> e.getName().toLowerCase().contains("sword"));
      verify(equipmentRepository).findByNameContainingIgnoreCase(eq("sword"), any(Pageable.class));
    }

    @Test
    @DisplayName("Should search equipment by name and filter by type")
    void shouldSearchEquipmentByNameAndFilterByType() {
      Pageable pageable = Pageable.unpaged();

      when(equipmentRepository.findByNameContainingIgnoreCase(eq("mail"), any(Pageable.class)))
          .thenReturn(List.of(testArmor));
      when(equipmentMapper.toResponse(testArmor)).thenReturn(testArmorResponse);

      List<EquipmentResponse> result = equipmentService.searchByName("mail", EquipmentType.ARMOR,
          pageable);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getType()).isEqualTo(EquipmentType.ARMOR);
    }

    @Test
    @DisplayName("Should return empty list when no matches found")
    void shouldReturnEmptyListWhenNoMatches() {
      Pageable pageable = Pageable.unpaged();

      when(equipmentRepository.findByNameContainingIgnoreCase(eq("NonExistent"),
          any(Pageable.class)))
          .thenReturn(List.of());

      List<EquipmentResponse> result = equipmentService.searchByName("NonExistent", null, pageable);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should filter out non-matching types in search")
    void shouldFilterOutNonMatchingTypesInSearch() {
      Pageable pageable = Pageable.unpaged();

      when(equipmentRepository.findByNameContainingIgnoreCase(eq("sword"), any(Pageable.class)))
          .thenReturn(List.of(testWeapon, heavyWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      when(equipmentMapper.toResponse(heavyWeapon)).thenReturn(heavyWeaponResponse);

      List<EquipmentResponse> result = equipmentService.searchByName("sword", EquipmentType.WEAPON,
          pageable);

      assertThat(result).hasSize(2).allMatch(e -> e.getType() == EquipmentType.WEAPON);
    }
  }

  @Nested
  @DisplayName("Create Equipment")
  class CreateEquipmentTests {

    @Test
    @DisplayName("Should create equipment successfully")
    void shouldCreateEquipmentSuccessfully() {
      when(equipmentMapper.toEntity(testEquipmentRequest)).thenReturn(testWeapon);
      when(equipmentRepository.save(testWeapon)).thenReturn(testWeapon);
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);

      EquipmentResponse result = equipmentService.create(testEquipmentRequest);

      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("Longsword");
      verify(equipmentRepository).save(testWeapon);
    }
  }

  @Nested
  @DisplayName("Update Equipment")
  class UpdateEquipmentTests {

    @Test
    @DisplayName("Should update equipment successfully")
    void shouldUpdateEquipmentSuccessfully() {
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testWeapon));
      when(equipmentRepository.save(testWeapon)).thenReturn(testWeapon);
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);

      EquipmentResponse result = equipmentService.update(1L, testEquipmentRequest);

      assertThat(result).isNotNull();
      verify(equipmentMapper).updateEntity(testWeapon, testEquipmentRequest);
      verify(equipmentRepository).save(testWeapon);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent equipment")
    void shouldThrowExceptionWhenUpdatingNonExistentEquipment() {
      when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> equipmentService.update(999L, testEquipmentRequest))
          .isInstanceOf(ResourceNotFoundException.class);

      verify(equipmentRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Delete Equipment")
  class DeleteEquipmentTests {

    @Test
    @DisplayName("Should delete equipment successfully")
    void shouldDeleteEquipmentSuccessfully() {
      when(equipmentRepository.existsById(1L)).thenReturn(true);

      equipmentService.delete(1L);

      verify(equipmentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent equipment")
    void shouldThrowExceptionWhenDeletingNonExistentEquipment() {
      when(equipmentRepository.existsById(999L)).thenReturn(false);

      assertThatThrownBy(() -> equipmentService.delete(999L))
          .isInstanceOf(ResourceNotFoundException.class);

      verify(equipmentRepository, never()).deleteById(anyLong());
    }
  }
}
