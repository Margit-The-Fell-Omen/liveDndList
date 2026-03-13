package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final CacheManager cacheManager;

  private static final String USER_RESOURCE = "User:";
  private static final String USERS_NAMESPACE = "Users";

  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers(Boolean enabled, String role) {
    CompositeKey key = new CompositeKey("all", enabled, role);

    return cacheManager.get(USERS_NAMESPACE, key, () -> {
      List<User> users = userRepository.findAll();

      Stream<User> stream = users.stream();

      if (enabled != null) {
        stream = stream.filter(u -> u.isEnabled() == enabled);
      }
      if (role != null) {
        Role roleEnum = Role.valueOf(role);
        stream = stream.filter(u -> u.getRole() == roleEnum);
      }

      return stream
          .map(userMapper::toResponse)
          .toList();
    });
  }

  @Transactional(readOnly = true)
  public List<UserResponse> searchUsers(String query, Pageable pageable) {
    CompositeKey key = new CompositeKey("search", query);

    return cacheManager.get(USERS_NAMESPACE, key, () -> {
      List<User> users = userRepository
          .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable);
      return users.stream()
          .map(userMapper::toResponse)
          .toList();
    });
  }

  @Transactional(readOnly = true)
  public UserResponse getUserById(Long id) {
    CompositeKey key = new CompositeKey("byId", id);

    return cacheManager.get(USERS_NAMESPACE, key, () -> {
      User user = userRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
      return userMapper.toResponse(user);
    });
  }

  @Transactional(readOnly = true)
  public UserResponse getUserByUsername(String username) {
    CompositeKey key = new CompositeKey("byUsername", username);

    return cacheManager.get(USERS_NAMESPACE, key, () -> {
      User user = userRepository.findByUsername(username)
          .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
      return userMapper.toResponse(user);
    });
  }

  @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
  public UserResponse updateUser(Long id, UserUpdateRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    String oldUsername = user.getUsername();

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

    cacheManager.invalidateByPrefix(USERS_NAMESPACE);
    cacheManager.invalidateByPrefix(USER_RESOURCE + oldUsername);
    if (!oldUsername.equals(savedUser.getUsername())) {
      cacheManager.invalidateByPrefix(USER_RESOURCE + savedUser.getUsername());
    }

    return userMapper.toResponse(savedUser);
  }

  public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    String username = user.getUsername();

    userRepository.deleteById(id);

    cacheManager.invalidateByPrefix(USERS_NAMESPACE);
    cacheManager.invalidateByPrefix(USER_RESOURCE + username);
  }
}
