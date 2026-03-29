package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.service.sync.SyncMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "System metrics and monitoring")
@SecurityRequirement(name = "bearerAuth")
public class MetricsController {

  private final SyncMetrics syncMetrics;

  @GetMapping("/sync")
  @Operation(summary = "Get sync metrics")
  public ResponseEntity<SyncMetrics.MetricsSnapshot> getSyncMetrics() {
    return ResponseEntity.ok(syncMetrics.getSnapshot());
  }

  @PostMapping("/sync/reset")
  @Operation(summary = "Reset sync metrics")
  public ResponseEntity<Void> resetSyncMetrics() {
    syncMetrics.reset();
    return ResponseEntity.noContent().build();
  }
}
