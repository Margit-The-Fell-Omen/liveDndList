package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for user profile")
public class UserResponse {

  @Schema(description = "User ID", example = "42")
  private Long id;

  @Schema(description = "Username", example = "gandalf")
  private String username;

  @Schema(description = "Email address", example = "gandalf@example.com")
  private String email;

  @Schema(description = "User roles", example = "[USER, ADMIN]")
  private Set<Role> roles;

  @Schema(description = "Is account active?", example = "true")
  private boolean enabled;

  @Schema(description = "Account creation date", example = "2023-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Last profile update date", example = "2023-01-05T14:30:00")
  private LocalDateTime updatedAt;
}
