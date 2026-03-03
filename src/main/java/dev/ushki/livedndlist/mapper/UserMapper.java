package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

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
