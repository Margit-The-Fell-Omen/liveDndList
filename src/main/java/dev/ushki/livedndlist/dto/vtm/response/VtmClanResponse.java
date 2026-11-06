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
@Schema(description = "VtM clan reference data")
public class VtmClanResponse {

  @Schema(description = "Unique identifier of the clan", example = "1")
  private Long id;

  @Schema(description = "Clan name", example = "Brujah")
  private String name;

  @Schema(description = "Description of the clan")
  private String description;

  @Schema(description = "Clan curse")
  private String curse;

  @Schema(description = "Clan compulsion")
  private String compulsion;

  @Schema(description = "Source book of the clan")
  private String sourcebook;
}