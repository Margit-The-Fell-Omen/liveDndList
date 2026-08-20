package dev.ushki.livedndlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User's answer to a feature choice")
public class SubmitChoiceRequest {

  @NotNull
  @Schema(
      description = "Selected values as a JSON array",
      example = "[\"ATHLETICS\", \"PERCEPTION\"]"
  )
  private List<Object> selectedValues;
}

