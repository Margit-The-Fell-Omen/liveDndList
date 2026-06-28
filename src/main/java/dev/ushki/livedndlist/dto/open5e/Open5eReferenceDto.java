package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Open5eReferenceDto {

  private String name;
  private String key;
}
