package dev.ushki.livedndlist.dto.share;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ushki.livedndlist.dto.dnd.response.CharacterResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmCharacterResponse;
import dev.ushki.livedndlist.enums.CharacterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Public, read-only character view resolved from a share link")
public class PublicCharacterResponse {

  @Schema(description = "Character ruleset", example = "DND14")
  private CharacterType characterType;

  @Schema(description = "D&D character payload, present when characterType is DND14 or DND24")
  private CharacterResponse dndCharacter;

  @Schema(description = "VTM character payload, present when characterType is VTM5")
  private VtmCharacterResponse vtmCharacter;
}
