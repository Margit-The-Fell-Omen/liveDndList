package dev.ushki.livedndlist.dto.open5e.response;

import dev.ushki.livedndlist.dto.open5e.Open5eClassDto;
import lombok.Data;

import java.util.List;

@Data
public class Open5eClassResponse {

  private Integer count;

  private String next;

  private String previous;

  private List<Open5eClassDto> results;
}
