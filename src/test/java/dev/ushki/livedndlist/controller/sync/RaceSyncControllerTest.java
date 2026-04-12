package dev.ushki.livedndlist.controller.sync;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.Race;
import dev.ushki.livedndlist.repository.RaceRepository;
import dev.ushki.livedndlist.security.jwt.JwtAuthenticationEntryPoint;
import dev.ushki.livedndlist.security.jwt.JwtAuthenticationFilter;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import dev.ushki.livedndlist.service.Open5eRaceService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = RaceSyncController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
class RaceSyncControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private Open5eRaceService raceSyncService;

  @MockitoBean
  private RaceRepository raceRepository;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockitoBean
  private UserDetailsService userDetailsService;

  private SyncStatusDto syncStatusInProgress;
  private SyncStatusDto syncStatusIdle;
  private SyncResultDto successfulSyncResult;
  private SyncResultDto failedSyncResult;
  private List<Race> testRaces;

  @BeforeEach
  void setUp() {
    syncStatusInProgress = SyncStatusDto.builder()
        .inProgress(true)
        .currentOperation("Syncing races")
        .processedCount(5)
        .totalCount(10)
        .progressPercent(50.0)
        .build();

    syncStatusIdle = SyncStatusDto.builder()
        .inProgress(false)
        .currentOperation(null)
        .processedCount(0)
        .totalCount(0)
        .progressPercent(0.0)
        .build();

    successfulSyncResult = SyncResultDto.builder()
        .success(true)
        .message("Sync completed successfully")
        .syncedAt(LocalDateTime.now())
        .statistics(SyncResultDto.SyncStatistics.builder()
            .totalFetched(10)
            .created(8)
            .updated(2)
            .failed(0)
            .durationMs(5000L)
            .build())
        .errors(List.of())
        .build();

    failedSyncResult = SyncResultDto.builder()
        .success(false)
        .message("Sync failed")
        .syncedAt(LocalDateTime.now())
        .statistics(SyncResultDto.SyncStatistics.builder()
            .totalFetched(10)
            .created(2)
            .updated(1)
            .failed(7)
            .durationMs(2000L)
            .build())
        .errors(List.of("Error syncing Human", "Error syncing Elf"))
        .build();

    Race human = Race.builder()
        .id(1L)
        .name("Human")
        .slug("human")
        .sizeRaw("Medium")
        .build();

    Race elf = Race.builder()
        .id(2L)
        .name("Elf")
        .slug("elf")
        .sizeRaw("Medium")
        .build();

    Race dwarf = Race.builder()
        .id(3L)
        .name("Dwarf")
        .slug("dwarf")
        .sizeRaw("Medium")
        .build();

    testRaces = List.of(human, elf, dwarf);
  }

  @Nested
  @DisplayName("GET /api/sync/races/status")
  class GetSyncStatusTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return sync status when sync is in progress")
    void shouldReturnSyncStatusWhenInProgress() throws Exception {
      when(raceSyncService.getSyncStatus()).thenReturn(syncStatusInProgress);

      mockMvc.perform(get("/api/sync/races/status"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.inProgress").value(true))
          .andExpect(jsonPath("$.data.currentOperation").value("Syncing races"))
          .andExpect(jsonPath("$.data.processedCount").value(5))
          .andExpect(jsonPath("$.data.totalCount").value(10))
          .andExpect(jsonPath("$.data.progressPercent").value(50.0));

      verify(raceSyncService).getSyncStatus();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return sync status when idle")
    void shouldReturnSyncStatusWhenIdle() throws Exception {
      when(raceSyncService.getSyncStatus()).thenReturn(syncStatusIdle);

      mockMvc.perform(get("/api/sync/races/status"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.inProgress").value(false))
          .andExpect(jsonPath("$.data.processedCount").value(0))
          .andExpect(jsonPath("$.data.totalCount").value(0))
          .andExpect(jsonPath("$.data.progressPercent").value(0.0));

      verify(raceSyncService).getSyncStatus();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/sync/races/status"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("POST /api/sync/races")
  class SyncAllRacesTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should sync all races successfully")
    void shouldSyncAllRacesSuccessfully() throws Exception {
      when(raceSyncService.syncAllRaces()).thenReturn(successfulSyncResult);

      mockMvc.perform(post("/api/sync/races")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.message").value("Sync completed successfully"))
          .andExpect(jsonPath("$.data.statistics.totalFetched").value(10))
          .andExpect(jsonPath("$.data.statistics.created").value(8))
          .andExpect(jsonPath("$.data.statistics.updated").value(2))
          .andExpect(jsonPath("$.data.statistics.failed").value(0))
          .andExpect(jsonPath("$.data.statistics.durationMs").value(5000))
          .andExpect(jsonPath("$.data.errors").isEmpty());

      verify(raceSyncService).syncAllRaces();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return failed sync result")
    void shouldReturnFailedSyncResult() throws Exception {
      when(raceSyncService.syncAllRaces()).thenReturn(failedSyncResult);

      mockMvc.perform(post("/api/sync/races")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.success").value(false))
          .andExpect(jsonPath("$.data.message").value("Sync failed"))
          .andExpect(jsonPath("$.data.statistics.totalFetched").value(10))
          .andExpect(jsonPath("$.data.statistics.created").value(2))
          .andExpect(jsonPath("$.data.statistics.updated").value(1))
          .andExpect(jsonPath("$.data.statistics.failed").value(7))
          .andExpect(jsonPath("$.data.errors.length()").value(2))
          .andExpect(jsonPath("$.data.errors[0]").value("Error syncing Human"))
          .andExpect(jsonPath("$.data.errors[1]").value("Error syncing Elf"));

      verify(raceSyncService).syncAllRaces();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 403 when CSRF token is missing")
    void shouldReturn403WhenCsrfTokenMissing() throws Exception {
      mockMvc.perform(post("/api/sync/races"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("POST /api/sync/races/async")
  class SyncAllRacesAsyncTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should start async sync when no sync in progress")
    void shouldStartAsyncSyncWhenNotInProgress() throws Exception {
      when(raceSyncService.getSyncStatus()).thenReturn(syncStatusIdle);

      mockMvc.perform(post("/api/sync/races/async")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value(
              "Sync started. Check status at: GET /api/sync/races/status"));

      verify(raceSyncService).getSyncStatus();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return bad request when sync already in progress")
    void shouldReturnBadRequestWhenSyncInProgress() throws Exception {
      when(raceSyncService.getSyncStatus()).thenReturn(syncStatusInProgress);

      mockMvc.perform(post("/api/sync/races/async")
              .with(csrf()))
          .andExpect(jsonPath("$.message").value("Sync already in progress"));

      verify(raceSyncService).getSyncStatus();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 403 when CSRF token is missing")
    void shouldReturn403WhenCsrfTokenMissing() throws Exception {
      mockMvc.perform(post("/api/sync/races/async"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("POST /api/sync/races/{slug}")
  class SyncRaceBySlugTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should sync race by slug successfully")
    void shouldSyncRaceBySlugSuccessfully() throws Exception {
      SyncResultDto singleRaceResult = SyncResultDto.builder()
          .success(true)
          .message("Race 'human' synced successfully")
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(1)
              .created(1)
              .updated(0)
              .failed(0)
              .durationMs(500L)
              .build())
          .errors(List.of())
          .build();

      when(raceSyncService.syncBySlug("human")).thenReturn(singleRaceResult);

      mockMvc.perform(post("/api/sync/races/human")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.message").value("Race 'human' synced successfully"))
          .andExpect(jsonPath("$.data.statistics.totalFetched").value(1))
          .andExpect(jsonPath("$.data.statistics.created").value(1));

      verify(raceSyncService).syncBySlug("human");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return failed result when race not found")
    void shouldReturnFailedResultWhenRaceNotFound() throws Exception {
      SyncResultDto notFoundResult = SyncResultDto.builder()
          .success(false)
          .message("Race 'nonexistent' not found")
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(0)
              .created(0)
              .updated(0)
              .failed(1)
              .durationMs(100L)
              .build())
          .errors(List.of("Race not found in Open5e API"))
          .build();

      when(raceSyncService.syncBySlug("nonexistent")).thenReturn(notFoundResult);

      mockMvc.perform(post("/api/sync/races/nonexistent")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.success").value(false))
          .andExpect(jsonPath("$.data.message").value("Race 'nonexistent' not found"))
          .andExpect(jsonPath("$.data.statistics.failed").value(1));

      verify(raceSyncService).syncBySlug("nonexistent");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should sync race with hyphenated slug")
    void shouldSyncRaceWithHyphenatedSlug() throws Exception {
      SyncResultDto halfElfResult = SyncResultDto.builder()
          .success(true)
          .message("Race 'half-elf' synced successfully")
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(1)
              .created(0)
              .updated(1)
              .failed(0)
              .durationMs(450L)
              .build())
          .errors(List.of())
          .build();

      when(raceSyncService.syncBySlug("half-elf")).thenReturn(halfElfResult);

      mockMvc.perform(post("/api/sync/races/half-elf")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.message").value("Race 'half-elf' synced successfully"))
          .andExpect(jsonPath("$.data.statistics.updated").value(1));

      verify(raceSyncService).syncBySlug("half-elf");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 403 when CSRF token is missing")
    void shouldReturn403WhenCsrfTokenMissing() throws Exception {
      mockMvc.perform(post("/api/sync/races/human"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("DELETE /api/sync/races")
  class ClearAllRacesTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should clear all races successfully")
    void shouldClearAllRacesSuccessfully() throws Exception {
      SyncResultDto clearResult = SyncResultDto.builder()
          .success(true)
          .message("All races deleted successfully")
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(0)
              .created(0)
              .updated(0)
              .failed(0)
              .durationMs(200L)
              .build())
          .errors(List.of())
          .build();

      when(raceSyncService.clearAll()).thenReturn(clearResult);

      mockMvc.perform(delete("/api/sync/races")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.success").value(true))
          .andExpect(jsonPath("$.data.message").value("All races deleted successfully"));

      verify(raceSyncService).clearAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return failed result when clear fails")
    void shouldReturnFailedResultWhenClearFails() throws Exception {
      SyncResultDto clearFailedResult = SyncResultDto.builder()
          .success(false)
          .message("Failed to delete races: database error")
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(0)
              .created(0)
              .updated(0)
              .failed(0)
              .durationMs(100L)
              .build())
          .errors(List.of("Database connection failed"))
          .build();

      when(raceSyncService.clearAll()).thenReturn(clearFailedResult);

      mockMvc.perform(delete("/api/sync/races")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.success").value(false))
          .andExpect(jsonPath("$.data.message").value("Failed to delete races: database error"))
          .andExpect(jsonPath("$.data.errors[0]").value("Database connection failed"));

      verify(raceSyncService).clearAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 403 when CSRF token is missing")
    void shouldReturn403WhenCsrfTokenMissing() throws Exception {
      mockMvc.perform(delete("/api/sync/races"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("GET /api/sync/races/count")
  class GetRaceCountTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return race count")
    void shouldReturnRaceCount() throws Exception {
      when(raceRepository.count()).thenReturn(15L);

      mockMvc.perform(get("/api/sync/races/count"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data").value(15));

      verify(raceRepository).count();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/sync/races/count"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/sync/races/list")
  class GetAllRacesTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return list of all races")
    void shouldReturnListOfAllRaces() throws Exception {
      when(raceRepository.findAll()).thenReturn(testRaces);

      mockMvc.perform(get("/api/sync/races/list"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(3))
          .andExpect(jsonPath("$.data[0].id").value(1))
          .andExpect(jsonPath("$.data[0].name").value("Human"))
          .andExpect(jsonPath("$.data[0].slug").value("human"))
          .andExpect(jsonPath("$.data[0].size").value("Medium"))
          .andExpect(jsonPath("$.data[1].id").value(2))
          .andExpect(jsonPath("$.data[1].name").value("Elf"))
          .andExpect(jsonPath("$.data[1].slug").value("elf"))
          .andExpect(jsonPath("$.data[1].size").value("Medium"))
          .andExpect(jsonPath("$.data[2].id").value(3))
          .andExpect(jsonPath("$.data[2].name").value("Dwarf"))
          .andExpect(jsonPath("$.data[2].slug").value("dwarf"))
          .andExpect(jsonPath("$.data[2].size").value("Medium"));

      verify(raceRepository).findAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return empty list when no races exist")
    void shouldReturnEmptyListWhenNoRaces() throws Exception {
      when(raceRepository.findAll()).thenReturn(List.of());

      mockMvc.perform(get("/api/sync/races/list"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(0));

      verify(raceRepository).findAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return races with different sizes")
    void shouldReturnRacesWithDifferentSizes() throws Exception {
      Race halfling = Race.builder()
          .id(4L)
          .name("Halfling")
          .slug("halfling")
          .sizeRaw("Small")
          .build();

      when(raceRepository.findAll()).thenReturn(List.of(halfling));

      mockMvc.perform(get("/api/sync/races/list"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(1))
          .andExpect(jsonPath("$.data[0].name").value("Halfling"))
          .andExpect(jsonPath("$.data[0].size").value("Small"));

      verify(raceRepository).findAll();
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return list for regular user")
    void shouldReturnListForRegularUser() throws Exception {
      when(raceRepository.findAll()).thenReturn(testRaces);

      mockMvc.perform(get("/api/sync/races/list"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(3));

      verify(raceRepository).findAll();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/sync/races/list"))
          .andExpect(status().isUnauthorized());
    }
  }
}
