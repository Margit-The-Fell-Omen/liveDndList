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
@Schema(description = "Represents D&D currency breakdown")
public class DndCurrencyResponse {

  @Schema(description = "Copper pieces", example = "50")
  private Integer copper;

  @Schema(description = "Silver pieces", example = "10")
  private Integer silver;

  @Schema(description = "Electrum pieces", example = "0")
  private Integer electrum;

  @Schema(description = "Gold pieces", example = "100")
  private Integer gold;

  @Schema(description = "Platinum pieces", example = "5")
  private Integer platinum;
}
