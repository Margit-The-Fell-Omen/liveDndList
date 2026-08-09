package dev.ushki.livedndlist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User-authored narrative feature")
public class CustomFeatureResponse {

  @Schema(description = "Custom feature id", example = "42")
  private Long id;

  @Schema(description = "Name", example = "Magic Ring")
  private String name;

  @Schema(description = "Description (markdown)")
  private String description;
}
