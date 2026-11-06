package dev.ushki.livedndlist.dto.vtm.seed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VtmPredatorTypeSeed {

  private String name;
  private String description;
  private String grantedSpecialty;
  private String sourcebook;
}