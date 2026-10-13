package dev.ushki.livedndlist.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheManager {

  private static final String DEFAULT_PREFIX = "cache";
  private static final long DEFAULT_TTL_SECONDS = 600L;

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper cacheObjectMapper;
  private final String cachePrefix;
  private final long ttlSeconds;

  private final Map<String, Map<CompositeKey, Object>> inMemory = new ConcurrentHashMap<>();

  public CacheManager() {
    this(null, null, DEFAULT_PREFIX, DEFAULT_TTL_SECONDS);
  }

  @Autowired
  public CacheManager(
      StringRedisTemplate redisTemplate,
      @Qualifier("cacheObjectMapper") ObjectMapper cacheObjectMapper,
      @Value("${cache.prefix:cache}") String cachePrefix,
      @Value("${cache.ttl-seconds:600}") long ttlSeconds) {
    this.redisTemplate = redisTemplate;
    this.cacheObjectMapper = cacheObjectMapper;
    this.cachePrefix = cachePrefix;
    this.ttlSeconds = ttlSeconds;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String namespace, CompositeKey key, Supplier<T> dbCall) {
    if (redisTemplate == null || cacheObjectMapper == null) {
      return getFromMemory(namespace, key, dbCall);
    }

    String redisKey = buildKey(namespace, key);
    try {
      String json = redisTemplate.opsForValue().get(redisKey);
      if (json != null) {
        log.info("CACHE HIT | Namespace: {} | Key: {}", namespace, key);
        Object read = cacheObjectMapper.readValue(json, Object.class);
        if (read instanceof CacheEntry entry) {
          return (T) entry.getValue();
        }
        return (T) read;
      }
    } catch (Exception e) {
      log.warn("Redis read failed for {}; falling back to DB", redisKey, e);
    }

    log.info("CACHE MISS | Namespace: {} | Key: {}", namespace, key);
    T value = dbCall.get();
    if (value != null) {
      try {
        String json = cacheObjectMapper.writeValueAsString(new CacheEntry(value));
        redisTemplate.opsForValue().set(redisKey, json, ttlSeconds, TimeUnit.SECONDS);
      } catch (Exception e) {
        log.warn("Redis write failed for {}", redisKey, e);
      }
    }
    return value;
  }

  public void invalidate(String namespace) {
    if (redisTemplate == null) {
      inMemory.remove(namespace);
      return;
    }
    try {
      deleteKeys(namespace, scanKeys(cachePrefix + ":" + namespace + ":*"));
    } catch (Exception e) {
      log.warn("Redis invalidate failed for namespace {}", namespace, e);
    }
  }

  public void invalidateByPrefix(String prefix) {
    if (redisTemplate == null) {
      String boundary = prefix;
      inMemory.keySet().removeIf(k -> k.equals(boundary) || k.startsWith(boundary + ":"));
      return;
    }
    try {
      Set<String> candidates = scanKeys(cachePrefix + ":" + prefix + "*");
      String boundary = cachePrefix + ":" + prefix;
      Set<String> keys = new HashSet<>();
      for (String candidate : candidates) {
        if (candidate.equals(boundary) || candidate.startsWith(boundary + ":")) {
          keys.add(candidate);
        }
      }
      deleteKeys(prefix, keys);
    } catch (Exception e) {
      log.warn("Redis invalidateByPrefix failed for prefix {}", prefix, e);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getFromMemory(String namespace, CompositeKey key, Supplier<T> dbCall) {
    Map<CompositeKey, Object> cache = inMemory.computeIfAbsent(namespace,
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

  private String buildKey(String namespace, CompositeKey key) {
    return cachePrefix + ":" + namespace + ":" + sha256Hex(key.toKeyString());
  }

  private String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) {
        sb.append(Character.forDigit((b >> 4) & 0xF, 16));
        sb.append(Character.forDigit(b & 0xF, 16));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private Set<String> scanKeys(String pattern) {
    return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
      Set<String> keys = new HashSet<>();
      try (Cursor<byte[]> cursor = connection.scan(
          ScanOptions.scanOptions().match(pattern).count(100).build())) {
        while (cursor.hasNext()) {
          keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
        }
      } catch (Exception e) {
        log.warn("Redis SCAN failed for pattern {}", pattern, e);
      }
      return keys;
    });
  }

  private void deleteKeys(String label, Set<String> keys) {
    if (keys == null || keys.isEmpty()) {
      return;
    }
    for (String key : keys) {
      log.info("CACHE EVICT | {} | Key: {}", label, key);
    }
    redisTemplate.delete(keys);
  }
}