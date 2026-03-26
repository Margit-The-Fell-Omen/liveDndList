package dev.ushki.livedndlist.controller.sync;

import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.DndClass;
import dev.ushki.livedndlist.repository.DndClassRepository;
import dev.ushki.livedndlist.service.Open5eClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/sync/classes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Class Sync", description = "API for syncing classes from Open5e")
@SecurityRequirement(name = "bearerAuth")
public class ClassSyncController {

  private final Open5eClassService classSyncService;
  private final DndClassRepository dndClassRepository;

  @GetMapping("/status")
  @Operation(summary = "Get sync status")
  public ResponseEntity<SyncStatusDto> getSyncStatus() {
    return ResponseEntity.ok(classSyncService.getSyncStatus());
  }

  @PostMapping
  @Operation(summary = "Start synchronization of all classes")
  public ResponseEntity<SyncResultDto> syncAllClasses() {
    log.info("Received request to sync all classes");
    SyncResultDto result = classSyncService.syncAllClasses();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/async")
  @Operation(summary = "Start asynchronous synchronization of all classes")
  public ResponseEntity<String> syncAllClassesAsync() {
    log.info("Received request to async sync all classes");

    SyncStatusDto status = classSyncService.getSyncStatus();
    if (status.isInProgress()) {
      return ResponseEntity.badRequest().body("Sync already in progress");
    }

    CompletableFuture.runAsync(classSyncService::syncAllClasses);

    return ResponseEntity.accepted()
        .body("Sync started. Check status at: GET /api/sync/classes/status");
  }

  @PostMapping("/{slug}")
  @Operation(summary = "Sync specific class by slug")
  public ResponseEntity<SyncResultDto> syncClassBySlug(@PathVariable String slug) {
    log.info("Received request to sync class: {}", slug);
    SyncResultDto result = classSyncService.syncBySlug(slug);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping
  @Operation(summary = "Delete all classes from database")
  public ResponseEntity<SyncResultDto> clearAllClasses() {
    log.info("Received request to delete all classes");
    SyncResultDto result = classSyncService.clearAll();
    return ResponseEntity.ok(result);
  }

  @GetMapping("/count")
  @Operation(summary = "Get class count in database")
  public ResponseEntity<Long> getClassCount() {
    return ResponseEntity.ok(dndClassRepository.count());
  }

  @GetMapping("/list")
  @Operation(summary = "Get list of all classes from database")
  public ResponseEntity<List<ClassSummary>> getAllClasses() {
    List<DndClass> classes = dndClassRepository.findAll();
    List<ClassSummary> summaries = classes.stream()
        .map(c -> new ClassSummary(c.getId(), c.getName(), c.getSlug(), c.getHitDice()))
        .toList();
    return ResponseEntity.ok(summaries);
  }

  public record ClassSummary(Long id, String name, String slug, String hitDice) {

  }
}
