package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class Open5eRaceDto {

  private String name;

  private String key;

  @JsonProperty("desc")
  private String desc;

  private Open5eDocumentDto document;

  private boolean isSubspecies;

  @JsonProperty("subspecies_of")
  private String subspeciesOf;

  private List<Open5eTraitDto> traits;

  private Open5eCrossReferencesDto crossreferences;
}
