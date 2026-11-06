package dev.ushki.livedndlist.dto.vtm.response;

import dev.ushki.livedndlist.enums.ChronicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight VtM chronicle summary for list views")
public class ChronicleSummaryResponse {

  @Schema(description = "Unique identifier of the chronicle", example = "1")
  private Long id;

  @Schema(description = "Chronicle title", example = "Night over Chicago")
  private String title;

  @Schema(description = "Start date of the chronicle")
  private LocalDate startDate;

  @Schema(description = "Status of the chronicle", example = "ACTIVE")
  private ChronicleStatus status;

  @Schema(description = "Number of characters in the chronicle", example = "4")
  private long characterCount;
}