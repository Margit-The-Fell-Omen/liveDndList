package dev.ushki.livedndlist.controller.sync;

import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.DndFeatResponse;
import dev.ushki.livedndlist.repository.DndFeatRepository;
import dev.ushki.livedndlist.service.DndFeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync/feat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Race Sync", description = "API for syncing races from Open5e")
@SecurityRequirement(name = "bearerAuth")
public class DndFeatSyncController {

  private final DndFeatService featSyncService;
  private final DndFeatRepository featRepository;

  @GetMapping("/status")
  @Operation(summary = "Get sync status")
  public ApiResponse<SyncStatusDto> getSyncStatus() {
    return ApiResponse.success(featSyncService.getSyncStatus());
  }

  @PostMapping
  @Operation(summary = "Start synchronization of all classes")
  public ApiResponse<SyncResultDto> syncAllClasses() {
    log.info("Received request to sync all classes");
    SyncResultDto result = featSyncService.syncAllFeats();
    return result.isSuccess() ? ApiResponse.success(result)
        : ApiResponse.error(result.getMessage(), result);
  }

  @PostMapping("/async")
  @Operation(summary = "Start asynchronous synchronization of all classes")
  public ApiResponse<String> syncAllClassesAsync() {
    log.info("Received request to async sync all classes");

    SyncStatusDto status = featSyncService.getSyncStatus();
    if (status.isInProgress()) {
      return ApiResponse.error("Sync already in progress");
    }

    CompletableFuture.runAsync(featSyncService::syncAllFeats);

    return ApiResponse.success("Sync started. Check status at: GET /api/sync/classes/status");
  }

  @DeleteMapping
  @Operation(summary = "Delete all classes from database")
  public ApiResponse<SyncResultDto> clearAllClasses() {
    log.info("Received request to delete all classes");
    SyncResultDto result = featSyncService.clearAll();
    return result.isSuccess() ? ApiResponse.success(result)
        : ApiResponse.error(result.getMessage(), result);
  }

  @GetMapping("/count")
  @Operation(summary = "Get class count in database")
  public ApiResponse<Long> getClassCount() {
    return ApiResponse.success(featRepository.count());
  }

  @GetMapping("/list")
  @Operation(summary = "Get list of all classes from database")
  public ApiResponse<List<DndFeatResponse>> getAllClasses() {
    List<DndFeatResponse> classes = featSyncService.getAllFeats();
    return ApiResponse.success(classes);
  }
}
