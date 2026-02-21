package dev.ushki.live_dnd_list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.live_dnd_list.dto.request.AbilityScoresRequest;
import dev.ushki.live_dnd_list.dto.request.CharacterCreateRequest;
import dev.ushki.live_dnd_list.dto.request.CharacterUpdateRequest;
import dev.ushki.live_dnd_list.dto.request.EquipmentRequest;
import dev.ushki.live_dnd_list.dto.response.CharacterResponse;
import dev.ushki.live_dnd_list.dto.response.CharacterSummaryResponse;
import dev.ushki.live_dnd_list.enums.CharacterAlignment;
import dev.ushki.live_dnd_list.enums.CharacterRace;
import dev.ushki.live_dnd_list.enums.EquipmentType;
import dev.ushki.live_dnd_list.security.jwt.JwtAuthenticationEntryPoint;
import dev.ushki.live_dnd_list.security.jwt.JwtAuthenticationFilter;
import dev.ushki.live_dnd_list.security.jwt.JwtTokenProvider;
import dev.ushki.live_dnd_list.service.CharacterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CharacterController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CharacterService characterService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private CharacterResponse testCharacterResponse;
    private CharacterSummaryResponse testCharacterSummary;

    @BeforeEach
    void setUp() {
        testCharacterResponse = CharacterResponse.builder()
                .id(1L)
                .name("Gandalf")
                .race(CharacterRace.HUMAN)
                .alignment(CharacterAlignment.NEUTRAL_GOOD)
                .totalLevel(5)
                .maxHitPoints(45)
                .currentHitPoints(45)
                .armorClass(15)
                .build();

        testCharacterSummary = CharacterSummaryResponse.builder()
                .id(1L)
                .name("Gandalf")
                .race(CharacterRace.HUMAN)
                .classDisplay("Wizard 5")
                .totalLevel(5)
                .currentHitPoints(45)
                .maxHitPoints(45)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/characters")
    class GetAllCharactersTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should return all user's characters")
        void shouldReturnAllCharacters() throws Exception {
            when(characterService.getAllByUsername("testuser"))
                    .thenReturn(List.of(testCharacterSummary));

            mockMvc.perform(get("/api/v1/characters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].name").value("Gandalf"))
                    .andExpect(jsonPath("$.data[0].race").value("HUMAN"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/characters"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/characters/{id}")
    class GetCharacterByIdTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should return character by ID")
        void shouldReturnCharacterById() throws Exception {
            when(characterService.getById(1L, "testuser")).thenReturn(testCharacterResponse);

            mockMvc.perform(get("/api/v1/characters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Gandalf"))
                    .andExpect(jsonPath("$.data.race").value("HUMAN"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/characters")
    class CreateCharacterTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should create character successfully")
        void shouldCreateCharacterSuccessfully() throws Exception {
            CharacterCreateRequest request = CharacterCreateRequest.builder()
                    .name("Legolas")
                    .race(CharacterRace.ELF)
                    .className("Ranger")
                    .alignment(CharacterAlignment.CHAOTIC_GOOD)
                    .abilityScores(AbilityScoresRequest.builder()
                            .strength(12)
                            .dexterity(18)
                            .constitution(14)
                            .intelligence(13)
                            .wisdom(14)
                            .charisma(10)
                            .build())
                    .maxHitPoints(28)
                    .build();

            CharacterResponse createdResponse = CharacterResponse.builder()
                    .id(2L)
                    .name("Legolas")
                    .race(CharacterRace.ELF)
                    .alignment(CharacterAlignment.CHAOTIC_GOOD)
                    .totalLevel(1)
                    .maxHitPoints(28)
                    .currentHitPoints(28)
                    .build();

            when(characterService.create(any(CharacterCreateRequest.class), eq("testuser")))
                    .thenReturn(createdResponse);

            mockMvc.perform(post("/api/v1/characters")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Legolas"))
                    .andExpect(jsonPath("$.data.race").value("ELF"));
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            CharacterCreateRequest request = CharacterCreateRequest.builder()
                    .name("")
                    .race(CharacterRace.ELF)
                    .className("Ranger")
                    .build();

            mockMvc.perform(post("/api/v1/characters")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should return 400 when race is null")
        void shouldReturn400WhenRaceNull() throws Exception {
            CharacterCreateRequest request = CharacterCreateRequest.builder()
                    .name("Legolas")
                    .race(null)
                    .className("Ranger")
                    .build();

            mockMvc.perform(post("/api/v1/characters")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/characters/{id}")
    class UpdateCharacterTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should update character successfully")
        void shouldUpdateCharacterSuccessfully() throws Exception {
            CharacterUpdateRequest request = CharacterUpdateRequest.builder()
                    .name("Gandalf the White")
                    .currentHitPoints(50)
                    .maxHitPoints(50)
                    .build();

            CharacterResponse updatedResponse = CharacterResponse.builder()
                    .id(1L)
                    .name("Gandalf the White")
                    .race(CharacterRace.HUMAN)
                    .maxHitPoints(50)
                    .currentHitPoints(50)
                    .build();

            when(characterService.update(eq(1L), any(CharacterUpdateRequest.class), eq("testuser")))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/v1/characters/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Gandalf the White"))
                    .andExpect(jsonPath("$.data.maxHitPoints").value(50));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/characters/{id}")
    class DeleteCharacterTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should delete character successfully")
        void shouldDeleteCharacterSuccessfully() throws Exception {
            doNothing().when(characterService).delete(1L, "testuser");

            mockMvc.perform(delete("/api/v1/characters/1")
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/characters/{id}/equipment")
    class AddEquipmentTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should add equipment to character")
        void shouldAddEquipmentToCharacter() throws Exception {
            EquipmentRequest request = EquipmentRequest.builder()
                    .name("Longsword")
                    .type(EquipmentType.WEAPON)
                    .damage("1d8")
                    .damageType("slashing")
                    .quantity(1)
                    .build();

            when(characterService.addEquipment(eq(1L), any(EquipmentRequest.class), eq("testuser")))
                    .thenReturn(testCharacterResponse);

            mockMvc.perform(post("/api/v1/characters/1/equipment")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/characters/{id}/equipment/{equipmentId}")
    class RemoveEquipmentTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should remove equipment from character")
        void shouldRemoveEquipmentFromCharacter() throws Exception {
            when(characterService.removeEquipment(1L, 1L, "testuser"))
                    .thenReturn(testCharacterResponse);

            mockMvc.perform(delete("/api/v1/characters/1/equipment/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/characters/{id}/spells/{spellId}")
    class AddSpellTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should add spell to character")
        void shouldAddSpellToCharacter() throws Exception {
            when(characterService.addSpell(1L, 1L, "testuser"))
                    .thenReturn(testCharacterResponse);

            mockMvc.perform(post("/api/v1/characters/1/spells/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/characters/{id}/spells/{spellId}")
    class RemoveSpellTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should remove spell from character")
        void shouldRemoveSpellFromCharacter() throws Exception {
            when(characterService.removeSpell(1L, 1L, "testuser"))
                    .thenReturn(testCharacterResponse);

            mockMvc.perform(delete("/api/v1/characters/1/spells/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
