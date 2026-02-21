package dev.ushki.live_dnd_list.service;

import dev.ushki.live_dnd_list.dto.request.EquipmentRequest;
import dev.ushki.live_dnd_list.dto.response.EquipmentResponse;
import dev.ushki.live_dnd_list.entity.character.Equipment;
import dev.ushki.live_dnd_list.enums.EquipmentType;
import dev.ushki.live_dnd_list.exceptions.ResourceNotFoundException;
import dev.ushki.live_dnd_list.mapper.EquipmentMapper;
import dev.ushki.live_dnd_list.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private EquipmentMapper equipmentMapper;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment testEquipment;
    private EquipmentResponse testEquipmentResponse;
    private EquipmentRequest testEquipmentRequest;

    @BeforeEach
    void setUp() {
        testEquipment = Equipment.builder()
                .id(1L)
                .name("Longsword")
                .type(EquipmentType.WEAPON)
                .damage("1d8")
                .damageType("slashing")
                .quantity(1)
                .weight(3.0)
                .build();

        testEquipmentResponse = EquipmentResponse.builder()
                .id(1L)
                .name("Longsword")
                .type(EquipmentType.WEAPON)
                .damage("1d8")
                .damageType("slashing")
                .build();

        testEquipmentRequest = EquipmentRequest.builder()
                .name("Longsword")
                .type(EquipmentType.WEAPON)
                .damage("1d8")
                .damageType("slashing")
                .quantity(1)
                .build();
    }

    @Test
    @DisplayName("Should get all equipment")
    void shouldGetAllEquipment() {
        when(equipmentRepository.findAll()).thenReturn(List.of(testEquipment));
        when(equipmentMapper.toResponseList(anyList())).thenReturn(List.of(testEquipmentResponse));

        List<EquipmentResponse> result = equipmentService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Longsword");
        verify(equipmentRepository).findAll();
    }

    @Test
    @DisplayName("Should get equipment by ID")
    void shouldGetEquipmentById() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(equipmentMapper.toResponse(testEquipment)).thenReturn(testEquipmentResponse);

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

    @Test
    @DisplayName("Should get equipment by type")
    void shouldGetEquipmentByType() {
        when(equipmentRepository.findByType(EquipmentType.WEAPON)).thenReturn(List.of(testEquipment));
        when(equipmentMapper.toResponseList(anyList())).thenReturn(List.of(testEquipmentResponse));

        List<EquipmentResponse> result = equipmentService.getByType(EquipmentType.WEAPON);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(EquipmentType.WEAPON);
        verify(equipmentRepository).findByType(EquipmentType.WEAPON);
    }

    @Test
    @DisplayName("Should search equipment by name")
    void shouldSearchEquipmentByName() {
        when(equipmentRepository.findByNameContainingIgnoreCase("sword")).thenReturn(List.of(testEquipment));
        when(equipmentMapper.toResponseList(anyList())).thenReturn(List.of(testEquipmentResponse));

        List<EquipmentResponse> result = equipmentService.searchByName("sword");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("sword");
        verify(equipmentRepository).findByNameContainingIgnoreCase("sword");
    }

    @Test
    @DisplayName("Should create equipment successfully")
    void shouldCreateEquipmentSuccessfully() {
        when(equipmentMapper.toEntity(testEquipmentRequest)).thenReturn(testEquipment);
        when(equipmentRepository.save(testEquipment)).thenReturn(testEquipment);
        when(equipmentMapper.toResponse(testEquipment)).thenReturn(testEquipmentResponse);

        EquipmentResponse result = equipmentService.create(testEquipmentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Longsword");
        verify(equipmentRepository).save(testEquipment);
    }

    @Test
    @DisplayName("Should update equipment successfully")
    void shouldUpdateEquipmentSuccessfully() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(equipmentRepository.save(testEquipment)).thenReturn(testEquipment);
        when(equipmentMapper.toResponse(testEquipment)).thenReturn(testEquipmentResponse);

        EquipmentResponse result = equipmentService.update(1L, testEquipmentRequest);

        assertThat(result).isNotNull();
        verify(equipmentMapper).updateEntity(testEquipment, testEquipmentRequest);
        verify(equipmentRepository).save(testEquipment);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent equipment")
    void shouldThrowExceptionWhenUpdatingNonExistentEquipment() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.update(999L, testEquipmentRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(equipmentRepository, never()).save(any());
    }

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
