package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.CharacterRace;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing a summary of character information. Used for listing characters without
 * loading full character details.
 *
 * <p>This lightweight representation is ideal for:
 * <ul>
 *   <li>Character selection screens</li>
 *   <li>Character list views</li>
 *   <li>Dashboard displays</li>
 * </ul>
 *
 * <p>For complete character details, use {@link CharacterResponse}.
 */
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
