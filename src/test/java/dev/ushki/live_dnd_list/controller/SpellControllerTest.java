package dev.ushki.live_dnd_list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.live_dnd_list.dto.request.SpellRequest;
import dev.ushki.live_dnd_list.dto.response.SpellResponse;
import dev.ushki.live_dnd_list.enums.SpellSchool;
import dev.ushki.live_dnd_list.security.jwt.JwtTokenProvider;
import dev.ushki.live_dnd_list.service.SpellService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpellController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security filters for public endpoints
class SpellControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpellService spellService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private SpellResponse testSpellResponse;

    @BeforeEach
    void setUp() {
        testSpellResponse = SpellResponse.builder()
                .id(1L)
                .name("Fireball")
                .level(3)
                .school(SpellSchool.EVOCATION)
                .castingTime("1 action")
                .range("150 feet")
                .components("V, S, M")
                .duration("Instantaneous")
                .description("A bright streak flashes from your pointing finger...")
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/spells")
    class GetAllSpellsTests {

        @Test
        @DisplayName("Should return all spells")
        void shouldReturnAllSpells() throws Exception {
            when(spellService.getAllSpells()).thenReturn(List.of(testSpellResponse));

            mockMvc.perform(get("/api/v1/spells"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].name").value("Fireball"))
                    .andExpect(jsonPath("$.data[0].level").value(3));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/spells/{id}")
    class GetSpellByIdTests {

        @Test
        @DisplayName("Should return spell by ID")
        void shouldReturnSpellById() throws Exception {
            when(spellService.getById(1L)).thenReturn(testSpellResponse);

            mockMvc.perform(get("/api/v1/spells/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Fireball"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/spells/level/{level}")
    class GetSpellsByLevelTests {

        @Test
        @DisplayName("Should return spells by level")
        void shouldReturnSpellsByLevel() throws Exception {
            when(spellService.getByLevel(3)).thenReturn(List.of(testSpellResponse));

            mockMvc.perform(get("/api/v1/spells/level/3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].level").value(3));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/spells/school/{school}")
    class GetSpellsBySchoolTests {

        @Test
        @DisplayName("Should return spells by school")
        void shouldReturnSpellsBySchool() throws Exception {
            when(spellService.getBySchool(SpellSchool.EVOCATION)).thenReturn(List.of(testSpellResponse));

            mockMvc.perform(get("/api/v1/spells/school/EVOCATION"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].school").value("EVOCATION"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/spells/search")
    class SearchSpellsTests {

        @Test
        @DisplayName("Should search spells by name")
        void shouldSearchSpellsByName() throws Exception {
            when(spellService.searchByName("fire")).thenReturn(List.of(testSpellResponse));

            mockMvc.perform(get("/api/v1/spells/search").param("name", "fire"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].name").value("Fireball"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/spells")
    class CreateSpellTests {

        @Test
        @DisplayName("Should create spell successfully")
        void shouldCreateSpellSuccessfully() throws Exception {
            SpellRequest request = SpellRequest.builder()
                    .name("Lightning Bolt")
                    .level(3)
                    .school(SpellSchool.EVOCATION)
                    .castingTime("1 action")
                    .range("Self (100-foot line)")
                    .components("V, S, M")
                    .duration("Instantaneous")
                    .description("A stroke of lightning forms...")
                    .build();

            SpellResponse createdResponse = SpellResponse.builder()
                    .id(2L)
                    .name("Lightning Bolt")
                    .level(3)
                    .school(SpellSchool.EVOCATION)
                    .build();

            when(spellService.create(any(SpellRequest.class))).thenReturn(createdResponse);

            mockMvc.perform(post("/api/v1/spells")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Lightning Bolt"));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            SpellRequest request = SpellRequest.builder()
                    .name("")
                    .level(3)
                    .school(SpellSchool.EVOCATION)
                    .build();

            mockMvc.perform(post("/api/v1/spells")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/spells/{id}")
    class UpdateSpellTests {

        @Test
        @DisplayName("Should update spell successfully")
        void shouldUpdateSpellSuccessfully() throws Exception {
            SpellRequest request = SpellRequest.builder()
                    .name("Fireball")
                    .level(3)
                    .school(SpellSchool.EVOCATION)
                    .description("Updated description")
                    .build();

            when(spellService.update(anyLong(), any(SpellRequest.class))).thenReturn(testSpellResponse);

            mockMvc.perform(put("/api/v1/spells/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/spells/{id}")
    class DeleteSpellTests {

        @Test
        @DisplayName("Should delete spell successfully")
        void shouldDeleteSpellSuccessfully() throws Exception {
            doNothing().when(spellService).delete(1L);

            mockMvc.perform(delete("/api/v1/spells/1"))
                    .andExpect(status().isNoContent());
        }
    }
}
