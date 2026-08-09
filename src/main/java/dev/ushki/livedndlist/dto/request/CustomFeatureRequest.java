package dev.ushki.livedndlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Custom (narrative-only) character feature")
public class CustomFeatureRequest {

  @NotBlank
  @Size(max = 255)
  @Schema(description = "Feature name", example = "Magic Ring")
  private String name;

  @Schema(description = "Feature description (markdown)")
  private String description;
}
