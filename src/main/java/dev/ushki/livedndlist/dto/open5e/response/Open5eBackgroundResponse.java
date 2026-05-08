package dev.ushki.livedndlist.dto.open5e.response;

import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundDto;
import java.util.List;
import lombok.Data;

@Data
public class Open5eBackgroundResponse {

  private Integer count;

  private String next;

  private String previous;

  private List<Open5eBackgroundDto> results;
}
