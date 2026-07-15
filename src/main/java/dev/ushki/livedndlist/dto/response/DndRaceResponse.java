package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.dto.open5e.Open5eDocumentDto;
import dev.ushki.livedndlist.dto.open5e.Open5eTraitDto;
import java.util.List;
import lombok.Data;

@Data
public class DndRaceResponse {

  private String name;
  private String key;
  private String desc;
  private boolean isSubspecies;
  private String subraceOf;
  private List<String> subraceOfThis;
  private List<Open5eTraitDto> traits;
  private Open5eDocumentDto document;
}
