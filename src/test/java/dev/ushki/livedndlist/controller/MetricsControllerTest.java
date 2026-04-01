package dev.ushki.livedndlist.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.ushki.livedndlist.security.jwt.JwtAuthenticationEntryPoint;
import dev.ushki.livedndlist.security.jwt.JwtAuthenticationFilter;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
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
    controllers = MetricsController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
class MetricsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SyncMetrics syncMetrics;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockitoBean
  private UserDetailsService userDetailsService;

  private SyncMetrics.MetricsSnapshot testSnapshot;

  @BeforeEach
  void setUp() {
    testSnapshot = SyncMetrics.MetricsSnapshot.builder()
        .totalRequests(100)
        .successfulRequests(95)
        .failedRequests(5)
        .averageResponseTime(150.5)
        .concurrentOperations(3)
        .build();
  }

  @Nested
  @DisplayName("GET /api/metrics/sync")
  class GetSyncMetricsTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return sync metrics successfully")
    void shouldReturnSyncMetricsSuccessfully() throws Exception {
      when(syncMetrics.getSnapshot()).thenReturn(testSnapshot);

      mockMvc.perform(get("/api/metrics/sync"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalRequests").value(100))
          .andExpect(jsonPath("$.successfulRequests").value(95))
          .andExpect(jsonPath("$.failedRequests").value(5))
          .andExpect(jsonPath("$.averageResponseTime").value(150.5))
          .andExpect(jsonPath("$.concurrentOperations").value(3));

      verify(syncMetrics).getSnapshot();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return sync metrics for admin user")
    void shouldReturnSyncMetricsForAdmin() throws Exception {
      when(syncMetrics.getSnapshot()).thenReturn(testSnapshot);

      mockMvc.perform(get("/api/metrics/sync"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalRequests").value(100));

      verify(syncMetrics).getSnapshot();
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return zero metrics when no syncs have occurred")
    void shouldReturnZeroMetricsWhenNoSyncs() throws Exception {
      SyncMetrics.MetricsSnapshot emptySnapshot = SyncMetrics.MetricsSnapshot.builder()
          .totalRequests(0)
          .successfulRequests(0)
          .failedRequests(0)
          .averageResponseTime(0.0)
          .concurrentOperations(0)
          .build();

      when(syncMetrics.getSnapshot()).thenReturn(emptySnapshot);

      mockMvc.perform(get("/api/metrics/sync"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalRequests").value(0))
          .andExpect(jsonPath("$.successfulRequests").value(0))
          .andExpect(jsonPath("$.failedRequests").value(0))
          .andExpect(jsonPath("$.averageResponseTime").value(0.0))
          .andExpect(jsonPath("$.concurrentOperations").value(0));

      verify(syncMetrics).getSnapshot();
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return metrics with high concurrent operations")
    void shouldReturnMetricsWithHighConcurrentOperations() throws Exception {
      SyncMetrics.MetricsSnapshot highConcurrencySnapshot = SyncMetrics.MetricsSnapshot.builder()
          .totalRequests(1000)
          .successfulRequests(950)
          .failedRequests(50)
          .averageResponseTime(75.25)
          .concurrentOperations(50)
          .build();

      when(syncMetrics.getSnapshot()).thenReturn(highConcurrencySnapshot);

      mockMvc.perform(get("/api/metrics/sync"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalRequests").value(1000))
          .andExpect(jsonPath("$.concurrentOperations").value(50));

      verify(syncMetrics).getSnapshot();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/metrics/sync"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("POST /api/metrics/sync/reset")
  class ResetSyncMetricsTests {

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should reset sync metrics successfully")
    void shouldResetSyncMetricsSuccessfully() throws Exception {
      doNothing().when(syncMetrics).reset();

      mockMvc.perform(post("/api/metrics/sync/reset")
              .with(csrf()))
          .andExpect(status().isNoContent());

      verify(syncMetrics).reset();
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return 403 when CSRF token is missing")
    void shouldReturn403WhenCsrfTokenMissing() throws Exception {
      mockMvc.perform(post("/api/metrics/sync/reset"))
          .andExpect(status().isForbidden());
    }
  }
}
