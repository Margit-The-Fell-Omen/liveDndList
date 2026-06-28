package dev.ushki.livedndlist.dto.open5e.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Open5ePaginatedResponse<T> {

  private Integer count;
  private String next;
  private String previous;
  private List<T> results;
}
