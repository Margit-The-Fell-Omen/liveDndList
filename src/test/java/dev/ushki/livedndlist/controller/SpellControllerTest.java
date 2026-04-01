package dev.ushki.livedndlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
    @DisplayName("Should return spells filtered by minimum level")
    void shouldReturnSpellsFilteredByMinLevel() throws Exception {
      when(spellService.getAllSpells(
          isNull(), eq(3), isNull(), isNull(), isNull(),
          eq("name"), eq("asc")))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("minLevel", "3"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].level").value(3));
    }

    @Test
    @DisplayName("Should return spells filtered by maximum level")
    void shouldReturnSpellsFilteredByMaxLevel() throws Exception {
      when(spellService.getAllSpells(
          isNull(), isNull(), eq(1), isNull(), isNull(),
          eq("name"), eq("asc")))
          .thenReturn(List.of(shieldResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("maxLevel", "1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].level").value(1));
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
    @DisplayName("Should return spells filtered by concentration true")
    void shouldReturnSpellsFilteredByConcentrationTrue() throws Exception {
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
    @DisplayName("Should return spells filtered by concentration false")
    void shouldReturnSpellsFilteredByConcentrationFalse() throws Exception {
      when(spellService.getAllSpells(
          isNull(), isNull(), isNull(), isNull(), eq(false),
          eq("name"), eq("asc")))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse, shieldResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("concentration", "false"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("Should return spells filtered by ritual true")
    void shouldReturnSpellsFilteredByRitualTrue() throws Exception {
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
    @DisplayName("Should return spells filtered by ritual false")
    void shouldReturnSpellsFilteredByRitualFalse() throws Exception {
      when(spellService.getAllSpells(
          isNull(), isNull(), isNull(), eq(false), isNull(),
          eq("name"), eq("asc")))
          .thenReturn(List.of(fireballResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("ritual", "false"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return spells with custom sorting ascending")
    void shouldReturnSpellsWithCustomSortingAscending() throws Exception {
      when(spellService.getAllSpells(
          isNull(), isNull(), isNull(), isNull(), isNull(),
          eq("level"), eq("asc")))
          .thenReturn(List.of(shieldResponse, fireballResponse, lightningBoltResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("sortBy", "level")
              .param("sortDir", "asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return spells with custom sorting descending")
    void shouldReturnSpellsWithCustomSortingDescending() throws Exception {
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

    @Test
    @DisplayName("Should return spells with all filters applied")
    void shouldReturnSpellsWithAllFilters() throws Exception {
      when(spellService.getAllSpells(
          SpellSchool.EVOCATION, 1, 5, false, false,
          "level", "desc"))
          .thenReturn(List.of(fireballResponse));

      mockMvc.perform(get("/api/v1/spells")
              .param("school", "EVOCATION")
              .param("minLevel", "1")
              .param("maxLevel", "5")
              .param("ritual", "false")
              .param("concentration", "false")
              .param("sortBy", "level")
              .param("sortDir", "desc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("Should return empty list when no spells match")
    void shouldReturnEmptyListWhenNoSpellsMatch() throws Exception {
      when(spellService.getAllSpells(
          eq(SpellSchool.NECROMANCY), isNull(), isNull(), isNull(), isNull(),
          eq("name"), eq("asc")))
          .thenReturn(List.of());

      mockMvc.perform(get("/api/v1/spells")
              .param("school", "NECROMANCY"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(0));
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

    @Test
    @DisplayName("Should return spell with all fields")
    void shouldReturnSpellWithAllFields() throws Exception {
      when(spellService.getById(1L)).thenReturn(fireballResponse);

      mockMvc.perform(get("/api/v1/spells/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.id").value(1))
          .andExpect(jsonPath("$.data.name").value("Fireball"))
          .andExpect(jsonPath("$.data.level").value(3))
          .andExpect(jsonPath("$.data.school").value("EVOCATION"))
          .andExpect(jsonPath("$.data.castingTime").value("1 action"))
          .andExpect(jsonPath("$.data.range").value("150 feet"))
          .andExpect(jsonPath("$.data.components").value("V, S, M"))
          .andExpect(jsonPath("$.data.duration").value("Instantaneous"))
          .andExpect(jsonPath("$.data.concentration").value(false))
          .andExpect(jsonPath("$.data.ritual").value(false));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/spells/search")
  class SearchSpellsTests {

    @Test
    @DisplayName("Should search spells by name")
    void shouldSearchSpellsByName() throws Exception {
      when(spellService.searchByName(eq("fire"), isNull(), isNull(), any(Pageable.class)))
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
      when(spellService.searchByName(eq("bolt"), eq(SpellSchool.EVOCATION), isNull(),
          any(Pageable.class)))
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
      when(spellService.searchByName(eq("shield"), isNull(), eq(2), any(Pageable.class)))
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
      when(spellService.searchByName(eq("fire"), eq(SpellSchool.EVOCATION), eq(5),
          any(Pageable.class)))
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
      when(spellService.searchByName(eq("nonexistent"), isNull(), isNull(), any(Pageable.class)))
          .thenReturn(List.of());

      mockMvc.perform(get("/api/v1/spells/search")
              .param("name", "nonexistent"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Should search with pagination parameters")
    void shouldSearchWithPaginationParameters() throws Exception {
      when(spellService.searchByName(eq("fire"), isNull(), isNull(), any(Pageable.class)))
          .thenReturn(List.of(fireballResponse));

      mockMvc.perform(get("/api/v1/spells/search")
              .param("name", "fire")
              .param("page", "0")
              .param("size", "10")
              .param("sort", "name,desc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true));
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
          .andExpect(jsonPath("$.message").value("Spell created"))
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
          .andExpect(jsonPath("$.message").value("Spell updated"))
          .andExpect(jsonPath("$.data.name").value("Fireball"));
    }

    @Test
    @DisplayName("Should return 400 when update request is invalid")
    void shouldReturn400WhenUpdateRequestInvalid() throws Exception {
      SpellRequest request = SpellRequest.builder()
          .name("")
          .level(3)
          .school(SpellSchool.EVOCATION)
          .build();

      mockMvc.perform(put("/api/v1/spells/1")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
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

    @Test
    @DisplayName("Should delete spell by different ID")
    void shouldDeleteSpellByDifferentId() throws Exception {
      doNothing().when(spellService).delete(999L);

      mockMvc.perform(delete("/api/v1/spells/999"))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/spells/bulk")
  class CreateBulkSpellsTests {

    @Test
    @DisplayName("Should create multiple spells successfully")
    void shouldCreateMultipleSpellsSuccessfully() throws Exception {
      SpellRequest request1 = SpellRequest.builder()
          .name("Fireball")
          .level(3)
          .school(SpellSchool.EVOCATION)
          .description("A bright streak flashes from your pointing finger...")
          .build();
      SpellRequest request2 = SpellRequest.builder()
          .name("Lightning Bolt")
          .level(3)
          .school(SpellSchool.EVOCATION)
          .description("A stroke of lightning forming a line...")
          .build();
      List<SpellRequest> requests = List.of(request1, request2);

      when(spellService.createBulk(anyList()))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse));

      mockMvc.perform(post("/api/v1/spells/bulk")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(requests)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("2 spells created successfully"))
          .andExpect(jsonPath("$.data.length()").value(2))
          .andExpect(jsonPath("$.data[0].name").value("Fireball"))
          .andExpect(jsonPath("$.data[1].name").value("Lightning Bolt"));
    }

    @Test
    @DisplayName("Should create single spell in bulk")
    void shouldCreateSingleSpellInBulk() throws Exception {
      SpellRequest request = SpellRequest.builder()
          .name("Fireball")
          .level(3)
          .school(SpellSchool.EVOCATION)
          .description("A bright streak flashes from your pointing finger...")
          .build();
      List<SpellRequest> requests = List.of(request);

      when(spellService.createBulk(anyList()))
          .thenReturn(List.of(fireballResponse));

      mockMvc.perform(post("/api/v1/spells/bulk")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(requests)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("1 spells created successfully"))
          .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("Should create three spells in bulk")
    void shouldCreateThreeSpellsInBulk() throws Exception {
      SpellRequest request1 = SpellRequest.builder()
          .name("Fireball")
          .level(3)
          .school(SpellSchool.EVOCATION)
          .description("A bright streak flashes...")
          .build();
      SpellRequest request2 = SpellRequest.builder()
          .name("Lightning Bolt")
          .level(3)
          .school(SpellSchool.EVOCATION)
          .description("A stroke of lightning...")
          .build();
      SpellRequest request3 = SpellRequest.builder()
          .name("Shield")
          .level(1)
          .school(SpellSchool.ABJURATION)
          .description("An invisible barrier of magical force...")
          .build();
      List<SpellRequest> requests = List.of(request1, request2, request3);

      when(spellService.createBulk(anyList()))
          .thenReturn(List.of(fireballResponse, lightningBoltResponse, shieldResponse));

      mockMvc.perform(post("/api/v1/spells/bulk")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(requests)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("3 spells created successfully"))
          .andExpect(jsonPath("$.data.length()").value(3));
    }
  }
}
