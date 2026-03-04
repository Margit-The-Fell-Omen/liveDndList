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
  @Order(5)
  @DisplayName("5. Should get all user characters")
  void shouldGetAllUserCharacters() throws Exception {
    mockMvc.perform(get("/api/v1/characters")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].name").value("Integration Hero"));
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
  @Order(8)
  @DisplayName("8. Should add spell to character")
  void shouldAddSpellToCharacter() throws Exception {
    // Assuming spell with ID 1 exists (created by DataInitializer)
    mockMvc.perform(post("/api/v1/characters/" + characterId + "/spells/1")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @Order(9)
  @DisplayName("9. Should return 401 when accessing protected endpoint without token")
  void shouldReturn401WhenNoToken() throws Exception {
    mockMvc.perform(get("/api/v1/characters"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Order(10)
  @DisplayName("10. Should delete character")
  void shouldDeleteCharacter() throws Exception {
    mockMvc.perform(delete("/api/v1/characters/" + characterId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    // Verify deletion
    mockMvc.perform(get("/api/v1/characters/" + characterId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound());
  }
}
