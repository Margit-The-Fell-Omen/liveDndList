package dev.ushki.live_dnd_list.service;

import dev.ushki.live_dnd_list.dto.request.UserUpdateRequest;
import dev.ushki.live_dnd_list.dto.response.UserResponse;
import dev.ushki.live_dnd_list.entity.User;
import dev.ushki.live_dnd_list.enums.Role;
import dev.ushki.live_dnd_list.exceptions.DuplicateResourceException;
import dev.ushki.live_dnd_list.exceptions.ResourceNotFoundException;
import dev.ushki.live_dnd_list.mapper.UserMapper;
import dev.ushki.live_dnd_list.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("encoded_password")
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        verify(userRepository).findAll();
    }

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
                .roles(Set.of(Role.ROLE_USER))
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
    @DisplayName("Should throw exception when updating with existing email")
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("existing@test.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow updating with same username")
    void shouldAllowUpdatingWithSameUsername() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("testuser")  // Same as current
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        UserResponse result = userService.updateUser(1L, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("999");

        verify(userRepository, never()).deleteById(any());
    }
}
