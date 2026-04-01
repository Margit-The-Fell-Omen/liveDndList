package dev.ushki.livedndlist.controller.sync;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.DndClass;
import dev.ushki.livedndlist.repository.DndClassRepository;
import dev.ushki.livedndlist.security.jwt.JwtAuthenticationEntryPoint;
import dev.ushki.livedndlist.security.jwt.JwtAuthenticationFilter;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import dev.ushki.livedndlist.service.Open5eClassService;
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
    controllers = ClassSyncController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
class ClassSyncControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private Open5eClassService classSyncService;

  @MockitoBean
  private DndClassRepository dndClassRepository;

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
  private List<DndClass> testClasses;

  @BeforeEach
  void setUp() {
    syncStatusInProgress = SyncStatusDto.builder()
        .inProgress(true)
        .currentOperation("Syncing classes")
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
        .errors(List.of("Error syncing Fighter", "Error syncing Wizard"))
        .build();

    DndClass fighter = DndClass.builder()
        .id(1L)
        .name("Fighter")
        .slug("fighter")
        .hitDice("1d10")
        .build();

    DndClass wizard = DndClass.builder()
        .id(2L)
        .name("Wizard")
        .slug("wizard")
        .hitDice("1d6")
        .build();

    DndClass rogue = DndClass.builder()
        .id(3L)
        .name("Rogue")
        .slug("rogue")
        .hitDice("1d8")
        .build();

    testClasses = List.of(fighter, wizard, rogue);
  }

  @Nested
  @DisplayName("GET /api/sync/classes/status")
  class GetSyncStatusTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return sync status when sync is in progress")
    void shouldReturnSyncStatusWhenInProgress() throws Exception {
      when(classSyncService.getSyncStatus()).thenReturn(syncStatusInProgress);

      mockMvc.perform(get("/api/sync/classes/status"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inProgress").value(true))
          .andExpect(jsonPath("$.currentOperation").value("Syncing classes"))
          .andExpect(jsonPath("$.processedCount").value(5))
          .andExpect(jsonPath("$.totalCount").value(10))
          .andExpect(jsonPath("$.progressPercent").value(50.0));

      verify(classSyncService).getSyncStatus();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return sync status when idle")
    void shouldReturnSyncStatusWhenIdle() throws Exception {
      when(classSyncService.getSyncStatus()).thenReturn(syncStatusIdle);

      mockMvc.perform(get("/api/sync/classes/status"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inProgress").value(false))
          .andExpect(jsonPath("$.processedCount").value(0))
          .andExpect(jsonPath("$.totalCount").value(0))
          .andExpect(jsonPath("$.progressPercent").value(0.0));

      verify(classSyncService).getSyncStatus();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/sync/classes/status"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("POST /api/sync/classes")
  class SyncAllClassesTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should sync all classes successfully")
    void shouldSyncAllClassesSuccessfully() throws Exception {
      when(classSyncService.syncAllClasses()).thenReturn(successfulSyncResult);

      mockMvc.perform(post("/api/sync/classes")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Sync completed successfully"))
          .andExpect(jsonPath("$.statistics.totalFetched").value(10))
          .andExpect(jsonPath("$.statistics.created").value(8))
          .andExpect(jsonPath("$.statistics.updated").value(2))
          .andExpect(jsonPath("$.statistics.failed").value(0))
          .andExpect(jsonPath("$.statistics.durationMs").value(5000))
          .andExpect(jsonPath("$.errors").isEmpty());

      verify(classSyncService).syncAllClasses();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return failed sync result")
    void shouldReturnFailedSyncResult() throws Exception {
      when(classSyncService.syncAllClasses()).thenReturn(failedSyncResult);

      mockMvc.perform(post("/api/sync/classes")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message").value("Sync failed"))
          .andExpect(jsonPath("$.statistics.totalFetched").value(10))
          .andExpect(jsonPath("$.statistics.created").value(2))
          .andExpect(jsonPath("$.statistics.updated").value(1))
          .andExpect(jsonPath("$.statistics.failed").value(7))
          .andExpect(jsonPath("$.errors.length()").value(2))
          .andExpect(jsonPath("$.errors[0]").value("Error syncing Fighter"))
          .andExpect(jsonPath("$.errors[1]").value("Error syncing Wizard"));

      verify(classSyncService).syncAllClasses();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 403 when CSRF token is missing")
    void shouldReturn403WhenCsrfTokenMissing() throws Exception {
      mockMvc.perform(post("/api/sync/classes"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("POST /api/sync/classes/async")
  class SyncAllClassesAsyncTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should start async sync when no sync in progress")
    void shouldStartAsyncSyncWhenNotInProgress() throws Exception {
      when(classSyncService.getSyncStatus()).thenReturn(syncStatusIdle);

      mockMvc.perform(post("/api/sync/classes/async")
              .with(csrf()))
          .andExpect(status().isAccepted())
          .andExpect(
              content().string("Sync started. Check status at: GET /api/sync/classes/status"));

      verify(classSyncService).getSyncStatus();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return bad request when sync already in progress")
    void shouldReturnBadRequestWhenSyncInProgress() throws Exception {
      when(classSyncService.getSyncStatus()).thenReturn(syncStatusInProgress);

      mockMvc.perform(post("/api/sync/classes/async")
              .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("Sync already in progress"));

      verify(classSyncService).getSyncStatus();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 403 when CSRF token is missing")
    void shouldReturn403WhenCsrfTokenMissing() throws Exception {
      mockMvc.perform(post("/api/sync/classes/async"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("POST /api/sync/classes/{slug}")
  class SyncClassBySlugTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should sync class by slug successfully")
    void shouldSyncClassBySlugSuccessfully() throws Exception {
      SyncResultDto singleClassResult = SyncResultDto.builder()
          .success(true)
          .message("Class 'fighter' synced successfully")
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

      when(classSyncService.syncBySlug("fighter")).thenReturn(singleClassResult);

      mockMvc.perform(post("/api/sync/classes/fighter")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Class 'fighter' synced successfully"))
          .andExpect(jsonPath("$.statistics.totalFetched").value(1))
          .andExpect(jsonPath("$.statistics.created").value(1));

      verify(classSyncService).syncBySlug("fighter");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return failed result when class not found")
    void shouldReturnFailedResultWhenClassNotFound() throws Exception {
      SyncResultDto notFoundResult = SyncResultDto.builder()
          .success(false)
          .message("Class 'nonexistent' not found")
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(0)
              .created(0)
              .updated(0)
              .failed(1)
              .durationMs(100L)
              .build())
          .errors(List.of("Class not found in Open5e API"))
          .build();

      when(classSyncService.syncBySlug("nonexistent")).thenReturn(notFoundResult);

      mockMvc.perform(post("/api/sync/classes/nonexistent")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message").value("Class 'nonexistent' not found"))
          .andExpect(jsonPath("$.statistics.failed").value(1));

      verify(classSyncService).syncBySlug("nonexistent");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 403 when CSRF token is missing")
    void shouldReturn403WhenCsrfTokenMissing() throws Exception {
      mockMvc.perform(post("/api/sync/classes/fighter"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("DELETE /api/sync/classes")
  class ClearAllClassesTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should clear all classes successfully")
    void shouldClearAllClassesSuccessfully() throws Exception {
      SyncResultDto clearResult = SyncResultDto.builder()
          .success(true)
          .message("All classes deleted successfully")
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

      when(classSyncService.clearAll()).thenReturn(clearResult);

      mockMvc.perform(delete("/api/sync/classes")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("All classes deleted successfully"));

      verify(classSyncService).clearAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return failed result when clear fails")
    void shouldReturnFailedResultWhenClearFails() throws Exception {
      SyncResultDto clearFailedResult = SyncResultDto.builder()
          .success(false)
          .message("Failed to delete classes: database error")
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

      when(classSyncService.clearAll()).thenReturn(clearFailedResult);

      mockMvc.perform(delete("/api/sync/classes")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message").value("Failed to delete classes: database error"))
          .andExpect(jsonPath("$.errors[0]").value("Database connection failed"));

      verify(classSyncService).clearAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 403 when CSRF token is missing")
    void shouldReturn403WhenCsrfTokenMissing() throws Exception {
      mockMvc.perform(delete("/api/sync/classes"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("GET /api/sync/classes/count")
  class GetClassCountTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return class count")
    void shouldReturnClassCount() throws Exception {
      when(dndClassRepository.count()).thenReturn(15L);

      mockMvc.perform(get("/api/sync/classes/count"))
          .andExpect(status().isOk())
          .andExpect(content().string("15"));

      verify(dndClassRepository).count();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return zero count when no classes exist")
    void shouldReturnZeroCountWhenNoClasses() throws Exception {
      when(dndClassRepository.count()).thenReturn(0L);

      mockMvc.perform(get("/api/sync/classes/count"))
          .andExpect(status().isOk())
          .andExpect(content().string("0"));

      verify(dndClassRepository).count();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/sync/classes/count"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/sync/classes/list")
  class GetAllClassesTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return list of all classes")
    void shouldReturnListOfAllClasses() throws Exception {
      when(dndClassRepository.findAll()).thenReturn(testClasses);

      mockMvc.perform(get("/api/sync/classes/list"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(3))
          .andExpect(jsonPath("$[0].id").value(1))
          .andExpect(jsonPath("$[0].name").value("Fighter"))
          .andExpect(jsonPath("$[0].slug").value("fighter"))
          .andExpect(jsonPath("$[0].hitDice").value("1d10"))
          .andExpect(jsonPath("$[1].id").value(2))
          .andExpect(jsonPath("$[1].name").value("Wizard"))
          .andExpect(jsonPath("$[1].slug").value("wizard"))
          .andExpect(jsonPath("$[1].hitDice").value("1d6"))
          .andExpect(jsonPath("$[2].id").value(3))
          .andExpect(jsonPath("$[2].name").value("Rogue"))
          .andExpect(jsonPath("$[2].slug").value("rogue"))
          .andExpect(jsonPath("$[2].hitDice").value("1d8"));

      verify(dndClassRepository).findAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return empty list when no classes exist")
    void shouldReturnEmptyListWhenNoClasses() throws Exception {
      when(dndClassRepository.findAll()).thenReturn(List.of());

      mockMvc.perform(get("/api/sync/classes/list"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(0));

      verify(dndClassRepository).findAll();
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return list for regular user")
    void shouldReturnListForRegularUser() throws Exception {
      when(dndClassRepository.findAll()).thenReturn(testClasses);

      mockMvc.perform(get("/api/sync/classes/list"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(3));

      verify(dndClassRepository).findAll();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/sync/classes/list"))
          .andExpect(status().isUnauthorized());
    }
  }
}
