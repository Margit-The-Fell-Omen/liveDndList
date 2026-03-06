package dev.ushki.livedndlist.cache;

import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.dto.response.PageResponse;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CharacterQueryIndex {

  private final Map<CharacterQueryKey, CachedEntry<PageResponse<CharacterSummaryResponse>>> index;

  private final Map<Long, Set<CharacterQueryKey>> userKeyIndex;

  private final AtomicLong hitCount = new AtomicLong(0);
  private final AtomicLong missCount = new AtomicLong(0);

  private final long ttlSeconds;

  private final int maxSize;

  public CharacterQueryIndex(
      @Value("${app.cache.character-index.ttl-seconds:300}") long ttlSeconds,
      @Value("${app.cache.character-index.max-size:1000}") int maxSize) {

    this.index = new ConcurrentHashMap<>();
    this.userKeyIndex = new ConcurrentHashMap<>();
    this.ttlSeconds = ttlSeconds;
    this.maxSize = maxSize;

    log.info("CharacterQueryIndex initialized with TTL={}s, maxSize={}", ttlSeconds, maxSize);
  }

  public Optional<PageResponse<CharacterSummaryResponse>> get(CharacterQueryKey key) {
    CachedEntry<PageResponse<CharacterSummaryResponse>> entry = index.get(key);

    if (entry == null) {
      missCount.incrementAndGet();
      log.debug("Cache MISS for key: {}", key);
      return Optional.empty();
    }

    if (entry.isExpired(ttlSeconds)) {
      // Remove expired entry
      remove(key);
      missCount.incrementAndGet();
      log.debug("Cache EXPIRED for key: {}", key);
      return Optional.empty();
    }

    hitCount.incrementAndGet();
    log.debug("Cache HIT for key: {}", key);
    return Optional.of(entry.getData());
  }

  public void put(CharacterQueryKey key, PageResponse<CharacterSummaryResponse> data) {
    if (index.size() >= maxSize) {
      evictExpiredEntries();

      if (index.size() >= maxSize) {
        evictOldestEntries(maxSize / 10); // Evict 10%
      }
    }

    index.put(key, new CachedEntry<>(data));

    userKeyIndex.computeIfAbsent(key.getUserId(), k -> ConcurrentHashMap.newKeySet())
        .add(key);

    log.debug("Cached result for key: {}", key);
  }

  public void remove(CharacterQueryKey key) {
    index.remove(key);

    Set<CharacterQueryKey> userKeys = userKeyIndex.get(key.getUserId());
    if (userKeys != null) {
      userKeys.remove(key);
    }
  }

  public void invalidateByUser(Long userId) {
    Set<CharacterQueryKey> keysToRemove = userKeyIndex.remove(userId);

    if (keysToRemove != null && !keysToRemove.isEmpty()) {
      keysToRemove.forEach(index::remove);
      log.info("Invalidated {} cached entries for user {}", keysToRemove.size(), userId);
    }
  }

  public void invalidateAll() {
    index.clear();
    userKeyIndex.clear();
    hitCount.set(0);
    missCount.set(0);
    int size = index.size();
    log.info("Invalidated all {} cached entries", size);
  }

  public void evictExpiredEntries() {
    int beforeSize = index.size();

    Set<CharacterQueryKey> expiredKeys = index.entrySet().stream()
        .filter(entry -> entry.getValue().isExpired(ttlSeconds))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());

    expiredKeys.forEach(this::remove);

    int evicted = beforeSize - index.size();
    if (evicted > 0) {
      log.info("Evicted {} expired entries", evicted);
    }
  }

  private void evictOldestEntries(int count) {
    index.entrySet().stream()
        .sorted(Comparator.comparing(e -> e.getValue().getCachedAt()))
        .limit(count)
        .map(Map.Entry::getKey)
        .toList()
        .forEach(this::remove);

    log.info("Evicted {} oldest entries", count);
  }

  public CacheStats getStats() {
    return new CacheStats(
        index.size(),
        hitCount.get(),
        missCount.get(),
        maxSize,
        ttlSeconds
    );
  }

  public record CacheStats(
      int currentSize,
      long hits,
      long misses,
      int maxSize,
      long ttlSeconds
  ) {

    public double hitRate() {
      long total = hits + misses;
      return total == 0 ? 0.0 : (double) hits / total * 100;
    }
  }
}
