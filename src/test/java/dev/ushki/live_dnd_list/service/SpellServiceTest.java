package dev.ushki.live_dnd_list.service;

import dev.ushki.live_dnd_list.dto.request.SpellRequest;
import dev.ushki.live_dnd_list.dto.response.SpellResponse;
import dev.ushki.live_dnd_list.entity.character.Spell;
import dev.ushki.live_dnd_list.enums.SpellSchool;
import dev.ushki.live_dnd_list.exceptions.DuplicateResourceException;
import dev.ushki.live_dnd_list.exceptions.ResourceNotFoundException;
import dev.ushki.live_dnd_list.mapper.SpellMapper;
import dev.ushki.live_dnd_list.repository.SpellRepository;
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
class SpellServiceTest {

    @Mock
    private SpellRepository spellRepository;

    @Mock
    private SpellMapper spellMapper;

    @InjectMocks
    private SpellService spellService;

    private Spell testSpell;
    private SpellResponse testSpellResponse;
    private SpellRequest testSpellRequest;

    @BeforeEach
    void setUp() {
        testSpell = Spell.builder()
                .id(1L)
                .name("Fireball")
                .level(3)
                .school(SpellSchool.EVOCATION)
                .castingTime("1 action")
                .range("150 feet")
                .components("V, S, M")
                .duration("Instantaneous")
                .description("A bright streak flashes...")
                .build();

        testSpellResponse = SpellResponse.builder()
                .id(1L)
                .name("Fireball")
                .level(3)
                .school(SpellSchool.EVOCATION)
                .castingTime("1 action")
                .range("150 feet")
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

    @Test
    @DisplayName("Should get all spells")
    void shouldGetAllSpells() {
        when(spellRepository.findAll()).thenReturn(List.of(testSpell));
        when(spellMapper.toResponseList(anyList())).thenReturn(List.of(testSpellResponse));

        List<SpellResponse> result = spellService.getAllSpells();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fireball");
        verify(spellRepository).findAll();
    }

    @Test
    @DisplayName("Should get spell by ID")
    void shouldGetSpellById() {
        when(spellRepository.findById(1L)).thenReturn(Optional.of(testSpell));
        when(spellMapper.toResponse(testSpell)).thenReturn(testSpellResponse);

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

    @Test
    @DisplayName("Should get spells by level")
    void shouldGetSpellsByLevel() {
        when(spellRepository.findByLevel(3)).thenReturn(List.of(testSpell));
        when(spellMapper.toResponseList(anyList())).thenReturn(List.of(testSpellResponse));

        List<SpellResponse> result = spellService.getByLevel(3);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLevel()).isEqualTo(3);
        verify(spellRepository).findByLevel(3);
    }

    @Test
    @DisplayName("Should get spells by school")
    void shouldGetSpellsBySchool() {
        when(spellRepository.findBySchool(SpellSchool.EVOCATION)).thenReturn(List.of(testSpell));
        when(spellMapper.toResponseList(anyList())).thenReturn(List.of(testSpellResponse));

        List<SpellResponse> result = spellService.getBySchool(SpellSchool.EVOCATION);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSchool()).isEqualTo(SpellSchool.EVOCATION);
        verify(spellRepository).findBySchool(SpellSchool.EVOCATION);
    }

    @Test
    @DisplayName("Should search spells by name")
    void shouldSearchSpellsByName() {
        when(spellRepository.findByNameContainingIgnoreCase("fire")).thenReturn(List.of(testSpell));
        when(spellMapper.toResponseList(anyList())).thenReturn(List.of(testSpellResponse));

        List<SpellResponse> result = spellService.searchByName("fire");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("fire");
        verify(spellRepository).findByNameContainingIgnoreCase("fire");
    }

    @Test
    @DisplayName("Should create spell successfully")
    void shouldCreateSpellSuccessfully() {
        when(spellRepository.existsByName("Fireball")).thenReturn(false);
        when(spellMapper.toEntity(testSpellRequest)).thenReturn(testSpell);
        when(spellRepository.save(testSpell)).thenReturn(testSpell);
        when(spellMapper.toResponse(testSpell)).thenReturn(testSpellResponse);

        SpellResponse result = spellService.create(testSpellRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Fireball");
        verify(spellRepository).save(testSpell);
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

    @Test
    @DisplayName("Should update spell successfully")
    void shouldUpdateSpellSuccessfully() {
        when(spellRepository.findById(1L)).thenReturn(Optional.of(testSpell));
        when(spellRepository.save(testSpell)).thenReturn(testSpell);
        when(spellMapper.toResponse(testSpell)).thenReturn(testSpellResponse);

        SpellResponse result = spellService.update(1L, testSpellRequest);

        assertThat(result).isNotNull();
        verify(spellMapper).updateEntity(testSpell, testSpellRequest);
        verify(spellRepository).save(testSpell);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent spell")
    void shouldThrowExceptionWhenUpdatingNonExistentSpell() {
        when(spellRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spellService.update(999L, testSpellRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(spellRepository, never()).save(any());
    }

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
