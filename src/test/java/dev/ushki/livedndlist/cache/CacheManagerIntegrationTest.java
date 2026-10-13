package dev.ushki.livedndlist.cache;

import static org.assertj.core.api.Assertions.assertThat;

import dev.ushki.livedndlist.AbstractIntegrationTest;
import dev.ushki.livedndlist.service.features.FeatureRef;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CacheManagerIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private CacheManager cacheManager;

  @Test
  void cachesBetweenGetsAndInvalidates() {
    AtomicInteger calls = new AtomicInteger();
    Supplier<String> dbCall = () -> "value-" + calls.incrementAndGet();

    String first = cacheManager.get("IT", new CompositeKey("a", 1), dbCall);
    String second = cacheManager.get("IT", new CompositeKey("a", 1), dbCall);

    assertThat(first).isEqualTo("value-1");
    assertThat(second).isEqualTo("value-1");
    assertThat(calls.get()).isEqualTo(1);

    cacheManager.invalidate("IT");

    String third = cacheManager.get("IT", new CompositeKey("a", 1), dbCall);
    assertThat(third).isEqualTo("value-2");
    assertThat(calls.get()).isEqualTo(2);
  }

  @Test
  void invalidateByPrefixRespectsBoundary() {
    Supplier<String> dbA = () -> "A";
    Supplier<String> dbB = () -> "B";

    cacheManager.get("User:alice:Characters", new CompositeKey("x"), dbA);
    cacheManager.get("User:alice2:Characters", new CompositeKey("x"), dbB);

    cacheManager.invalidateByPrefix("User:alice");

    AtomicInteger aCalls = new AtomicInteger();
    cacheManager.get("User:alice:Characters", new CompositeKey("x"),
        () -> "A2-" + aCalls.incrementAndGet());
    assertThat(aCalls.get()).isEqualTo(1);

    AtomicInteger bCalls = new AtomicInteger();
    String b = cacheManager.get("User:alice2:Characters", new CompositeKey("x"),
        () -> "B2-" + bCalls.incrementAndGet());
    assertThat(b).isEqualTo("B");
    assertThat(bCalls.get()).isZero();
  }

  @Test
  void roundTripsCatalogDtos() {
    FeatureRef ref = FeatureRef.builder()
        .id(42L)
        .key("race_human_trait")
        .name("A Trait")
        .gainedAtLevel(null)
        .build();

    List<FeatureRef> stored = cacheManager.get("Catalog", new CompositeKey("bySource", "RACE"),
        () -> List.of(ref));
    List<FeatureRef> loaded = cacheManager.get("Catalog", new CompositeKey("bySource", "RACE"),
        () -> List.of());

    assertThat(loaded).hasSize(1);
    assertThat(loaded.get(0).getId()).isEqualTo(42L);
    assertThat(loaded.get(0).getKey()).isEqualTo("race_human_trait");
    assertThat(stored).hasSize(1);
  }
}