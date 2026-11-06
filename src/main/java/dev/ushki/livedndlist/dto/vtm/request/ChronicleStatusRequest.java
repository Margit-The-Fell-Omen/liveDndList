package dev.ushki.livedndlist.dto.vtm.request;

import dev.ushki.livedndlist.enums.ChronicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to change a chronicle status")
public class ChronicleStatusRequest {

  @NotNull(message = "Status is required")
  @Schema(description = "New chronicle status", example = "ACTIVE")
  private ChronicleStatus status;
}