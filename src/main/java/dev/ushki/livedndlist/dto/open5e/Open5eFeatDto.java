package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Schema(description = "Feat definition")
public class Open5eFeatDto {

  @Schema(description = "Stable feat key", example = "a5e-ag_ace-driver")
  private String key;

  @Schema(description = "Display name", example = "Ace Driver")
  private String name;

  @Schema(description = "Full description text")
  private String desc;

  @Schema(description = "Feat type", example = "GENERAL")
  private String type;

  @Schema(description = "Whether the feat has a prerequisite")
  private Boolean hasPrerequisite;

  @Schema(description = "Prerequisite description", example = "Proficiency with a type of vehicle")
  private String prerequisite;

  @Schema(description = "List of benefits granted by this feat")
  private List<FeatBenefit> benefits;

  @Schema(description = "Source document metadata")
  private Open5eDocumentDto document;

  @Schema(description = "Cross references to related content")
  private Open5eCrossReferencesDto crossreferences;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Schema(description = "A single benefit granted by a feat")
  public static class FeatBenefit {

    @Schema(description = "Benefit description")
    private String desc;
  }

}
