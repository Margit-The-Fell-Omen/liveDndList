package dev.ushki.livedndlist.service.sync;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SyncMetrics {

  private final AtomicInteger totalRequests = new AtomicInteger(0);
  private final AtomicInteger successfulRequests = new AtomicInteger(0);
  private final AtomicInteger failedRequests = new AtomicInteger(0);
  private final AtomicLong totalResponseTime = new AtomicLong(0);
  private final AtomicInteger concurrentOperations = new AtomicInteger(0);

  public void recordRequest(long responseTimeMs, boolean success) {
    totalRequests.incrementAndGet();
    totalResponseTime.addAndGet(responseTimeMs);

    if (success) {
      successfulRequests.incrementAndGet();
    } else {
      failedRequests.incrementAndGet();
    }
  }

  public void startOperation() {
    int current = concurrentOperations.incrementAndGet();
    log.debug("Concurrent operations: {}", current);
  }

  public void endOperation() {
    concurrentOperations.decrementAndGet();
  }

  public MetricsSnapshot getSnapshot() {
    int total = totalRequests.get();
    int successful = successfulRequests.get();
    int failed = failedRequests.get();
    long totalTime = totalResponseTime.get();

    return MetricsSnapshot.builder()
        .totalRequests(total)
        .successfulRequests(successful)
        .failedRequests(failed)
        .averageResponseTime(total > 0 ? (double) totalTime / total : 0)
        .concurrentOperations(concurrentOperations.get())
        .build();
  }

  public void reset() {
    totalRequests.set(0);
    successfulRequests.set(0);
    failedRequests.set(0);
    totalResponseTime.set(0);
    concurrentOperations.set(0);
  }

  @lombok.Data
  @lombok.Builder
  public static class MetricsSnapshot {

    private int totalRequests;
    private int successfulRequests;
    private int failedRequests;
    private double averageResponseTime;
    private int concurrentOperations;
  }
}
