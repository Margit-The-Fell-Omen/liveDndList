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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

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

  @Transactional(readOnly = true)
  public List<UserResponse> searchUsers(String query) {
    List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        query, query);
    return users.stream()
        .map(userMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public UserResponse getUserById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    return userMapper.toResponse(user);
  }

  @Transactional(readOnly = true)
  public UserResponse getUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    return userMapper.toResponse(user);
  }

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

  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User", "id", id);
    }
    userRepository.deleteById(id);
    log.info("User deleted: {}", id);
  }
}
