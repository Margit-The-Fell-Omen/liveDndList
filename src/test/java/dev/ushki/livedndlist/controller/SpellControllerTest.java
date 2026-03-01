package dev.ushki.livedndlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import dev.ushki.livedndlist.service.SpellService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for SpellController.
 */
@WebMvcTest(SpellController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpellControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SpellService spellService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  private SpellResponse fireballResponse;
  private SpellResponse lightningBoltResponse;
  private SpellResponse shieldResponse;

  @BeforeEach
  void setUp() {
    fireballResponse = SpellResponse.builder()
        .id(1L)
        .name("Fireball")
        .level(3)
        .school(SpellSchool.EVOCATION)
        .castingTime("1 action")
        .range("150 feet")
        .components("V, S, M")
        .duration("Instantaneous")
        .concentration(false)
        .ritual(false)
        .description("A bright streak flashes from your pointing finger...")
        .build();

    lightningBoltResponse = SpellResponse.builder()
        .id(2L)
        .name("Lightning Bolt")
        .level(3)
        .school(SpellSchool.EVOCATION)
        .castingTime("1 action")
        .range("Self (100-foot line)")
        .components("V, S, M")
        .duration("Instantaneous")
        .concentration(false)
        .ritual(false)
        .build();

    shieldResponse = SpellResponse.builder()
        .id(3L)
        .name("Shield")
        .level(1)
        .school(SpellSchool.ABJURATION)
        .castingTime("1 reaction")
        .range("Self")
        .components("V, S")
        .duration("1 round")
        .concentration(false)
        .ritual(false)
        .build();
  }

  @Nested
  @DisplayName("GET /api/v1/spells")
  class GetAllSpellsTests {

    @Test
    @DisplayName("Should return all spells without filters")
    void shouldReturnAllSpellsWithoutFilters() throws Exception {
      when(spellService.getAllSpells(
          isNull(), isNull(), isNull(), isNull(), isNull(),
          eq("name"), eq("asc")))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse, shieldResponse));

      mockMvc.perform(get("/api/v1/spells"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(3))
          .andExpect(jsonPath("$.data[0].name").value("Fireball"));
    }

    @Test
    @DisplayName("Should return spells filtered by school")
    void shouldReturnSpellsFilteredBySchool() throws Exception {
      when(spellService.getAllSpells(
          eq(SpellSchool.EVOCATION), isNull(), isNull(), isNull(), isNull(),
          eq("name"), eq("asc")))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("school", "EVOCATION"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(2))
          .andExpect(jsonPath("$.data[0].school").value("EVOCATION"));
    }

    @Test
    @DisplayName("Should return spells filtered by exact level")
    void shouldReturnSpellsFilteredByLevel() throws Exception {
      when(spellService.getAllSpells(
          isNull(), isNull(), isNull(), isNull(), isNull(),
          eq("name"), eq("asc")))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("level", "3"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].level").value(3));
    }

    @Test
    @DisplayName("Should return spells filtered by level range")
    void shouldReturnSpellsFilteredByLevelRange() throws Exception {
      when(spellService.getAllSpells(
          isNull(), eq(1), eq(3), isNull(), isNull(),
          eq("name"), eq("asc")))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse, shieldResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("minLevel", "1")
              .param("maxLevel", "3"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("Should return spells filtered by concentration")
    void shouldReturnSpellsFilteredByConcentration() throws Exception {
      when(spellService.getAllSpells(
          isNull(), isNull(), isNull(), isNull(), eq(true),
          eq("name"), eq("asc")))
          .thenReturn(List.of());

      mockMvc.perform(get("/api/v1/spells")
              .param("concentration", "true"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Should return spells filtered by ritual")
    void shouldReturnSpellsFilteredByRitual() throws Exception {
      when(spellService.getAllSpells(
          isNull(), isNull(), isNull(), eq(true), isNull(),
          eq("name"), eq("asc")))
          .thenReturn(List.of());

      mockMvc.perform(get("/api/v1/spells")
              .param("ritual", "true"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return spells with custom sorting")
    void shouldReturnSpellsWithCustomSorting() throws Exception {
      when(spellService.getAllSpells(
          isNull(), isNull(), isNull(), isNull(), isNull(),
          eq("level"), eq("desc")))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse, shieldResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("sortBy", "level")
              .param("sortDir", "desc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return spells with multiple filters")
    void shouldReturnSpellsWithMultipleFilters() throws Exception {
      when(spellService.getAllSpells(
          eq(SpellSchool.EVOCATION), eq(1), eq(5), isNull(), eq(false),
          eq("name"), eq("asc")))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("school", "EVOCATION")
              .param("minLevel", "1")
              .param("maxLevel", "5")
              .param("concentration", "false"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(2));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/spells/{id}")
  class GetSpellByIdTests {

    @Test
    @DisplayName("Should return spell by ID")
    void shouldReturnSpellById() throws Exception {
      when(spellService.getById(1L)).thenReturn(fireballResponse);

      mockMvc.perform(get("/api/v1/spells/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.name").value("Fireball"))
          .andExpect(jsonPath("$.data.level").value(3))
          .andExpect(jsonPath("$.data.school").value("EVOCATION"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/spells/search")
  class SearchSpellsTests {

    @Test
    @DisplayName("Should search spells by name")
    void shouldSearchSpellsByName() throws Exception {
      when(spellService.searchByName(eq("fire"), isNull(), isNull()))
          .thenReturn(List.of(fireballResponse));

      mockMvc.perform(get("/api/v1/spells/search")
              .param("name", "fire"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Fireball"));
    }

    @Test
    @DisplayName("Should search spells by name with school filter")
    void shouldSearchSpellsByNameWithSchoolFilter() throws Exception {
      when(spellService.searchByName(eq("bolt"), eq(SpellSchool.EVOCATION), isNull()))
          .thenReturn(List.of(lightningBoltResponse));

      mockMvc.perform(get("/api/v1/spells/search")
              .param("name", "bolt")
              .param("school", "EVOCATION"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Lightning Bolt"));
    }

    @Test
    @DisplayName("Should search spells by name with maxLevel filter")
    void shouldSearchSpellsByNameWithMaxLevelFilter() throws Exception {
      when(spellService.searchByName(eq("shield"), isNull(), eq(2)))
          .thenReturn(List.of(shieldResponse));

      mockMvc.perform(get("/api/v1/spells/search")
              .param("name", "shield")
              .param("maxLevel", "2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Shield"))
          .andExpect(jsonPath("$.data[0].level").value(1));
    }

    @Test
    @DisplayName("Should search spells with all filters")
    void shouldSearchSpellsWithAllFilters() throws Exception {
      when(spellService.searchByName(eq("fire"), eq(SpellSchool.EVOCATION), eq(5)))
          .thenReturn(List.of(fireballResponse));

      mockMvc.perform(get("/api/v1/spells/search")
              .param("name", "fire")
              .param("school", "EVOCATION")
              .param("maxLevel", "5"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Fireball"));
    }

    @Test
    @DisplayName("Should return empty list when no spells match search")
    void shouldReturnEmptyListWhenNoMatch() throws Exception {
      when(spellService.searchByName(eq("nonexistent"), isNull(), isNull()))
          .thenReturn(List.of());

      mockMvc.perform(get("/api/v1/spells/search")
              .param("name", "nonexistent"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(0));
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

      when(spellService.create(any(SpellRequest.class))).thenReturn(lightningBoltResponse);

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

    @Test
    @DisplayName("Should return 400 when level is null")
    void shouldReturn400WhenLevelNull() throws Exception {
      SpellRequest request = SpellRequest.builder()
          .name("Test Spell")
          .level(null)
          .school(SpellSchool.EVOCATION)
          .build();

      mockMvc.perform(post("/api/v1/spells")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when school is null")
    void shouldReturn400WhenSchoolNull() throws Exception {
      SpellRequest request = SpellRequest.builder()
          .name("Test Spell")
          .level(1)
          .school(null)
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

      when(spellService.update(anyLong(), any(SpellRequest.class)))
          .thenReturn(fireballResponse);

      mockMvc.perform(put("/api/v1/spells/1")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.name").value("Fireball"));
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
