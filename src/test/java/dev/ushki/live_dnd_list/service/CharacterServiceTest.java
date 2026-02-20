package dev.ushki.live_dnd_list.service;

import dev.ushki.live_dnd_list.dto.request.CharacterCreateRequest;
import dev.ushki.live_dnd_list.dto.response.CharacterResponse;
import dev.ushki.live_dnd_list.dto.response.CharacterSummaryResponse;
import dev.ushki.live_dnd_list.entity.User;
import dev.ushki.live_dnd_list.entity.character.DndCharacter;
import dev.ushki.live_dnd_list.entity.character.Spell;
import dev.ushki.live_dnd_list.enums.CharacterRace;
import dev.ushki.live_dnd_list.enums.Role;
import dev.ushki.live_dnd_list.enums.SpellSchool;
import dev.ushki.live_dnd_list.exceptions.ResourceNotFoundException;
import dev.ushki.live_dnd_list.exceptions.UnauthorizedException;
import dev.ushki.live_dnd_list.mapper.CharacterMapper;
import dev.ushki.live_dnd_list.mapper.EquipmentMapper;
import dev.ushki.live_dnd_list.repository.CharacterRepository;
import dev.ushki.live_dnd_list.repository.SpellRepository;
import dev.ushki.live_dnd_list.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private CharacterService characterService;

    private User testUser;
    private DndCharacter testCharacter;
    private CharacterResponse testCharacterResponse;
    private CharacterSummaryResponse testCharacterSummary;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("password")
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .build();

        testCharacter = DndCharacter.builder()
                .id(1L)
                .owner(testUser)
                .name("Gandalf")
                .race(CharacterRace.HUMAN)
                .maxHitPoints(45)
                .currentHitPoints(45)
                .build();

        testCharacterResponse = CharacterResponse.builder()
                .id(1L)
                .name("Gandalf")
                .race(CharacterRace.HUMAN)
                .maxHitPoints(45)
                .currentHitPoints(45)
                .build();

        testCharacterSummary = CharacterSummaryResponse.builder()
                .id(1L)
                .name("Gandalf")
                .race(CharacterRace.HUMAN)
                .totalLevel(1)
                .build();
    }

    @Test
    @DisplayName("Should get all characters by username")
    void shouldGetAllCharactersByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(characterRepository.findAllByOwnerOrderByUpdatedAtDesc(testUser))
                .thenReturn(List.of(testCharacter));
        when(characterMapper.toSummaryResponseList(anyList()))
                .thenReturn(List.of(testCharacterSummary));

        List<CharacterSummaryResponse> result = characterService.getAllByUsername("testuser");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Gandalf");
    }

    @Test
    @DisplayName("Should get character by ID")
    void shouldGetCharacterById() {
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

        CharacterResponse result = characterService.getById(1L, "testuser");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Gandalf");
    }

    @Test
    @DisplayName("Should throw exception when character not found")
    void shouldThrowExceptionWhenCharacterNotFound() {
        when(characterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> characterService.getById(999L, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when user doesn't own character")
    void shouldThrowExceptionWhenUserDoesntOwnCharacter() {
        User anotherUser = User.builder()
                .id(2L)
                .username("another")
                .build();
        DndCharacter otherCharacter = DndCharacter.builder()
                .id(2L)
                .owner(anotherUser)
                .name("Other")
                .race(CharacterRace.ELF)
                .build();

        when(characterRepository.findById(2L)).thenReturn(Optional.of(otherCharacter));

        assertThatThrownBy(() -> characterService.getById(2L, "testuser"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Should create character successfully")
    void shouldCreateCharacterSuccessfully() {
        CharacterCreateRequest request = CharacterCreateRequest.builder()
                .name("Legolas")
                .race(CharacterRace.ELF)
                .className("Ranger")
                .build();

        DndCharacter newCharacter = DndCharacter.builder()
                .name("Legolas")
                .race(CharacterRace.ELF)
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
    @DisplayName("Should delete character successfully")
    void shouldDeleteCharacterSuccessfully() {
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));

        characterService.delete(1L, "testuser");

        verify(characterRepository).delete(testCharacter);
    }

    @Test
    @DisplayName("Should add spell to character")
    void shouldAddSpellToCharacter() {
        Spell spell = Spell.builder()
                .id(1L)
                .name("Fireball")
                .level(3)
                .school(SpellSchool.EVOCATION)
                .build();

        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(spellRepository.findById(1L)).thenReturn(Optional.of(spell));
        when(characterRepository.save(any(DndCharacter.class))).thenReturn(testCharacter);
        when(characterMapper.toResponse(testCharacter)).thenReturn(testCharacterResponse);

        CharacterResponse result = characterService.addSpell(1L, 1L, "testuser");

        assertThat(result).isNotNull();
        verify(characterRepository).save(testCharacter);
    }
}
