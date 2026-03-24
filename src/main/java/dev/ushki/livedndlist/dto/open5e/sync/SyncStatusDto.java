package dev.ushki.livedndlist.dto.open5e.sync;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatusDto {

  private boolean inProgress;
  private String currentOperation;
  private int processedCount;
  private int totalCount;
  private double progressPercent;
}
