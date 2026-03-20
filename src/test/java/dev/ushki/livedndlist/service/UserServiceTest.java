package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.dto.request.UserUpdateRequest;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.enums.Role;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.UserMapper;
import dev.ushki.livedndlist.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  private UserService userService;

  private User testUser;
  private User adminUser;
  private User disabledUser;
  private UserResponse testUserResponse;
  private UserResponse adminUserResponse;
  private UserResponse disabledUserResponse;

  @BeforeEach
  void setUp() {
    CacheManager cacheManager = new CacheManager();

    userService = new UserService(userRepository, userMapper, cacheManager);

    testUser = User.builder()
        .id(1L)
        .username("testuser")
        .email("test@test.com")
        .password("encoded_password")
        .role(Role.ROLE_USER)
        .enabled(true)
        .build();

    adminUser = User.builder()
        .id(2L)
        .username("adminuser")
        .email("admin@test.com")
        .password("encoded_password")
        .role(Role.ROLE_ADMIN)
        .enabled(true)
        .build();

    disabledUser = User.builder()
        .id(3L)
        .username("disableduser")
        .email("disabled@test.com")
        .password("encoded_password")
        .role(Role.ROLE_USER)
        .enabled(false)
        .build();

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
        .username("adminuser")
        .email("admin@test.com")
        .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
        .enabled(true)
        .createdAt(LocalDateTime.now())
        .build();

    disabledUserResponse = UserResponse.builder()
        .id(3L)
        .username("disableduser")
        .email("disabled@test.com")
        .roles(Set.of(Role.ROLE_USER))
        .enabled(false)
        .createdAt(LocalDateTime.now())
        .build();
  }

  @Nested
  @DisplayName("Get All Users")
  class GetAllUsersTests {

    @Test
    @DisplayName("Should get all users without filters")
    void shouldGetAllUsers() {
      when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser, disabledUser));
      when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
      when(userMapper.toResponse(adminUser)).thenReturn(adminUserResponse);
      when(userMapper.toResponse(disabledUser)).thenReturn(disabledUserResponse);

      List<UserResponse> result = userService.getAllUsers(null, null);

      assertThat(result).hasSize(3);
      verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should filter users by enabled status true")
    void shouldFilterUsersByEnabledTrue() {
      when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser, disabledUser));
      when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
      when(userMapper.toResponse(adminUser)).thenReturn(adminUserResponse);

      List<UserResponse> result = userService.getAllUsers(true, null);

      assertThat(result).hasSize(2).allMatch(UserResponse::isEnabled);
    }

    @Test
    @DisplayName("Should filter users by enabled status false")
    void shouldFilterUsersByEnabledFalse() {
      when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser, disabledUser));
      when(userMapper.toResponse(disabledUser)).thenReturn(disabledUserResponse);

      List<UserResponse> result = userService.getAllUsers(false, null);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should filter users by role USER")
    void shouldFilterUsersByRoleUser() {
      when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser, disabledUser));
      when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
      when(userMapper.toResponse(disabledUser)).thenReturn(disabledUserResponse);

      List<UserResponse> result = userService.getAllUsers(null, "ROLE_USER");

      assertThat(result).hasSize(2).allMatch(u -> u.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    @DisplayName("Should filter users by role ADMIN")
    void shouldFilterUsersByRoleAdmin() {
      when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser, disabledUser));
      when(userMapper.toResponse(adminUser)).thenReturn(adminUserResponse);

      List<UserResponse> result = userService.getAllUsers(null, "ROLE_ADMIN");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getRoles()).contains(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should apply both enabled and role filters")
    void shouldApplyBothFilters() {
      when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser, disabledUser));
      when(userMapper.toResponse(adminUser)).thenReturn(adminUserResponse);

      List<UserResponse> result = userService.getAllUsers(true, "ROLE_ADMIN");

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().isEnabled()).isTrue();
      assertThat(result.getFirst().getRoles()).contains(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should return empty list when no users match filters")
    void shouldReturnEmptyListWhenNoMatch() {
      when(userRepository.findAll()).thenReturn(List.of(testUser));

      List<UserResponse> result = userService.getAllUsers(null, "ROLE_ADMIN");

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("Search Users")
  class SearchUsersTests {

    @Test
    @DisplayName("Should search users by username")
    void shouldSearchUsersByUsername() {
      Pageable pageable = Pageable.unpaged();

      when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
          eq("test"), eq("test"), any(Pageable.class))).thenReturn(List.of(testUser));
      when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

      List<UserResponse> result = userService.searchUsers("test", pageable);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getUsername()).contains("test");
    }

    @Test
    @DisplayName("Should search users by email")
    void shouldSearchUsersByEmail() {
      Pageable pageable = Pageable.unpaged();

      when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
          eq("admin@test.com"), eq("admin@test.com"), any(Pageable.class))).thenReturn(
          List.of(adminUser));
      when(userMapper.toResponse(adminUser)).thenReturn(adminUserResponse);

      List<UserResponse> result = userService.searchUsers("admin@test.com", pageable);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getEmail()).contains("admin@test.com");
    }

    @Test
    @DisplayName("Should search users by partial match")
    void shouldSearchUsersByPartialMatch() {
      Pageable pageable = Pageable.unpaged();

      when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
          eq("user"), eq("user"), any(Pageable.class))).thenReturn(
          List.of(testUser, adminUser, disabledUser));
      when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
      when(userMapper.toResponse(adminUser)).thenReturn(adminUserResponse);
      when(userMapper.toResponse(disabledUser)).thenReturn(disabledUserResponse);

      List<UserResponse> result = userService.searchUsers("user", pageable);

      assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Should return empty list when no users match search")
    void shouldReturnEmptyListWhenNoMatch() {
      Pageable pageable = Pageable.unpaged();

      when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
          eq("nonexistent"), eq("nonexistent"), any(Pageable.class))).thenReturn(List.of());

      List<UserResponse> result = userService.searchUsers("nonexistent", pageable);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should search users case-insensitively")
    void shouldSearchCaseInsensitively() {
      Pageable pageable = Pageable.unpaged();

      when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
          eq("TEST"), eq("TEST"), any(Pageable.class))).thenReturn(List.of(testUser));
      when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

      List<UserResponse> result = userService.searchUsers("TEST", pageable);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getUsername()).isEqualToIgnoringCase("testuser");
    }
  }

  @Nested
  @DisplayName("Get User By ID")
  class GetUserByIdTests {

    @Test
    @DisplayName("Should get user by ID")
    void shouldGetUserById() {
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

      UserResponse result = userService.getUserById(1L);

      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("testuser");
      verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.getUserById(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User")
          .hasMessageContaining("999");
    }
  }

  @Nested
  @DisplayName("Get User By Username")
  class GetUserByUsernameTests {

    @Test
    @DisplayName("Should get user by username")
    void shouldGetUserByUsername() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

      UserResponse result = userService.getUserByUsername("testuser");

      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("testuser");
      verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw exception when user not found by username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
      when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User")
          .hasMessageContaining("nonexistent");
    }
  }

  @Nested
  @DisplayName("Update User")
  class UpdateUserTests {

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
      UserUpdateRequest request = UserUpdateRequest.builder()
          .username("updateduser")
          .email("updated@test.com")
          .build();

      User updatedUser = User.builder()
          .id(1L)
          .username("updateduser")
          .email("updated@test.com")
          .password("encoded_password")
          .role(Role.ROLE_USER)
          .enabled(true)
          .build();

      UserResponse updatedResponse = UserResponse.builder()
          .id(1L)
          .username("updateduser")
          .email("updated@test.com")
          .roles(Set.of(Role.ROLE_USER))
          .enabled(true)
          .build();

      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.existsByUsername("updateduser")).thenReturn(false);
      when(userRepository.existsByEmail("updated@test.com")).thenReturn(false);
      when(userRepository.save(any(User.class))).thenReturn(updatedUser);
      when(userMapper.toResponse(any(User.class))).thenReturn(updatedResponse);

      UserResponse result = userService.updateUser(1L, request);

      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("updateduser");
      assertThat(result.getEmail()).isEqualTo("updated@test.com");
      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should update only username")
    void shouldUpdateOnlyUsername() {
      UserUpdateRequest request = UserUpdateRequest.builder()
          .username("newusername")
          .build();

      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.existsByUsername("newusername")).thenReturn(false);
      when(userRepository.save(any(User.class))).thenReturn(testUser);
      when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

      UserResponse result = userService.updateUser(1L, request);

      assertThat(result).isNotNull();
      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should update only email")
    void shouldUpdateOnlyEmail() {
      UserUpdateRequest request = UserUpdateRequest.builder()
          .email("newemail@test.com")
          .build();

      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.existsByEmail("newemail@test.com")).thenReturn(false);
      when(userRepository.save(any(User.class))).thenReturn(testUser);
      when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

      UserResponse result = userService.updateUser(1L, request);

      assertThat(result).isNotNull();
      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with existing username")
    void shouldThrowExceptionWhenUpdatingWithExistingUsername() {
      UserUpdateRequest request = UserUpdateRequest.builder()
          .username("existinguser")
          .build();

      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.existsByUsername("existinguser")).thenReturn(true);

      assertThatThrownBy(() -> userService.updateUser(1L, request))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("Username already exists");

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow updating with same username")
    void shouldAllowUpdatingWithSameUsername() {
      UserUpdateRequest request = UserUpdateRequest.builder()
          .username("testuser")
          .build();

      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class))).thenReturn(testUser);
      when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

      UserResponse result = userService.updateUser(1L, request);

      assertThat(result).isNotNull();
      verify(userRepository).save(any(User.class));
      verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    @DisplayName("Should allow updating with same email")
    void shouldAllowUpdatingWithSameEmail() {
      UserUpdateRequest request = UserUpdateRequest.builder()
          .email("test@test.com")
          .build();

      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class))).thenReturn(testUser);
      when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

      UserResponse result = userService.updateUser(1L, request);

      assertThat(result).isNotNull();
      verify(userRepository).save(any(User.class));
      verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
      UserUpdateRequest request = UserUpdateRequest.builder()
          .username("newusername")
          .build();

      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.updateUser(999L, request))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User")
          .hasMessageContaining("999");

      verify(userRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Delete User")
  class DeleteUserTests {

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

      userService.deleteUser(1L);

      verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.deleteUser(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User")
          .hasMessageContaining("999");

      verify(userRepository, never()).deleteById(any());
    }
  }
}
