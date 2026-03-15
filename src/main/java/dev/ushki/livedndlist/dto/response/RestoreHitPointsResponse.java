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
@Schema(description = "Response object for restoring hit points operation")
public class RestoreHitPointsResponse {

  @Schema(description = "Number of characters affected", example = "1")
  private int charactersUpdated;

  @Schema(description = "Operation result message", example = "Hit points restored successfully")
  private String message;
}
