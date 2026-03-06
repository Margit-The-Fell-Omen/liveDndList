package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.cache.CharacterQueryIndex;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CacheAdminController {

  private final CharacterQueryIndex queryIndex;

  @GetMapping("/stats")
  public ApiResponse<CharacterQueryIndex.CacheStats> getCacheStats() {
    return ApiResponse.success(queryIndex.getStats());
  }

  @DeleteMapping
  public ApiResponse<Void> clearCache() {
    queryIndex.invalidateAll();
    return ApiResponse.success("Cache cleared successfully");
  }

  @DeleteMapping("/expired")
  public ApiResponse<Void> evictExpired() {
    queryIndex.evictExpiredEntries();
    return ApiResponse.success("Expired entries evicted");
  }
}
