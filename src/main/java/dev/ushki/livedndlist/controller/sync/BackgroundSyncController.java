package dev.ushki.livedndlist.controller.sync;

import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundBenefitDto;
import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.repository.BackgroundRepository;
import dev.ushki.livedndlist.service.Open5eBackgroundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backgrounds")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Background Sync", description = "API for syncing backgrounds from Open5e")
@SecurityRequirement(name = "bearerAuth")
public class BackgroundSyncController {

  private final Open5eBackgroundService backgroundService;
  private final BackgroundRepository backgroundRepository;

  @GetMapping("/status")
  @Operation(summary = "Get sync status")
  public ApiResponse<SyncStatusDto> getSyncStatus() {
    return ApiResponse.success(backgroundService.getSyncStatus());
  }

  @PostMapping
  @Operation(summary = "Start synchronization of all classes")
  public ApiResponse<SyncResultDto> syncAllClasses() {
    log.info("Received request to sync all classes");
    SyncResultDto result = backgroundService.syncAllBackgrounds();
    return result.isSuccess() ? ApiResponse.success(result)
        : ApiResponse.error(result.getMessage(), result);
  }

  @PostMapping("/async")
  @Operation(summary = "Start asynchronous synchronization of all classes")
  public ApiResponse<String> syncAllClassesAsync() {
    log.info("Received request to async sync all classes");

    SyncStatusDto status = backgroundService.getSyncStatus();
    if (status.isInProgress()) {
      return ApiResponse.error("Sync already in progress");
    }

    CompletableFuture.runAsync(backgroundService::syncAllBackgrounds);

    return ApiResponse.success("Sync started. Check status at: GET /api/sync/backgrounds/status");
  }

  @DeleteMapping
  @Operation(summary = "Delete all classes from database")
  public ApiResponse<SyncResultDto> clearAllClasses() {
    log.info("Received request to delete all classes");
    SyncResultDto result = backgroundService.clearAll();
    return result.isSuccess() ? ApiResponse.success(result)
        : ApiResponse.error(result.getMessage(), result);
  }

  @GetMapping("/count")
  @Operation(summary = "Get class count in database")
  public ApiResponse<Long> getClassCount() {
    return ApiResponse.success(backgroundRepository.count());
  }

  @GetMapping("/list")
  @Operation(summary = "Get list of all backgrounds from database")
  public ApiResponse<List<Open5eBackgroundDto>> getAllBackgrounds() {
    List<Open5eBackgroundDto> backgrounds = backgroundService.getAllBackgrounds();
    return ApiResponse.success(backgrounds);
  }

  @GetMapping("/{key}/benefits")
  @Operation(summary = "Get all benefits of background by key")
  public ApiResponse<List<Open5eBackgroundBenefitDto>> getBenefitsByBackground(
      @PathVariable String key) {
    List<Open5eBackgroundBenefitDto> benefits = backgroundService.getBenefitsByBackground(key);
    return ApiResponse.success(benefits);
  }

}
