package dev.ushki.livedndlist.dto.vtm.response;

import dev.ushki.livedndlist.enums.DisciplineType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "VtM discipline reference data")
public class VtmDisciplineResponse {

  @Schema(description = "Unique identifier of the discipline", example = "1")
  private Long id;

  @Schema(description = "Discipline name", example = "Auspex")
  private String name;

  @Schema(description = "Description of the discipline")
  private String description;

  @Schema(description = "Discipline type", example = "MENTAL")
  private DisciplineType disciplineType;

  @Schema(description = "Source book of the discipline")
  private String sourcebook;
}