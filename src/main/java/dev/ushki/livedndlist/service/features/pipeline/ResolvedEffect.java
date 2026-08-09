package dev.ushki.livedndlist.service.features.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ushki.livedndlist.enums.CharacterFeatureSource;
import dev.ushki.livedndlist.enums.FeatureEffectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResolvedEffect {

  FeatureEffectType type;
  JsonNode payload;
  long sourceCharacterFeatureId;
  long sourceFeatureId;
  CharacterFeatureSource source;
  JsonNode sourceContext;
}
