package dev.ushki.livedndlist.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheManager {

  private final Map<String, Map<CompositeKey, Object>> caches = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T get(String namespace, CompositeKey key, Supplier<T> dbCall) {
    Map<CompositeKey, Object> cache = caches.computeIfAbsent(namespace,
        k -> new ConcurrentHashMap<>());

    if (cache.containsKey(key)) {
      log.info("CACHE HIT | Namespace: {} | Key: {}", namespace, key);
      return (T) cache.get(key);
    }

    log.info("CACHE MISS | Namespace: {} | Key: {}", namespace, key);
    T value = dbCall.get();
    if (value != null) {
      cache.put(key, value);
    }
    return value;
  }

  public void invalidate(String namespace) {
    log.info("CACHE INVALIDATE | Namespace: {}", namespace);
    caches.remove(namespace);
  }

  public void invalidateByPrefix(String prefix) {
    log.info("CACHE INVALIDATE PREFIX | Prefix: {}", prefix);
    caches.keySet().removeIf(k -> k.startsWith(prefix));
  }
}
