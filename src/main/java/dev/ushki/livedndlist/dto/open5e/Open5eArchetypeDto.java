package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Open5eArchetypeDto {

  private String name;

  private String slug;

  private String desc;

  @JsonProperty("document__slug")
  private String documentSlug;

  @JsonProperty("document__title")
  private String documentTitle;

  @JsonProperty("document__license_url")
  private String documentLicenseUrl;

  @JsonProperty("document__url")
  private String documentUrl;
}
