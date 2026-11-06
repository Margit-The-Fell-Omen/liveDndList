package dev.ushki.livedndlist.dto.vtm.request;

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
@Schema(description = "Request to create or update a mortal mask")
public class MortalMaskRequest {

  @NotBlank(message = "Mortal name is required")
  @Schema(description = "The mortal identity name", example = "John Doe")
  private String mortalName;

  @Schema(description = "Occupation used by the mask", example = "Night-shift cab driver")
  private String occupation;

  @Schema(description = "Cover story for the mask")
  private String coverStory;

  @Schema(description = "Additional notes about the mask")
  private String description;
}