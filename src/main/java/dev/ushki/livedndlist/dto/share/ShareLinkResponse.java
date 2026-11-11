package dev.ushki.livedndlist.dto.share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generated share link for a character")
public class ShareLinkResponse {

  @Schema(description = "Shareable URL", example = "https://live-dnd-list.duckdns.org/s/abc123")
  private String url;
}
