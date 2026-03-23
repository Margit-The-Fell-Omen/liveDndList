package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class Open5eSubraceDto {

  private String name;

  private String slug;

  private String desc;

  private List<Open5eAsiDto> asi;

  private String traits;

  @JsonProperty("asi_desc")
  private String asiDesc;

  @JsonProperty("document__slug")
  private String documentSlug;

  @JsonProperty("document__title")
  private String documentTitle;

  @JsonProperty("document__url")
  private String documentUrl;
}
