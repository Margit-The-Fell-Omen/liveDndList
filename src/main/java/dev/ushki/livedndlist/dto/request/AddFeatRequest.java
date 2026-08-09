package dev.ushki.livedndlist.dto.request;

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
@Schema(description = "Request to grant a feat to a character")
public class AddFeatRequest {

  @NotBlank
  @Schema(description = "Feat key", example = "srd_lucky")
  private String featKey;

  @Schema(description = "Class key of the ASI slot used to grant this feat", example = "srd-2024_fighter")
  private String asiSlotClassKey;
}
