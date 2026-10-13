package dev.ushki.livedndlist.service.features;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.enums.FeatureEffectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureEffectRef {

  private FeatureEffectType effectType;
  private JsonNode payload;
  private String choiceKey;
}