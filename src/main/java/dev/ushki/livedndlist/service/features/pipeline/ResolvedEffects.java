package dev.ushki.livedndlist.service.features.pipeline;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResolvedEffects {

  List<ResolvedEffect> effects;
  List<PendingChoice> pendingChoices;

}
