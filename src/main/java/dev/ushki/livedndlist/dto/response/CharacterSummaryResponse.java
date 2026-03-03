package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.CharacterRace;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterSummaryResponse {

  private Long id;
  private String name;
  private CharacterRace race;
  private String classDisplay;
  private Integer totalLevel;
  private Integer currentHitPoints;
  private Integer maxHitPoints;
  private String portraitUrl;
  private LocalDateTime updatedAt;
}
