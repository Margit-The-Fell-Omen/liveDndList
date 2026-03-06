package dev.ushki.livedndlist.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheMaintenanceScheduler {

  private final CharacterQueryIndex queryIndex;

  @Scheduled(fixedRate = 300000) // 5 minutes
  public void cleanupExpiredEntries() {
    log.debug("Running scheduled cache cleanup");
    queryIndex.evictExpiredEntries();
  }

  @Scheduled(fixedRate = 3600000) // 1 hour
  public void logCacheStats() {
    CharacterQueryIndex.CacheStats stats = queryIndex.getStats();
    log.info("Cache stats - Size: {}/{}, Hits: {}, Misses: {}, Hit Rate: {}%",
        stats.currentSize(),
        stats.maxSize(),
        stats.hits(),
        stats.misses(),
        stats.hitRate());
  }
}
