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
@Schema(description = "VtM power reference data")
public class VtmPowerResponse {

  @Schema(description = "Unique identifier of the power", example = "1")
  private Long id;

  @Schema(description = "Power name", example = "Mesmerize")
  private String name;

  @Schema(description = "Description of the power")
  private String description;

  @Schema(description = "Power level (dots), 1-5", example = "2")
  private Integer level;

  @Schema(description = "Amalgam discipline, if any")
  private String amalgam;

  @Schema(description = "Prerequisite power")
  private String prerequisite;

  @Schema(description = "Id of the owning discipline", example = "4")
  private Long disciplineId;

  @Schema(description = "Name of the owning discipline", example = "Dominate")
  private String disciplineName;
}