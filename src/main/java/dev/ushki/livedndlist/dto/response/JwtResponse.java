package dev.ushki.livedndlist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing JWT tokens")
public class JwtResponse {

  @Schema(description = "Access token for authentication",
      example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String accessToken;

  @Schema(description = "Refresh token to obtain new access token",
      example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String refreshToken;

  @Builder.Default
  @Schema(description = "Type of the token", example = "Bearer")
  private String tokenType = "Bearer";

  @Schema(description = "Access token expiration time in seconds", example = "3600")
  private Long expiresIn;

  @Schema(description = "User information associated with the token")
  private UserResponse user;
}
