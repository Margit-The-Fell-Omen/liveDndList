package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.EquipmentMapper;
import dev.ushki.livedndlist.repository.EquipmentRepository;
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
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

  @Mock
  private EquipmentRepository equipmentRepository;

  @Mock
  private EquipmentMapper equipmentMapper;

  @InjectMocks
  private EquipmentService equipmentService;

  private Equipment testWeapon;
  private Equipment testArmor;
  private Equipment heavyWeapon;
  private EquipmentResponse testWeaponResponse;
  private EquipmentResponse testArmorResponse;
  private EquipmentResponse heavyWeaponResponse;
  private EquipmentRequest testEquipmentRequest;

  @BeforeEach
  void setUp() {
    testWeapon = Equipment.builder()
        .id(1L)
        .name("Longsword")
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

      assertThat(result).hasSize(2);
      assertThat(result).allMatch(e -> e.getType() == EquipmentType.WEAPON);
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
      assertThat(result.get(0).getWeight()).isGreaterThanOrEqualTo(50.0);
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
      assertThat(result.get(0).getWeight()).isLessThanOrEqualTo(5.0);
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

      assertThat(result).hasSize(2);
      assertThat(result).allMatch(e -> e.getWeight() >= 2.0 && e.getWeight() <= 10.0);
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
      assertThat(result.get(0).getType()).isEqualTo(EquipmentType.WEAPON);
      assertThat(result.get(0).getWeight()).isBetween(5.0, 10.0);
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
      assertThat(result.get(0).getWeight()).isNotNull();
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
      when(equipmentRepository.findByNameContainingIgnoreCase("sword"))
          .thenReturn(List.of(testWeapon, heavyWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      when(equipmentMapper.toResponse(heavyWeapon)).thenReturn(heavyWeaponResponse);

      List<EquipmentResponse> result = equipmentService.searchByName("sword", null);

      assertThat(result).hasSize(2);
      assertThat(result).allMatch(e -> e.getName().toLowerCase().contains("sword"));
      verify(equipmentRepository).findByNameContainingIgnoreCase("sword");
    }

    @Test
    @DisplayName("Should search equipment by name and filter by type")
    void shouldSearchEquipmentByNameAndFilterByType() {
      when(equipmentRepository.findByNameContainingIgnoreCase("mail"))
          .thenReturn(List.of(testArmor));
      when(equipmentMapper.toResponse(testArmor)).thenReturn(testArmorResponse);

      List<EquipmentResponse> result = equipmentService.searchByName("mail", EquipmentType.ARMOR);

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getType()).isEqualTo(EquipmentType.ARMOR);
    }

    @Test
    @DisplayName("Should return empty list when no matches found")
    void shouldReturnEmptyListWhenNoMatches() {
      when(equipmentRepository.findByNameContainingIgnoreCase("NonExistent"))
          .thenReturn(List.of());

      List<EquipmentResponse> result = equipmentService.searchByName("NonExistent", null);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should filter out non-matching types in search")
    void shouldFilterOutNonMatchingTypesInSearch() {
      when(equipmentRepository.findByNameContainingIgnoreCase("sword"))
          .thenReturn(List.of(testWeapon, heavyWeapon));
      when(equipmentMapper.toResponse(testWeapon)).thenReturn(testWeaponResponse);
      when(equipmentMapper.toResponse(heavyWeapon)).thenReturn(heavyWeaponResponse);

      List<EquipmentResponse> result = equipmentService.searchByName("sword", EquipmentType.WEAPON);

      assertThat(result).hasSize(2);
      assertThat(result).allMatch(e -> e.getType() == EquipmentType.WEAPON);
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
