package dev.ushki.livedndlist.service.features;

import dev.ushki.livedndlist.enums.FeatureSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRef {

  private Long id;
  private String key;
  private String name;
  private String description;
  private FeatureSourceType sourceType;
  private String sourceKey;
  private Integer gainedAtLevel;
  private String prerequisite;
  private Integer displayOrder;
}