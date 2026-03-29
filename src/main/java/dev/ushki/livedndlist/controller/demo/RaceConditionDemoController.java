package dev.ushki.livedndlist.controller.demo;

import dev.ushki.livedndlist.demo.SafeCounterAtomic;
import dev.ushki.livedndlist.demo.SafeCounterSynchronized;
import dev.ushki.livedndlist.demo.UnsafeCounter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/race-condition")
@Slf4j
@Tag(name = "Race Condition Demo", description = "Demonstration of race condition and thread-safe solutions")
@SecurityRequirement(name = "bearerAuth")
public class RaceConditionDemoController {

  @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
  @GetMapping("/unsafe")
  @Operation(summary = "Demonstrate race condition with unsafe counter")
  public RaceConditionResult testUnsafeCounter(
      @RequestParam(defaultValue = "100") int threads,
      @RequestParam(defaultValue = "1000") int iterations) throws InterruptedException {

    UnsafeCounter counter = new UnsafeCounter();
    long startTime = System.currentTimeMillis();

    ExecutorService executor = Executors.newFixedThreadPool(threads);

    for (int i = 0; i < threads; i++) {
      executor.submit(() -> {
        for (int j = 0; j < iterations; j++) {
          counter.increment();
        }
      });
    }

    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS);

    long duration = System.currentTimeMillis() - startTime;
    int expectedCount = threads * iterations;
    int actualCount = counter.getCount();

    return new RaceConditionResult(
        "Unsafe Counter",
        threads,
        iterations,
        expectedCount,
        actualCount,
        expectedCount - actualCount,
        duration
    );
  }

  @GetMapping("/safe-synchronized")
  @Operation(summary = "Demonstrate thread-safe counter with synchronized")
  public RaceConditionResult testSafeSynchronized(
      @RequestParam(defaultValue = "100") int threads,
      @RequestParam(defaultValue = "1000") int iterations) throws InterruptedException {

    SafeCounterSynchronized counter = new SafeCounterSynchronized();
    long startTime = System.currentTimeMillis();

    ExecutorService executor = Executors.newFixedThreadPool(threads);

    for (int i = 0; i < threads; i++) {
      executor.submit(() -> {
        for (int j = 0; j < iterations; j++) {
          counter.increment();
        }
      });
    }

    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS);

    long duration = System.currentTimeMillis() - startTime;
    int expectedCount = threads * iterations;
    int actualCount = counter.getCount();

    return new RaceConditionResult(
        "Synchronized Counter",
        threads,
        iterations,
        expectedCount,
        actualCount,
        expectedCount - actualCount,
        duration
    );
  }

  @GetMapping("/safe-atomic")
  @Operation(summary = "Demonstrate thread-safe counter with Atomic")
  public RaceConditionResult testSafeAtomic(
      @RequestParam(defaultValue = "100") int threads,
      @RequestParam(defaultValue = "1000") int iterations) throws InterruptedException {

    SafeCounterAtomic counter = new SafeCounterAtomic();
    long startTime = System.currentTimeMillis();

    ExecutorService executor = Executors.newFixedThreadPool(threads);

    for (int i = 0; i < threads; i++) {
      executor.submit(() -> {
        for (int j = 0; j < iterations; j++) {
          counter.increment();
        }
      });
    }

    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS);

    long duration = System.currentTimeMillis() - startTime;
    int expectedCount = threads * iterations;
    int actualCount = counter.getCount();

    return new RaceConditionResult(
        "Atomic Counter",
        threads,
        iterations,
        expectedCount,
        actualCount,
        expectedCount - actualCount,
        duration
    );
  }

  @Data
  public static class RaceConditionResult {

    private final String counterType;
    private final int threads;
    private final int iterationsPerThread;
    private final int expectedCount;
    private final int actualCount;
    private final int lostUpdates;
    private final long executionTimeMs;
    private final boolean isCorrect;

    public RaceConditionResult(String counterType, int threads, int iterationsPerThread,
        int expectedCount, int actualCount, int lostUpdates, long executionTimeMs) {
      this.counterType = counterType;
      this.threads = threads;
      this.iterationsPerThread = iterationsPerThread;
      this.expectedCount = expectedCount;
      this.actualCount = actualCount;
      this.lostUpdates = lostUpdates;
      this.executionTimeMs = executionTimeMs;
      this.isCorrect = expectedCount == actualCount;
    }
  }
}
