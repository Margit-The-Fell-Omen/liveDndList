package dev.ushki.livedndlist.controller.sync;

import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.entity.character.Race;
import dev.ushki.livedndlist.repository.RaceRepository;
import dev.ushki.livedndlist.service.Open5eRaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync/races")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Race Sync", description = "API for syncing races from Open5e")
@SecurityRequirement(name = "bearerAuth")
public class RaceSyncController {

  private final Open5eRaceService raceSyncService;
  private final RaceRepository raceRepository;

  @GetMapping("/status")
  @Operation(summary = "Get sync status")
  public ApiResponse<SyncStatusDto> getSyncStatus() {
    return ApiResponse.success(raceSyncService.getSyncStatus());
  }

  @PostMapping
  @Operation(summary = "Start synchronization of all races")
  public ApiResponse<SyncResultDto> syncAllRaces() {
    log.info("Received request to sync all races");
    SyncResultDto result = raceSyncService.syncAllRaces();
    return ApiResponse.success(result);
  }

  @PostMapping("/async")
  @Operation(summary = "Start asynchronous synchronization of all races")
  public ApiResponse<String> syncAllRacesAsync() {
    log.info("Received request to async sync all races");

    SyncStatusDto status = raceSyncService.getSyncStatus();
    if (status.isInProgress()) {
      return ApiResponse.error("Sync already in progress");
    }

    CompletableFuture.runAsync(raceSyncService::syncAllRaces);

    return ApiResponse.error("Sync started. Check status at: GET /api/sync/races/status");
  }

  @PostMapping("/{slug}")
  @Operation(summary = "Sync specific race by slug")
  public ApiResponse<SyncResultDto> syncRaceBySlug(@PathVariable String slug) {
    log.info("Received request to sync race: {}", slug);
    SyncResultDto result = raceSyncService.syncBySlug(slug);
    return ApiResponse.success(result);
  }

  @DeleteMapping
  @Operation(summary = "Delete all races from database")
  public ApiResponse<SyncResultDto> clearAllRaces() {
    log.info("Received request to delete all races");
    SyncResultDto result = raceSyncService.clearAll();
    return ApiResponse.success(result);
  }

  @GetMapping("/count")
  @Operation(summary = "Get race count in database")
  public ApiResponse<Long> getRaceCount() {
    return ApiResponse.success(raceRepository.count());
  }

  @GetMapping("/list")
  @Operation(summary = "Get list of all races from database")
  public ApiResponse<List<RaceSummary>> getAllRaces() {
    List<Race> races = raceRepository.findAll();
    List<RaceSummary> summaries = races.stream()
        .map(r -> new RaceSummary(r.getId(), r.getName(), r.getSlug(), r.getSizeRaw()))
        .toList();
    return ApiResponse.success(summaries);
  }

  public record RaceSummary(Long id, String name, String slug, String size) {

  }
}
