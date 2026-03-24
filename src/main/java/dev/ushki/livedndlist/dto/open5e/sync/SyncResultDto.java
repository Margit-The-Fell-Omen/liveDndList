package dev.ushki.livedndlist.dto.open5e.sync;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultDto {

  private boolean success;
  private String message;
  private LocalDateTime syncedAt;
  private SyncStatistics statistics;
  private List<String> errors;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SyncStatistics {

    private int totalFetched;
    private int created;
    private int updated;
    private int failed;
    private long durationMs;
  }
}
