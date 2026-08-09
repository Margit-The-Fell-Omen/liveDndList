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
@Schema(description = "Request to set the subclass for a class the character has")
public class SetSubclassRequest {

  @NotBlank
  @Schema(description = "Class key", example = "srd-2024_fighter")
  private String classKey;

  @NotBlank
  @Schema(description = "Subclass key", example = "srd-2024_champion")
  private String subclassKey;
}
