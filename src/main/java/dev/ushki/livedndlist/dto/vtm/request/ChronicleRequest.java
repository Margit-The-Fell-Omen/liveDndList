package dev.ushki.livedndlist.dto.vtm.request;

import dev.ushki.livedndlist.enums.ChronicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create or update a VtM chronicle")
public class ChronicleRequest {

  @NotBlank(message = "Chronicle title is required")
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
}