package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Open5eDocumentDto {

  private String name;
  private String key;
  private String type;

  @JsonProperty("display_name")
  private String displayName;

  private Open5eReferenceDto publisher;

  @JsonProperty("gamesystem")
  private Open5eReferenceDto gameSystem;

  private String permalink;
}
