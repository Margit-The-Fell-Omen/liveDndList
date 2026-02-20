package dev.ushki.live_dnd_list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.live_dnd_list.dto.request.LoginRequest;
import dev.ushki.live_dnd_list.dto.request.RegisterRequest;
import dev.ushki.live_dnd_list.dto.response.JwtResponse;
import dev.ushki.live_dnd_list.dto.response.UserResponse;
import dev.ushki.live_dnd_list.enums.Role;
import dev.ushki.live_dnd_list.security.jwt.JwtAuthenticationEntryPoint;
import dev.ushki.live_dnd_list.security.jwt.JwtAuthenticationFilter;
import dev.ushki.live_dnd_list.security.jwt.JwtTokenProvider;
import dev.ushki.live_dnd_list.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserResponse testUserResponse;
    private JwtResponse testJwtResponse;

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

        testJwtResponse = JwtResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .user(testUserResponse)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .username("newuser")
                    .email("newuser@test.com")
                    .password("password123")
                    .build();

            when(authService.register(any(RegisterRequest.class))).thenReturn(testUserResponse);

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value("testuser"));
        }

        @Test
        @DisplayName("Should return 400 when username is blank")
        void shouldReturn400WhenUsernameBlank() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .username("")
                    .email("test@test.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email is invalid")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("invalid-email")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is too short")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("test@test.com")
                    .password("123")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            when(authService.login(any(LoginRequest.class))).thenReturn(testJwtResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("Should return 400 when username is blank")
        void shouldReturn400WhenUsernameBlank() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .username("")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
