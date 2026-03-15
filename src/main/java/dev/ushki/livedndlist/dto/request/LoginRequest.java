package dev.ushki.livedndlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user login")
public class LoginRequest {

  @NotBlank(message = "Username is required")
  @Schema(description = "Username", example = "frodo")
  private String username;

  @NotBlank(message = "Password is required")
  @Schema(description = "User password", example = "TheRing123")
  private String password;
}
