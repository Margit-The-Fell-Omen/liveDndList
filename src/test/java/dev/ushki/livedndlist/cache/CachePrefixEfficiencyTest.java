package dev.ushki.livedndlist.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link CacheManager} prefix invalidation is as narrow as possible:
 * a service invalidating a per-user prefix must not evict another user's entries,
 * and an exact-namespace invalidation must not evict sibling entries.
 *
 * <p>The namespaces below mirror the convention used across services:
 * <pre>
 *   User:{username}:Characters      -> list of a user's characters
 *   User:{username}:Character:{id}  -> a single character
 *   Equipment / Spells / Users / FeatureCatalog -> global catalogs
 * </pre>
 */
@DisplayName("Cache prefix invalidation efficiency")
class CachePrefixEfficiencyTest {

  private static final CompositeKey KEY = new CompositeKey("dummy");

  private CacheManager cacheManager;

  @BeforeEach
  void setUp() {
    cacheManager = new CacheManager();
  }

  private void populate(String... namespaces) {
    for (String namespace : namespaces) {
      cacheManager.get(namespace, KEY, () -> namespace + "-value");
    }
  }

  /** Returns true if the namespace is still cached (subsequent get is a hit). */
  private boolean isCached(String namespace) {
    AtomicInteger calls = new AtomicInteger();
    cacheManager.get(namespace, KEY, (Supplier<String>) () -> {
      calls.incrementAndGet();
      return namespace + "-fresh";
    });
    return calls.get() == 0;
  }

  @Test
  @DisplayName("Per-user prefix invalidates only that user's namespaces")
  void perUserPrefixInvalidatesOnlyThatUser() {
    populate(
        "User:alice:Characters",
        "User:alice:Character:1",
        "User:alice:Character:2",
        "User:bob:Characters",
        "User:bob:Character:1",
        "User:alice2:Characters");

    cacheManager.invalidateByPrefix("User:alice");

    assertThat(isCached("User:alice:Characters")).isFalse();
    assertThat(isCached("User:alice:Character:1")).isFalse();
    assertThat(isCached("User:alice:Character:2")).isFalse();

    assertThat(isCached("User:bob:Characters")).isTrue();
    assertThat(isCached("User:bob:Character:1")).isTrue();
    assertThat(isCached("User:alice2:Characters")).isTrue();
  }

  @Test
  @DisplayName("Username boundary: 'User:alice' must not evict 'User:alice2'")
  void usernamePrefixRespectsBoundary() {
    populate("User:alice:Characters", "User:alice2:Characters");

    cacheManager.invalidateByPrefix("User:alice");

    assertThat(isCached("User:alice:Characters")).isFalse();
    assertThat(isCached("User:alice2:Characters")).isTrue();
  }

  @Test
  @DisplayName("Exact namespace invalidation does not evict sibling entries")
  void exactNamespaceInvalidationIsTargeted() {
    populate(
        "User:alice:Character:1",
        "User:alice:Character:2",
        "User:alice:Characters");

    cacheManager.invalidate("User:alice:Character:1");

    assertThat(isCached("User:alice:Character:1")).isFalse();
    assertThat(isCached("User:alice:Character:2")).isTrue();
    assertThat(isCached("User:alice:Characters")).isTrue();
  }

  @Test
  @DisplayName("Global catalog invalidation does not touch other catalogs")
  void globalNamespaceInvalidationIsIsolated() {
    populate("Equipment", "Spells", "Users", "FeatureCatalog");

    cacheManager.invalidateByPrefix("Equipment");

    assertThat(isCached("Equipment")).isFalse();
    assertThat(isCached("Spells")).isTrue();
    assertThat(isCached("Users")).isTrue();
    assertThat(isCached("FeatureCatalog")).isTrue();
  }
}