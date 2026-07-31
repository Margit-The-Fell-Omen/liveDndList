package dev.ushki.livedndlist.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.ushki.livedndlist.dto.open5e.Open5eDocumentDto;
import dev.ushki.livedndlist.enums.DndFeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Feat definition response")
public class DndFeatResponse {

  @Schema(description = "Stable feat key", example = "a5e-ag_ace-driver")
  private String key;

  @Schema(description = "Display name", example = "Ace Driver")
  private String name;

  @Schema(description = "Full description text")
  private String desc;

  @Schema(description = "Feat type", example = "GENERAL")
  private DndFeatType type;

  @Schema(description = "Whether the feat has a prerequisite")
  private boolean hasPrerequisite;

  @Schema(description = "Prerequisite description", example = "Proficiency with a type of vehicle")
  private String prerequisite;

  @Schema(description = "List of benefits granted by this feat")
  private List<String> benefits;

  @Schema(description = "Source document metadata")
  private Open5eDocumentDto document;

}
