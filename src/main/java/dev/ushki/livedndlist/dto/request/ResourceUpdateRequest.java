package dev.ushki.livedndlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Adjust a character resource's current uses")
public class ResourceUpdateRequest {

  @Schema(description = "Absolute value to set current uses to", example = "3")
  private Integer current;

  @Schema(description = "Relative change (positive or negative)", example = "-1")
  private Integer delta;
}
