package dev.ushki.livedndlist.controller.sync;

import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.DndClassResponse;
import dev.ushki.livedndlist.repository.DndClassRepository;
import dev.ushki.livedndlist.service.Open5eClassService;
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
@RequestMapping("/api/v1/sync/classes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Class Sync", description = "API for syncing classes from Open5e")
@SecurityRequirement(name = "bearerAuth")
public class ClassSyncController {

  private final Open5eClassService classSyncService;
  private final DndClassRepository dndClassRepository;

  @GetMapping("/status")
  @Operation(summary = "Get sync status")
  public ApiResponse<SyncStatusDto> getSyncStatus() {
    return ApiResponse.success(classSyncService.getSyncStatus());
  }

  @PostMapping
  @Operation(summary = "Start synchronization of all classes")
  public ApiResponse<SyncResultDto> syncAllClasses() {
    log.info("Received request to sync all classes");
    SyncResultDto result = classSyncService.syncAllClasses();
    return result.isSuccess() ? ApiResponse.success(result)
        : ApiResponse.error(result.getMessage(), result);
  }

  @PostMapping("/async")
  @Operation(summary = "Start asynchronous synchronization of all classes")
  public ApiResponse<String> syncAllClassesAsync() {
    log.info("Received request to async sync all classes");

    SyncStatusDto status = classSyncService.getSyncStatus();
    if (status.isInProgress()) {
      return ApiResponse.error("Sync already in progress");
    }

    CompletableFuture.runAsync(classSyncService::syncAllClasses);

    return ApiResponse.success("Sync started. Check status at: GET /api/sync/classes/status");
  }

  @DeleteMapping
  @Operation(summary = "Delete all classes from database")
  public ApiResponse<SyncResultDto> clearAllClasses() {
    log.info("Received request to delete all classes");
    SyncResultDto result = classSyncService.clearAll();
    return result.isSuccess() ? ApiResponse.success(result)
        : ApiResponse.error(result.getMessage(), result);
  }

  @GetMapping("/count")
  @Operation(summary = "Get class count in database")
  public ApiResponse<Long> getClassCount() {
    return ApiResponse.success(dndClassRepository.count());
  }

  @GetMapping("/list")
  @Operation(summary = "Get list of all classes from database")
  public ApiResponse<List<DndClassResponse>> getAllClasses() {
    List<DndClassResponse> classes = classSyncService.getAllClasses();
    return ApiResponse.success(classes);
  }

}
