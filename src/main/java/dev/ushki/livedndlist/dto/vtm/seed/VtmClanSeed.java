package dev.ushki.livedndlist.dto.vtm.seed;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Seed payload for a single clan. {@link #disciplines} lists the names of the
 * disciplines the clan grants (resolved against the seeded discipline catalog).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VtmClanSeed {

  private String name;
  private String description;
  private String curse;
  private String compulsion;
  private String sourcebook;
  private List<String> disciplines;
}