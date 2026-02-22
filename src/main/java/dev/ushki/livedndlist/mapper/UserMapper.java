package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between User entities and DTOs. Handles safe conversion of user data,
 * excluding sensitive information.
 *
 * <p>Security note: This mapper intentionally excludes:
 * <ul>
 *   <li>Password (stored as BCrypt hash)</li>
 *   <li>Characters list (to avoid circular serialization and performance issues)</li>
 * </ul>
 */
@Component
public class UserMapper {

  /**
   * Converts a User entity to a UserResponse DTO. Excludes sensitive information such as password.
   *
   * @param user the user entity to convert
   * @return the user response DTO with public information only
   */
  public UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .roles(user.getRoles())
        .enabled(user.isEnabled())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
