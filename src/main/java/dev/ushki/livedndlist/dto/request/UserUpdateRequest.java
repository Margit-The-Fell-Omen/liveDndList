package dev.ushki.livedndlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object to update user profile")
public class UserUpdateRequest {

  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Schema(description = "New username", example = "legolas")
  private String username;

  @Email(message = "Invalid email format")
  @Schema(description = "New email address", example = "legolas@mirkwood.com")
  private String email;
}
