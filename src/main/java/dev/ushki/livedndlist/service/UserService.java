package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.request.UserUpdateRequest;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.enums.Role;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.UserMapper;
import dev.ushki.livedndlist.repository.UserRepository;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing user accounts. Handles user retrieval, profile updates, and account
 * deletion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  /**
   * Retrieves all user accounts with optional filtering.
   *
   * @param enabled optional filter by account enabled status
   * @param role    optional filter by role name
   * @return list of users matching the criteria
   */
  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers(Boolean enabled, String role) {
    List<User> users = userRepository.findAll();

    Stream<User> stream = users.stream();

    if (enabled != null) {
      stream = stream.filter(u -> u.isEnabled() == enabled);
    }
    if (role != null) {
      Role roleEnum = Role.valueOf(role);
      stream = stream.filter(u -> u.getRoles().contains(roleEnum));
    }

    return stream
        .map(userMapper::toResponse)
        .toList();
  }

  /**
   * Searches for users by username or email.
   *
   * @param query the search term (case-insensitive, partial match)
   * @return list of matching users
   */
  @Transactional(readOnly = true)
  public List<UserResponse> searchUsers(String query) {
    List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        query, query);
    return users.stream()
        .map(userMapper::toResponse)
        .toList();
  }

  /**
   * Retrieves a specific user by ID.
   *
   * @param id the user ID
   * @return the user's information
   */
  @Transactional(readOnly = true)
  public UserResponse getUserById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    return userMapper.toResponse(user);
  }

  /**
   * Retrieves a specific user by username.
   *
   * @param username the username to search for
   * @return the user's information
   */
  @Transactional(readOnly = true)
  public UserResponse getUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    return userMapper.toResponse(user);
  }

  /**
   * Updates a user's profile information.
   *
   * @param id      the user ID
   * @param request the update request with fields to change
   * @return the updated user information
   */
  public UserResponse updateUser(Long id, UserUpdateRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
      if (userRepository.existsByUsername(request.getUsername())) {
        throw new DuplicateResourceException("Username already exists");
      }
      user.setUsername(request.getUsername());
    }

    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
      if (userRepository.existsByEmail(request.getEmail())) {
        throw new DuplicateResourceException("Email already exists");
      }
      user.setEmail(request.getEmail());
    }

    User savedUser = userRepository.save(user);
    log.info("User updated: {}", savedUser.getUsername());

    return userMapper.toResponse(savedUser);
  }

  /**
   * Deletes a user account.
   *
   * @param id the user ID to delete
   */
  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User", "id", id);
    }
    userRepository.deleteById(id);
    log.info("User deleted: {}", id);
  }
}
