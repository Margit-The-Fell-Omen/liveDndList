package dev.ushki.livedndlist.dto.share;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to import a character from a share link")
public class ImportCharacterRequest {

  @NotBlank(message = "Share URL is required")
  @Size(max = 120, message = "Share URL must not exceed 120 characters")
  @Schema(description = "Shareable character URL",
      example = "https://live-dnd-list.duckdns.org/s/abc123")
  private String url;
}
