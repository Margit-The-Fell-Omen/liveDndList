package dev.ushki.livedndlist.dto.vtm.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mortal mask (human identity) of a vampire character")
public class MortalMaskResponse {

  @Schema(description = "Unique identifier of the mask", example = "1")
  private Long id;

  @Schema(description = "The mortal identity name", example = "John Doe")
  private String mortalName;

  @Schema(description = "Occupation used by the mask", example = "Night-shift taxi driver")
  private String occupation;

  @Schema(description = "Cover story for the mask")
  private String coverStory;

  @Schema(description = "Additional notes about the mask")
  private String description;
}