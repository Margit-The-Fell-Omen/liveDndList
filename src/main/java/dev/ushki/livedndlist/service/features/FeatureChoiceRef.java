package dev.ushki.livedndlist.service.features;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.enums.ChoiceOptionsSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureChoiceRef {

  private String choiceKey;
  private String name;
  private String description;
  private Integer chooseCount;
  private ChoiceOptionsSource optionsSource;
  private JsonNode optionsFilter;
}