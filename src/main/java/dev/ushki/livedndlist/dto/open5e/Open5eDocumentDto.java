package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Open5eDocumentDto {

  private String name;
  private String key;
  private String type;

  @JsonProperty("display_name")
  private String displayName;

  private Open5ePublisherDto publisher;

  @JsonProperty("gamesystem")
  private Open5eGameSystemDto gameSystem;

  private String permalink;
}
