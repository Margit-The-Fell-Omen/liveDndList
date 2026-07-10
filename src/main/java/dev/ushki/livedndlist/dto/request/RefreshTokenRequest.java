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
@Schema(description = "Request object for refreshing access token")
public class RefreshTokenRequest {

  @NotBlank(message = "Refresh token is required")
  @Schema(description = "The refresh token issued at login")
  private String refreshToken;
}
