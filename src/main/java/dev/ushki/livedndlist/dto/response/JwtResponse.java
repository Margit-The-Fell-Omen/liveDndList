package dev.ushki.livedndlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing JWT authentication tokens. Returned upon successful login or token
 * refresh.
 *
 * <p>Usage example:
 * <pre>{@code
 * Authorization: Bearer <accessToken>
 * }</pre>
 *
 * <p>The access token should be included in the Authorization header
 * for all authenticated API requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

  private String accessToken;
  private String refreshToken;

  @Builder.Default
  private String tokenType = "Bearer";

  private Long expiresIn;
  private UserResponse user;
}
