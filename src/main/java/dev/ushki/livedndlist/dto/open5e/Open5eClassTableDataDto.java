package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Open5eClassTableDataDto {

  private Integer level;

  @JsonProperty("column_value")
  private String columnValue;
}
