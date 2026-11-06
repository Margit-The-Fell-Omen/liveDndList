package dev.ushki.livedndlist.dto.vtm.seed;

import dev.ushki.livedndlist.enums.DisciplineType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VtmDisciplineSeed {

  private String name;
  private String description;
  private DisciplineType type;
  private String sourcebook;
  private List<VtmPowerSeed> powers;
}