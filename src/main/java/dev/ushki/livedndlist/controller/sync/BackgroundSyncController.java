package dev.ushki.livedndlist.controller.sync;

import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.repository.BackgroundRepository;
import dev.ushki.livedndlist.service.Open5eBackgroundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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

}
