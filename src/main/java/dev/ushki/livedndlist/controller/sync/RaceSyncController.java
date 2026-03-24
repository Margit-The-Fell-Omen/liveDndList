package dev.ushki.livedndlist.controller.sync;

import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.Race;
import dev.ushki.livedndlist.repository.RaceRepository;
import dev.ushki.livedndlist.service.Open5eRaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/sync/races")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Race Sync", description = "API для синхронизации рас из Open5e")
public class RaceSyncController {

  private final Open5eRaceService raceSyncService;
  private final RaceRepository raceRepository;

  @GetMapping("/status")
  @Operation(summary = "Получить статус синхронизации")
  public ResponseEntity<SyncStatusDto> getSyncStatus() {
    return ResponseEntity.ok(raceSyncService.getSyncStatus());
  }

  @PostMapping
  @Operation(summary = "Запустить синхронизацию всех рас")
  public ResponseEntity<SyncResultDto> syncAllRaces() {
    log.info("Получен запрос на синхронизацию всех рас");
    SyncResultDto result = raceSyncService.syncAllRaces();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/async")
  @Operation(summary = "Запустить асинхронную синхронизацию всех рас")
  public ResponseEntity<String> syncAllRacesAsync() {
    log.info("Получен запрос на асинхронную синхронизацию всех рас");

    SyncStatusDto status = raceSyncService.getSyncStatus();
    if (status.isInProgress()) {
      return ResponseEntity.badRequest()
          .body("Синхронизация уже выполняется");
    }

    CompletableFuture.runAsync(raceSyncService::syncAllRaces);

    return ResponseEntity.accepted()
        .body("Синхронизация запущена. Проверяйте статус: GET /api/sync/races/status");
  }

  @PostMapping("/{slug}")
  @Operation(summary = "Синхронизировать конкретную расу по slug")
  public ResponseEntity<SyncResultDto> syncRaceBySlug(@PathVariable String slug) {
    log.info("Получен запрос на синхронизацию расы: {}", slug);
    SyncResultDto result = raceSyncService.syncRaceBySlug(slug);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping
  @Operation(summary = "Удалить все расы из базы данных")
  public ResponseEntity<SyncResultDto> clearAllRaces() {
    log.info("Получен запрос на удаление всех рас");
    SyncResultDto result = raceSyncService.clearAllRaces();
    return ResponseEntity.ok(result);
  }

  @GetMapping("/count")
  @Operation(summary = "Получить количество рас в базе данных")
  public ResponseEntity<Long> getRaceCount() {
    return ResponseEntity.ok(raceRepository.count());
  }

  @GetMapping("/list")
  @Operation(summary = "Получить список всех рас из БД")
  public ResponseEntity<List<RaceSummary>> getAllRaces() {
    List<Race> races = raceRepository.findAll();
    List<RaceSummary> summaries = races.stream()
        .map(r -> new RaceSummary(r.getId(), r.getName(), r.getSlug(), r.getSizeRaw()))
        .toList();
    return ResponseEntity.ok(summaries);
  }

  public record RaceSummary(Long id, String name, String slug, String size) {

  }
}
