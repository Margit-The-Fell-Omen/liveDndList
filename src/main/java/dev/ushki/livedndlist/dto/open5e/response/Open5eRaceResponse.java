package dev.ushki.livedndlist.dto.open5e.response;

import dev.ushki.livedndlist.dto.open5e.Open5eRaceDto;
import java.util.List;
import lombok.Data;

@Data
public class Open5eRaceResponse {

  private Integer count;

  private String next;

  private String previous;

  private List<Open5eRaceDto> results;
}
