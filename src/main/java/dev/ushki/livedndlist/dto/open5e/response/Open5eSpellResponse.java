package dev.ushki.livedndlist.dto.open5e.response;

import dev.ushki.livedndlist.dto.open5e.Open5eSpellDto;
import java.util.List;
import lombok.Data;

@Data
public class Open5eSpellResponse {

  private Integer count;

  private String next;

  private String previous;

  private List<Open5eSpellDto> results;
}
