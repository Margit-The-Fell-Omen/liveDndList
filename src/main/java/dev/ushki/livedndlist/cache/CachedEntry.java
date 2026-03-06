package dev.ushki.livedndlist.cache;

import java.time.Instant;

public class CachedEntry<T> {

  private final T data;
  private final Instant cachedAt;

  public CachedEntry(T data) {
    this.data = data;
    this.cachedAt = Instant.now();
  }

  public T getData() {
    return data;
  }

  public Instant getCachedAt() {
    return cachedAt;
  }

  public boolean isExpired(long ttlSeconds) {
    return Instant.now().isAfter(cachedAt.plusSeconds(ttlSeconds));
  }
}
