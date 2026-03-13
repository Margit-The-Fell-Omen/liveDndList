package dev.ushki.livedndlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.dto.request.UserUpdateRequest;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.enums.Role;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import dev.ushki.livedndlist.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  private UserResponse testUserResponse;
  private UserResponse adminUserResponse;

  @BeforeEach
  void setUp() {
    testUserResponse = UserResponse.builder()
        .id(1L)
        .username("testuser")
        .email("test@test.com")
        .roles(Set.of(Role.ROLE_USER))
        .enabled(true)
        .createdAt(LocalDateTime.now())
        .build();

    adminUserResponse = UserResponse.builder()
        .id(2L)
        .username("admin")
        .email("admin@test.com")
        .roles(Set.of(Role.ROLE_ADMIN))
        .enabled(true)
        .createdAt(LocalDateTime.now())
        .build();
  }

  @Nested
  @DisplayName("GET /api/v1/users")
  class GetAllUsersTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return all users without filters")
    void shouldReturnAllUsersWithoutFilters() throws Exception {
      when(userService.getAllUsers(isNull(), isNull()))
          .thenReturn(List.of(testUserResponse, adminUserResponse));

      mockMvc.perform(get("/api/v1/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(2))
          .andExpect(jsonPath("$.data[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return users filtered by enabled status")
    void shouldReturnUsersFilteredByEnabled() throws Exception {
      when(userService.getAllUsers(eq(true), isNull()))
          .thenReturn(List.of(testUserResponse));

      mockMvc.perform(get("/api/v1/users")
              .param("enabled", "true"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].enabled").value(true));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return users filtered by enabled and role")
    void shouldReturnUsersFilteredByEnabledAndRole() throws Exception {
      when(userService.getAllUsers(true, "ROLE_USER"))
          .thenReturn(List.of(testUserResponse));

      mockMvc.perform(get("/api/v1/users")
              .param("enabled", "true")
              .param("role", "ROLE_USER"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].username").value("testuser"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/users"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/users/search")
  class SearchUsersTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should search users by query")
    void shouldSearchUsersByQuery() throws Exception {
      when(userService.searchUsers("test", any(Pageable.class)))
          .thenReturn(List.of(testUserResponse));

      mockMvc.perform(get("/api/v1/users/search")
              .param("query", "test"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return empty list when no users match query")
    void shouldReturnEmptyListWhenNoMatch() throws Exception {
      when(userService.searchUsers("nonexistent", any(Pageable.class)))
          .thenReturn(List.of());

      mockMvc.perform(get("/api/v1/users/search")
              .param("query", "nonexistent"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should search users by email")
    void shouldSearchUsersByEmail() throws Exception {
      when(userService.searchUsers("@test.com", any(Pageable.class)))
          .thenReturn(List.of(testUserResponse, adminUserResponse));

      mockMvc.perform(get("/api/v1/users/search")
              .param("query", "@test.com"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(2));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/users/me")
  class GetCurrentUserTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return current user")
    void shouldReturnCurrentUser() throws Exception {
      when(userService.getUserByUsername("testuser")).thenReturn(testUserResponse);

      mockMvc.perform(get("/api/v1/users/me"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/users/me"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/users/{id}")
  class GetUserByIdTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return user by ID")
    void shouldReturnUserById() throws Exception {
      when(userService.getUserById(1L)).thenReturn(testUserResponse);

      mockMvc.perform(get("/api/v1/users/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(1));
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/users/{id}")
  class UpdateUserTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should update user")
    void shouldUpdateUser() throws Exception {
      UserUpdateRequest request = UserUpdateRequest.builder()
          .username("updateduser")
          .email("updated@test.com")
          .build();

      UserResponse updatedResponse = UserResponse.builder()
          .id(1L)
          .username("updateduser")
          .email("updated@test.com")
          .roles(Set.of(Role.ROLE_USER))
          .enabled(true)
          .build();

      when(userService.updateUser(anyLong(), any(UserUpdateRequest.class)))
          .thenReturn(updatedResponse);

      mockMvc.perform(put("/api/v1/users/1")
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.username").value("updateduser"));
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/users/{id}")
  class DeleteUserTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should delete user")
    void shouldDeleteUser() throws Exception {
      doNothing().when(userService).deleteUser(1L);

      mockMvc.perform(delete("/api/v1/users/1")
              .with(csrf()))
          .andExpect(status().isNoContent());
    }
  }
}
