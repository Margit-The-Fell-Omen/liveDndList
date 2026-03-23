package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class Open5eRaceDto {

  private String name;

  private String slug;

  private String desc;

  @JsonProperty("asi_desc")
  private String asiDesc;

  private List<Open5eAsiDto> asi;

  private String age;

  private String alignment;

  private String size;

  @JsonProperty("size_raw")
  private String sizeRaw;

  private Open5eSpeedDto speed;

  @JsonProperty("speed_desc")
  private String speedDesc;

  private String languages;

  private String vision;

  private String traits;

  private List<Open5eSubraceDto> subraces;

  @JsonProperty("document__slug")
  private String documentSlug;

  @JsonProperty("document__title")
  private String documentTitle;

  @JsonProperty("document__license_url")
  private String documentLicenseUrl;

  @JsonProperty("document__url")
  private String documentUrl;
}
