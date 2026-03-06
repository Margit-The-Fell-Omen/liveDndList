package dev.ushki.livedndlist.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.LoginRequest;
import dev.ushki.livedndlist.dto.request.RegisterRequest;
import dev.ushki.livedndlist.enums.CharacterRace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private static String accessToken;
  private static Long characterId;

  @BeforeEach
  void setUp() {
    // Clean up before each test class run
  }

  @Test
  @Order(1)
  @DisplayName("1. Should register a new user")
  void shouldRegisterNewUser() throws Exception {
    RegisterRequest request = RegisterRequest.builder()
        .username("integrationuser")
        .email("integration@test.com")
        .password("password123")
        .build();

    MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("integrationuser"))
        .andReturn();

    String response = result.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(response);
    Long userId = jsonNode.get("data").get("id").asLong();

    assertThat(userId).isNotNull();
  }

  @Test
  @Order(2)
  @DisplayName("2. Should login with registered user")
  void shouldLoginWithRegisteredUser() throws Exception {
    LoginRequest request = LoginRequest.builder()
        .username("integrationuser")
        .password("password123")
        .build();

    MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.accessToken").exists())
        .andReturn();

    String response = result.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(response);
    accessToken = jsonNode.get("data").get("accessToken").asText();

    assertThat(accessToken).isNotBlank();
  }

  @Test
  @Order(3)
  @DisplayName("3. Should get current user profile")
  void shouldGetCurrentUserProfile() throws Exception {
    mockMvc.perform(get("/api/v1/users/me")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("integrationuser"));
  }

  @Test
  @Order(4)
  @DisplayName("4. Should create a new character")
  void shouldCreateNewCharacter() throws Exception {
    CharacterCreateRequest request = CharacterCreateRequest.builder()
        .name("Integration Hero")
        .race(CharacterRace.HUMAN)
        .className("Fighter")
        .maxHitPoints(12)
        .build();

    MvcResult result = mockMvc.perform(post("/api/v1/characters")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("Integration Hero"))
        .andReturn();

    String response = result.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(response);
    characterId = jsonNode.get("data").get("id").asLong();

    assertThat(characterId).isNotNull();
  }

  @Test
  @Order(6)
  @DisplayName("6. Should get character by ID")
  void shouldGetCharacterById() throws Exception {
    mockMvc.perform(get("/api/v1/characters/" + characterId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("Integration Hero"));
  }

  @Test
  @Order(7)
  @DisplayName("7. Should get all spells (public endpoint)")
  void shouldGetAllSpells() throws Exception {
    mockMvc.perform(get("/api/v1/spells"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  @Order(9)
  @DisplayName("9. Should return 401 when accessing protected endpoint without token")
  void shouldReturn401WhenNoToken() throws Exception {
    mockMvc.perform(get("/api/v1/characters"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Order(11)
  @DisplayName("11. Should search characters with pagination")
  void shouldSearchCharactersWithPagination() throws Exception {
    // First create another character for search test
    CharacterCreateRequest request = CharacterCreateRequest.builder()
        .name("Searchable Hero")
        .race(CharacterRace.ELF)
        .className("Wizard")
        .maxHitPoints(8)
        .build();

    MvcResult createResult = mockMvc.perform(post("/api/v1/characters")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();

    String createResponse = createResult.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(createResponse);
    long searchableCharacterId = jsonNode.get("data").get("id").asLong();

    // Search for the character
    mockMvc.perform(get("/api/v1/characters/search")
            .header("Authorization", "Bearer " + accessToken)
            .param("name", "Searchable"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content[0].name").value("Searchable Hero"))
        .andExpect(jsonPath("$.data.totalElements").value(1));

    // Cleanup
    mockMvc.perform(delete("/api/v1/characters/" + searchableCharacterId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  @Test
  @Order(12)
  @DisplayName("12. Should get recent characters")
  void shouldGetRecentCharacters() throws Exception {
    // Create a character for recent test
    CharacterCreateRequest request = CharacterCreateRequest.builder()
        .name("Recent Hero")
        .race(CharacterRace.DWARF)
        .className("Cleric")
        .maxHitPoints(10)
        .build();

    MvcResult createResult = mockMvc.perform(post("/api/v1/characters")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();

    String createResponse = createResult.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(createResponse);
    long recentCharacterId = jsonNode.get("data").get("id").asLong();

    // Get recent characters (returns List, not PageResponse)
    mockMvc.perform(get("/api/v1/characters/recent")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].name").value("Recent Hero"));

    // Cleanup
    mockMvc.perform(delete("/api/v1/characters/" + recentCharacterId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  @Test
  @Order(13)
  @DisplayName("13. Should filter characters by race with pagination")
  void shouldFilterCharactersByRace() throws Exception {
    // Create elf character
    CharacterCreateRequest elfRequest = CharacterCreateRequest.builder()
        .name("Elf Hero")
        .race(CharacterRace.ELF)
        .className("Ranger")
        .maxHitPoints(10)
        .build();

    MvcResult elfResult = mockMvc.perform(post("/api/v1/characters")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(elfRequest)))
        .andExpect(status().isCreated())
        .andReturn();

    String elfResponse = elfResult.getResponse().getContentAsString();
    long elfCharacterId = objectMapper.readTree(elfResponse).get("data").get("id").asLong();

    // Create human character
    CharacterCreateRequest humanRequest = CharacterCreateRequest.builder()
        .name("Human Hero")
        .race(CharacterRace.HUMAN)
        .className("Fighter")
        .maxHitPoints(12)
        .build();

    MvcResult humanResult = mockMvc.perform(post("/api/v1/characters")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(humanRequest)))
        .andExpect(status().isCreated())
        .andReturn();

    String humanResponse = humanResult.getResponse().getContentAsString();
    long humanCharacterId = objectMapper.readTree(humanResponse).get("data").get("id").asLong();

    // Filter by ELF race
    mockMvc.perform(get("/api/v1/characters")
            .header("Authorization", "Bearer " + accessToken)
            .param("race", "ELF"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content[0].race").value("ELF"));

    // Cleanup
    mockMvc.perform(delete("/api/v1/characters/" + elfCharacterId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/api/v1/characters/" + humanCharacterId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }
}
