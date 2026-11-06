package dev.ushki.livedndlist.dto.vtm.response;

import dev.ushki.livedndlist.enums.DisciplineType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A discipline learned by a character, with dots and available powers")
public class DisciplineDotsResponse {

  @Schema(description = "Id of the learned discipline", example = "4")
  private Long disciplineId;

  @Schema(description = "Name of the learned discipline", example = "Dominate")
  private String disciplineName;

  @Schema(description = "Type of the discipline", example = "MENTAL")
  private DisciplineType disciplineType;

  @Schema(description = "Number of dots in the discipline (1-5)", example = "2")
  private Integer dots;

  @Schema(description = "Powers available at this dot level")
  private List<VtmPowerResponse> powers;
}