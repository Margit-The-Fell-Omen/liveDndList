package dev.ushki.livedndlist.dto.vtm.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update the dots of a character discipline")
public class VtmDotsUpdateRequest {

  @NotNull(message = "Dots are required")
  @Min(value = 1, message = "Dots must be at least 1")
  @Max(value = 5, message = "Dots must be at most 5")
  @Schema(description = "New number of dots (1-5)", example = "4")
  private Integer dots;
}