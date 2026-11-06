package dev.ushki.livedndlist.dto.vtm.seed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VtmPowerSeed {

  private String name;
  private String description;
  private Integer level;
  private String amalgam;
  private String prerequisite;
}