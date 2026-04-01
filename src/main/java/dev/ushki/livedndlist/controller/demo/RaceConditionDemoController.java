package dev.ushki.livedndlist.controller.demo;

import dev.ushki.livedndlist.demo.SafeCounterAtomic;
import dev.ushki.livedndlist.demo.SafeCounterSynchronized;
import dev.ushki.livedndlist.demo.UnsafeCounter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/demo/race-condition")
@Slf4j
@Tag(name = "Race Condition Demo",
     description = "Demonstration of race condition and thread-safe solutions")
@SecurityRequirement(name = "bearerAuth")
public class RaceConditionDemoController {

  private static final int TERMINATION_TIMEOUT_SECONDS = 30;

  @GetMapping("/unsafe")
  @Operation(summary = "Demonstrate race condition with unsafe counter")
  public RaceConditionResult testUnsafeCounter(
      @RequestParam(defaultValue = "100") int threads,
      @RequestParam(defaultValue = "1000") int iterations) throws InterruptedException {

    UnsafeCounter counter = new UnsafeCounter();
    long duration = executeCounterTest(counter::increment, threads, iterations);

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
    long duration = executeCounterTest(counter::increment, threads, iterations);

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
    long duration = executeCounterTest(counter::increment, threads, iterations);

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

  @GetMapping("/compare-all")
  @Operation(summary = "Compare all counter implementations")
  public ComparisonResult compareAll(
      @RequestParam(defaultValue = "100") int threads,
      @RequestParam(defaultValue = "1000") int iterations) throws InterruptedException {

    RaceConditionResult unsafeResult = testUnsafeCounter(threads, iterations);
    RaceConditionResult synchronizedResult = testSafeSynchronized(threads, iterations);
    RaceConditionResult atomicResult = testSafeAtomic(threads, iterations);

    return new ComparisonResult(unsafeResult, synchronizedResult, atomicResult);
  }

  private long executeCounterTest(Runnable incrementAction, int threads, int iterations)
      throws InterruptedException {

    long startTime = System.currentTimeMillis();
    ExecutorService executor = Executors.newFixedThreadPool(threads);

    try {
      for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
          for (int j = 0; j < iterations; j++) {
            incrementAction.run();
          }
        });
      }

      executor.shutdown();
      boolean terminated = executor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

      if (!terminated) {
        log.warn("Executor did not terminate in time, forcing shutdown");
        executor.shutdownNow();
      }

    } finally {
      if (!executor.isTerminated()) {
        log.warn("Forcing executor shutdown in finally block");
        executor.shutdownNow();
      }
    }

    return System.currentTimeMillis() - startTime;
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
    private final boolean correct;
    private final double lostPercentage;

    public RaceConditionResult(String counterType, int threads, int iterationsPerThread,
        int expectedCount, int actualCount, int lostUpdates, long executionTimeMs) {
      this.counterType = counterType;
      this.threads = threads;
      this.iterationsPerThread = iterationsPerThread;
      this.expectedCount = expectedCount;
      this.actualCount = actualCount;
      this.lostUpdates = lostUpdates;
      this.executionTimeMs = executionTimeMs;
      this.correct = expectedCount == actualCount;
      this.lostPercentage = expectedCount > 0
          ? Math.round((double) lostUpdates / expectedCount * 10000.0) / 100.0
          : 0;
    }
  }

  @Data
  public static class ComparisonResult {

    private final RaceConditionResult unsafe;
    private final RaceConditionResult synchronizedCounter;
    private final RaceConditionResult atomic;
    private final String recommendation;

    public ComparisonResult(RaceConditionResult unsafe,
        RaceConditionResult synchronizedCounter,
        RaceConditionResult atomic) {
      this.unsafe = unsafe;
      this.synchronizedCounter = synchronizedCounter;
      this.atomic = atomic;
      this.recommendation = generateRecommendation(synchronizedCounter, atomic);
    }

    private String generateRecommendation(RaceConditionResult sync, RaceConditionResult atomic) {
      if (atomic.getExecutionTimeMs() < sync.getExecutionTimeMs()) {
        double speedup = (double) sync.getExecutionTimeMs() / atomic.getExecutionTimeMs();
        return String.format(
            "AtomicInteger is %.1fx faster than synchronized. "
                + "Use Atomic for simple counter operations.",
            speedup
        );
      } else {
        return "Performance is similar. Choose based on use case complexity.";
      }
    }
  }
}
