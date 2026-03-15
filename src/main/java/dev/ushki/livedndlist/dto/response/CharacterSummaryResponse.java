package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.CharacterRace;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary response object for a character (list view)")
public class CharacterSummaryResponse {

  @Schema(description = "Unique identifier", example = "1")
  private Long id;

  @Schema(description = "Character name", example = "Aragorn")
  private String name;

  @Schema(description = "Character race", example = "HUMAN")
  private CharacterRace race;

  @Schema(description = "Display string for class and level", example = "Ranger 5")
  private String classDisplay;

  @Schema(description = "Total level", example = "5")
  private Integer totalLevel;

  @Schema(description = "Current HP", example = "32")
  private Integer currentHitPoints;

  @Schema(description = "Max HP", example = "45")
  private Integer maxHitPoints;

  @Schema(description = "Portrait URL", example = "https://example.com/aragorn.jpg")
  private String portraitUrl;

  @Schema(description = "Last update time", example = "2023-01-02T12:00:00")
  private LocalDateTime updatedAt;
}
