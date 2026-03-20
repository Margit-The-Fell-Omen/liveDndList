package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.dto.request.LoginRequest;
import dev.ushki.livedndlist.dto.request.RegisterRequest;
import dev.ushki.livedndlist.dto.response.JwtResponse;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.enums.Role;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.mapper.UserMapper;
import dev.ushki.livedndlist.repository.UserRepository;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private UserMapper userMapper;

  private AuthService authService;

  private User testUser;
  private UserResponse testUserResponse;

  @BeforeEach
  void setUp() {
    CacheManager cacheManager = new CacheManager();

    authService = new AuthService(userRepository, passwordEncoder, authenticationManager,
        jwtTokenProvider, userMapper, cacheManager);

    testUser = User.builder()
        .id(1L)
        .username("testuser")
        .email("test@test.com")
        .password("encoded_password")
        .role(Role.ROLE_USER)
        .enabled(true)
        .build();

    testUserResponse = UserResponse.builder()
        .id(1L)
        .username("testuser")
        .email("test@test.com")
        .roles(Set.of(Role.ROLE_USER))
        .enabled(true)
        .build();
  }

  @Test
  @DisplayName("Should register user successfully")
  void shouldRegisterUserSuccessfully() {
    RegisterRequest request = RegisterRequest.builder()
        .username("newuser")
        .email("new@test.com")
        .password("password123")
        .build();

    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

    UserResponse result = authService.register(request);

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw exception when username exists")
  void shouldThrowExceptionWhenUsernameExists() {
    RegisterRequest request = RegisterRequest.builder()
        .username("existinguser")
        .email("new@test.com")
        .password("password123")
        .build();

    when(userRepository.existsByUsername("existinguser")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Username already exists");
  }

  @Test
  @DisplayName("Should throw exception when email exists")
  void shouldThrowExceptionWhenEmailExists() {
    RegisterRequest request = RegisterRequest.builder()
        .username("newuser")
        .email("existing@test.com")
        .password("password123")
        .build();

    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Email already exists");
  }

  @Test
  @DisplayName("Should login successfully")
  void shouldLoginSuccessfully() {
    LoginRequest request = LoginRequest.builder()
        .username("testuser")
        .password("password123")
        .build();

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUser);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(jwtTokenProvider.generateToken(any())).thenReturn("jwt-token");
    when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

    JwtResponse result = authService.login(request);

    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo("jwt-token");
  }
}
