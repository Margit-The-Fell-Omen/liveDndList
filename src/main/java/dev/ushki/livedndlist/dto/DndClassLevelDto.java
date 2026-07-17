package dev.ushki.livedndlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single class assignment with its level for a character")
public class DndClassLevelDto {

  @NotBlank(message = "Class key is required")
  @Schema(description = "Key of the D&D class", example = "srd-2024_fighter")
  private String classKey;

  @NotNull(message = "Level is required")
  @Min(value = 1, message = "Level must be at least 1")
  @Max(value = 20, message = "Level must not exceed 20")
  @Schema(description = "Level in this class", example = "5")
  private Integer level;
}
