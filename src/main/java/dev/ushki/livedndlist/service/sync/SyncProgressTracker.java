package dev.ushki.livedndlist.service.sync;

import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncProgressTracker {

  private final AtomicBoolean syncInProgress = new AtomicBoolean(false);
  private final AtomicInteger processedCount = new AtomicInteger(0);
  private final AtomicInteger totalCount = new AtomicInteger(0);

  @Getter
  private volatile String currentOperation = "";

  public boolean tryStart() {
    return syncInProgress.compareAndSet(false, true);
  }

  public void finish() {
    syncInProgress.set(false);
    currentOperation = "";
    processedCount.set(0);
    totalCount.set(0);
  }

  public void setOperation(String operation) {
    this.currentOperation = operation;
  }

  public void setTotal(int total) {
    totalCount.set(total);
  }

  public void incrementProcessed() {
    processedCount.incrementAndGet();
  }

  public boolean isInProgress() {
    return syncInProgress.get();
  }

  public SyncStatusDto getStatus() {
    return SyncStatusDto.builder()
        .inProgress(syncInProgress.get())
        .currentOperation(currentOperation)
        .processedCount(processedCount.get())
        .totalCount(totalCount.get())
        .progressPercent(calculateProgress())
        .build();
  }

  private double calculateProgress() {
    int total = totalCount.get();
    if (total == 0) {
      return 0;
    }
    return Math.round((double) processedCount.get() / total * 10000.0) / 100.0;
  }
}
