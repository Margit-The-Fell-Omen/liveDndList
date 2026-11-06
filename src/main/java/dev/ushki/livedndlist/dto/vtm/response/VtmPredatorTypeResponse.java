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
@Schema(description = "VtM predator type reference data")
public class VtmPredatorTypeResponse {

  @Schema(description = "Unique identifier of the predator type", example = "1")
  private Long id;

  @Schema(description = "Predator type name", example = "Alleycat")
  private String name;

  @Schema(description = "Description of the predator type")
  private String description;

  @Schema(description = "Specialty granted by the predator type", example = "Brawl")
  private String grantedSpecialty;

  @Schema(description = "Source book of the predator type")
  private String sourcebook;
}