package dev.ushki.livedndlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreHitPointsResponse {

  private int charactersUpdated;
  private String message;
}
