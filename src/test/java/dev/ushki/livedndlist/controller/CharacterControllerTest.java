package dev.ushki.livedndlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.dto.request.AbilityScoresRequest;
import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.security.jwt.JwtAuthenticationEntryPoint;
import dev.ushki.livedndlist.security.jwt.JwtAuthenticationFilter;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import dev.ushki.livedndlist.service.CharacterService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
  private PageResponse<CharacterSummaryResponse> testPageResponse;

  @BeforeEach
  void setUp() {
    testCharacterResponse = CharacterResponse.builder()
        .id(1L)
        .name("Gandalf")
        .alignment(CharacterAlignment.NEUTRAL_GOOD)
        .totalLevel(5)
        .maxHitPoints(45)
        .currentHitPoints(45)
        .armorClass(15)
        .build();

    testCharacterSummary = CharacterSummaryResponse.builder()
        .id(1L)
        .name("Gandalf")
        .classDisplay("Wizard 5")
        .totalLevel(5)
        .currentHitPoints(45)
        .maxHitPoints(45)
        .updatedAt(LocalDateTime.now())
        .build();

    testPageResponse = PageResponse.<CharacterSummaryResponse>builder()
        .content(List.of(testCharacterSummary))
        .pageNumber(0)
        .pageSize(20)
        .totalElements(1)
        .totalPages(1)
        .first(true)
        .last(true)
        .empty(false)
        .build();
  }

  @Nested
  @DisplayName("GET /api/v1/characters")
  class GetAllCharactersTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return all user's characters with default pagination")
    void shouldReturnAllCharacters() throws Exception {
      when(characterService.getAllByUsername(
          eq("testuser"), isNull(), isNull(), any(Pageable.class)))
          .thenReturn(testPageResponse);

      mockMvc.perform(get("/api/v1/characters"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content[0].name").value("Gandalf"))
          .andExpect(jsonPath("$.data.pageNumber").value(0))
          .andExpect(jsonPath("$.data.pageSize").value(20))
          .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return characters filtered by level range with pagination")
    void shouldReturnCharactersFilteredByLevelRange() throws Exception {
      when(characterService.getAllByUsername(
          eq("testuser"), eq(1), eq(10), any(Pageable.class)))
          .thenReturn(testPageResponse);

      mockMvc.perform(get("/api/v1/characters")
              .param("minLevel", "1")
              .param("maxLevel", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return characters with custom pagination")
    void shouldReturnCharactersWithCustomPagination() throws Exception {
      when(characterService.getAllByUsername(
          eq("testuser"), isNull(), isNull(), any(Pageable.class)))
          .thenReturn(testPageResponse);

      mockMvc.perform(get("/api/v1/characters")
              .param("page", "0")
              .param("size", "10")
              .param("sort", "name,asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return characters with all filters applied")
    void shouldReturnCharactersWithAllFilters() throws Exception {
      when(characterService.getAllByUsername(
          eq("testuser"), eq(3), eq(15), any(Pageable.class)))
          .thenReturn(testPageResponse);

      mockMvc.perform(get("/api/v1/characters")
              .param("minLevel", "3")
              .param("maxLevel", "15")
              .param("page", "0")
              .param("size", "20")
              .param("sort", "updatedAt,desc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content[0].name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/search")
  class SearchCharactersTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should search characters by name with pagination")
    void shouldSearchCharactersByName() throws Exception {
      when(characterService.searchByName(eq("testuser"), eq("Gandalf"), any(Pageable.class)))
          .thenReturn(testPageResponse);

      mockMvc.perform(get("/api/v1/characters/search")
              .param("name", "Gandalf"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content[0].name").value("Gandalf"))
          .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return empty page when no matches found")
    void shouldReturnEmptyPageWhenNoMatches() throws Exception {
      PageResponse<CharacterSummaryResponse> emptyPage =
          PageResponse.<CharacterSummaryResponse>builder()
              .content(List.of())
              .pageNumber(0)
              .pageSize(20)
              .totalElements(0)
              .totalPages(0)
              .first(true)
              .last(true)
              .empty(true)
              .build();

      when(characterService.searchByName(eq("testuser"), eq("NonExistent"), any(Pageable.class)))
          .thenReturn(emptyPage);

      mockMvc.perform(get("/api/v1/characters/search")
              .param("name", "NonExistent"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content").isEmpty())
          .andExpect(jsonPath("$.data.empty").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should search with partial name match and pagination")
    void shouldSearchWithPartialNameMatch() throws Exception {
      when(characterService.searchByName(eq("testuser"), eq("Gan"), any(Pageable.class)))
          .thenReturn(testPageResponse);

      mockMvc.perform(get("/api/v1/characters/search")
              .param("name", "Gan")
              .param("page", "0")
              .param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content[0].name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/search")
              .param("name", "Gandalf"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/recent")
  class GetRecentCharactersTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return recent characters")
    void shouldReturnRecentCharacters() throws Exception {
      when(characterService.getRecentCharacters("testuser"))
          .thenReturn(List.of(testCharacterSummary));

      mockMvc.perform(get("/api/v1/characters/recent"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/recent"))
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
          .andExpect(jsonPath("$.data.name").value("Gandalf"));
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
          .raceId(1L)
          .classId(1L)
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
          .andExpect(jsonPath("$.message").value("Character created successfully"))
          .andExpect(jsonPath("$.data.name").value("Legolas"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return 400 when name is blank")
    void shouldReturn400WhenNameBlank() throws Exception {
      CharacterCreateRequest request = CharacterCreateRequest.builder()
          .name("")
          .build();

      mockMvc.perform(post("/api/v1/characters")
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return 400 when max hit points is negative")
    void shouldReturn400WhenMaxHitPointsNegative() throws Exception {
      CharacterCreateRequest request = CharacterCreateRequest.builder()
          .name("Legolas")
          .maxHitPoints(-10)
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
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Character updated successfully"))
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
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Character deleted successfully"));
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
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Equipment added"));
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
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Equipment removed"));
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
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Spell added"));
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
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Spell removed"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/{id}/sheet")
  class GetCharacterSheetTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return character sheet")
    void shouldReturnCharacterSheet() throws Exception {
      when(characterService.getCharacterSheet(1L, "testuser")).thenReturn(testCharacterResponse);

      mockMvc.perform(get("/api/v1/characters/1/sheet"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(1))
          .andExpect(jsonPath("$.data.name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/1/sheet"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/{id}/combat")
  class GetCharacterForCombatTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return character combat view")
    void shouldReturnCharacterCombatView() throws Exception {
      when(characterService.getCharacterForCombat(1L, "testuser")).thenReturn(
          testCharacterResponse);

      mockMvc.perform(get("/api/v1/characters/1/combat"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(1))
          .andExpect(jsonPath("$.data.name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/1/combat"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/{id}/spellcasting")
  class GetCharacterForSpellcastingTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return character spellcasting view")
    void shouldReturnCharacterSpellcastingView() throws Exception {
      when(characterService.getCharacterForSpellcasting(1L, "testuser")).thenReturn(
          testCharacterResponse);

      mockMvc.perform(get("/api/v1/characters/1/spellcasting"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(1))
          .andExpect(jsonPath("$.data.name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/1/spellcasting"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/{id}/skills")
  class GetCharacterWithSkillsTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return character with skills")
    void shouldReturnCharacterWithSkills() throws Exception {
      when(characterService.getCharacterWithSkills(1L, "testuser")).thenReturn(
          testCharacterResponse);

      mockMvc.perform(get("/api/v1/characters/1/skills"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(1))
          .andExpect(jsonPath("$.data.name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/1/skills"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/{id}/inventory")
  class GetCharacterWithEquipmentTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return character with equipment")
    void shouldReturnCharacterWithEquipment() throws Exception {
      when(characterService.getCharacterWithEquipment(1L, "testuser")).thenReturn(
          testCharacterResponse);

      mockMvc.perform(get("/api/v1/characters/1/inventory"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(1))
          .andExpect(jsonPath("$.data.name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/1/inventory"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/{id}/saving-throws")
  class GetCharacterWithSavingThrowsTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return character with saving throws")
    void shouldReturnCharacterWithSavingThrows() throws Exception {
      when(characterService.getCharacterWithSavingThrows(1L, "testuser")).thenReturn(
          testCharacterResponse);

      mockMvc.perform(get("/api/v1/characters/1/saving-throws"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(1))
          .andExpect(jsonPath("$.data.name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/1/saving-throws"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/{id}/summary")
  class GetCharacterSummaryTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return character summary")
    void shouldReturnCharacterSummary() throws Exception {
      when(characterService.getCharacterSummary(1L, "testuser")).thenReturn(testCharacterResponse);

      mockMvc.perform(get("/api/v1/characters/1/summary"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(1))
          .andExpect(jsonPath("$.data.name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/1/summary"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/characters/{id}/spells")
  class GetCharacterWithSpellsTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return character with spells")
    void shouldReturnCharacterWithSpells() throws Exception {
      when(characterService.getCharacterWithSpells(1L, "testuser")).thenReturn(
          testCharacterResponse);

      mockMvc.perform(get("/api/v1/characters/1/spells"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(1))
          .andExpect(jsonPath("$.data.name").value("Gandalf"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/characters/1/spells"))
          .andExpect(status().isUnauthorized());
    }
  }
}
