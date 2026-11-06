package dev.ushki.livedndlist.dto.vtm.response;

import dev.ushki.livedndlist.enums.ChronicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full VtM chronicle response")
public class ChronicleResponse {

  @Schema(description = "Unique identifier of the chronicle", example = "1")
  private Long id;

  @Schema(description = "Username of the player who runs the chronicle", example = "alice")
  private String playerUsername;

  @Schema(description = "Chronicle title", example = "Night over Chicago")
  private String title;

  @Schema(description = "Short description of the chronicle")
  private String description;

  @Schema(description = "Setting / campaign premise")
  private String setting;

  @Schema(description = "Start date of the chronicle")
  private LocalDate startDate;

  @Schema(description = "Status of the chronicle", example = "ACTIVE")
  private ChronicleStatus status;

  @Schema(description = "Number of characters in the chronicle", example = "4")
  private long characterCount;

  @Schema(description = "Creation timestamp", example = "2026-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Last update timestamp", example = "2026-01-02T12:00:00")
  private LocalDateTime updatedAt;
}