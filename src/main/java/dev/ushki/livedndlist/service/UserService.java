package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.request.UserUpdateRequest;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.UserMapper;
import dev.ushki.livedndlist.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing user accounts. Handles user retrieval, profile updates, and account
 * deletion.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>User account retrieval (all, by ID, by username)</li>
 *   <li>User profile updates with duplicate validation</li>
 *   <li>User account deletion (cascades to characters)</li>
 * </ul>
 *
 * <p>Security notes:
 * <ul>
 *   <li>Passwords cannot be changed through this service (use dedicated password reset)</li>
 *   <li>Username and email must remain unique</li>
 *   <li>Deleting a user cascades to delete all their characters</li>
 *   <li>Sensitive data (password) is never exposed in responses</li>
 * </ul>
 *
 * <p>All write operations are transactional and logged for audit purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  /**
   * Retrieves all user accounts. Typically restricted to administrators.
   *
   * <p>Note: Passwords are excluded from the response for security.
   *
   * @return list of all users (without password information)
   */
  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream()
        .map(userMapper::toResponse)
        .toList();
  }

  /**
   * Retrieves a specific user by ID.
   *
   * @param id the user ID
   * @return the user's information (excluding password)
   * @throws ResourceNotFoundException if the user is not found
   */
  @Transactional(readOnly = true)
  public UserResponse getUserById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    return userMapper.toResponse(user);
  }

  /**
   * Retrieves a specific user by username. Used for profile lookups and the "/me" endpoint.
   *
   * @param username the username to search for
   * @return the user's information (excluding password)
   * @throws ResourceNotFoundException if the user is not found
   */
  @Transactional(readOnly = true)
  public UserResponse getUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    return userMapper.toResponse(user);
  }

  /**
   * Updates a user's profile information. Supports partial updates - only provided fields are
   * changed.
   *
   * <p>Validation rules:
   * <ul>
   *   <li>New username must be unique (if changing)</li>
   *   <li>New email must be unique (if changing)</li>
   *   <li>Password cannot be changed through this method</li>
   * </ul>
   *
   * <p>If username or email is unchanged, no validation is performed
   * (prevents false duplicate errors when user submits current values).
   *
   * @param id      the user ID
   * @param request the update request with fields to change
   * @return the updated user information (excluding password)
   * @throws ResourceNotFoundException  if the user is not found
   * @throws DuplicateResourceException if new username or email already exists
   */
  public UserResponse updateUser(Long id, UserUpdateRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    // Only validate username uniqueness if it's being changed
    if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
      if (userRepository.existsByUsername(request.getUsername())) {
        throw new DuplicateResourceException("Username already exists");
      }
      user.setUsername(request.getUsername());
    }

    // Only validate email uniqueness if it's being changed
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
   * <p>Warning: This operation cascades to delete all characters owned by the user.
   * This is irreversible and should typically be restricted to administrators or the account owner
   * with confirmation.
   *
   * @param id the user ID to delete
   * @throws ResourceNotFoundException if the user is not found
   */
  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User", "id", id);
    }
    userRepository.deleteById(id);
    log.info("User deleted: {}", id);
  }
}
