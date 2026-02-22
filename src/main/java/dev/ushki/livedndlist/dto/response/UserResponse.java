package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.Role;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing user account information. Used for user profile display and
 * authentication responses.
 *
 * <p>Note: This response intentionally excludes sensitive information
 * such as passwords and security tokens.
 *
 * <p>User roles determine access permissions:
 * <ul>
 *   <li>{@code ROLE_USER} - Standard user access</li>
 *   <li>{@code ROLE_ADMIN} - Administrative privileges</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;
  private String username;
  private String email;
  private Set<Role> roles;
  private boolean enabled;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
