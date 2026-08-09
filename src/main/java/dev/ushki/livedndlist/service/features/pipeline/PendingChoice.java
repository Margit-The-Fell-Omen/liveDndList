package dev.ushki.livedndlist.service.features.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.enums.ChoiceOptionsSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PendingChoice {

  long characterFeatureId;
  String choiceKey;
  String name;
  String description;
  int chooseCount;
  ChoiceOptionsSource optionsSource;
  JsonNode optionsFilter;
  JsonNode currentSelection;               // null if never answered
}
