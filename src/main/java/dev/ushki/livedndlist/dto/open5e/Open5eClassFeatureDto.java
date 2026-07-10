package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ushki.livedndlist.enums.DndClassFeatureType;
import java.util.List;
import lombok.Data;

@Data
public class Open5eClassFeatureDto {

  private String key;

  private String name;

  private String desc;

  @JsonProperty("feature_type")
  private DndClassFeatureType featureType;

  @JsonProperty("gained_at")
  private List<Open5eGainedAtDto> gainedAt;

  @JsonProperty("data_for_class_table")
  private List<Open5eClassTableDataDto> dataForClassTable;
}
