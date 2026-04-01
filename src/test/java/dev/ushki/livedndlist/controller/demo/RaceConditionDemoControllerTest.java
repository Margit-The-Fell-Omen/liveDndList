package dev.ushki.livedndlist.controller.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.ushki.livedndlist.security.jwt.JwtAuthenticationEntryPoint;
import dev.ushki.livedndlist.security.jwt.JwtAuthenticationFilter;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
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
    controllers = RaceConditionDemoController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
class RaceConditionDemoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockitoBean
  private UserDetailsService userDetailsService;

  @Nested
  @DisplayName("GET /api/demo/race-condition/unsafe")
  class UnsafeCounterTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return unsafe counter result with default parameters")
    void shouldReturnUnsafeCounterResultWithDefaults() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/unsafe"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.counterType").value("Unsafe Counter"))
          .andExpect(jsonPath("$.threads").value(100))
          .andExpect(jsonPath("$.iterationsPerThread").value(1000))
          .andExpect(jsonPath("$.expectedCount").value(100000))
          .andExpect(jsonPath("$.actualCount").exists())
          .andExpect(jsonPath("$.lostUpdates").exists())
          .andExpect(jsonPath("$.executionTimeMs").exists())
          .andExpect(jsonPath("$.correct").exists())
          .andExpect(jsonPath("$.lostPercentage").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return unsafe counter result with custom parameters")
    void shouldReturnUnsafeCounterResultWithCustomParams() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/unsafe")
              .param("threads", "10")
              .param("iterations", "100"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.counterType").value("Unsafe Counter"))
          .andExpect(jsonPath("$.threads").value(10))
          .andExpect(jsonPath("$.iterationsPerThread").value(100))
          .andExpect(jsonPath("$.expectedCount").value(1000));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return result with small thread count")
    void shouldReturnResultWithSmallThreadCount() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/unsafe")
              .param("threads", "2")
              .param("iterations", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.threads").value(2))
          .andExpect(jsonPath("$.iterationsPerThread").value(10))
          .andExpect(jsonPath("$.expectedCount").value(20));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/unsafe"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/demo/race-condition/safe-synchronized")
  class SafeSynchronizedCounterTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return synchronized counter result with default parameters")
    void shouldReturnSynchronizedCounterResultWithDefaults() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-synchronized"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.counterType").value("Synchronized Counter"))
          .andExpect(jsonPath("$.threads").value(100))
          .andExpect(jsonPath("$.iterationsPerThread").value(1000))
          .andExpect(jsonPath("$.expectedCount").value(100000))
          .andExpect(jsonPath("$.actualCount").exists())
          .andExpect(jsonPath("$.lostUpdates").exists())
          .andExpect(jsonPath("$.executionTimeMs").exists())
          .andExpect(jsonPath("$.correct").exists())
          .andExpect(jsonPath("$.lostPercentage").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return synchronized counter result with custom parameters")
    void shouldReturnSynchronizedCounterResultWithCustomParams() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-synchronized")
              .param("threads", "10")
              .param("iterations", "100"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.counterType").value("Synchronized Counter"))
          .andExpect(jsonPath("$.threads").value(10))
          .andExpect(jsonPath("$.iterationsPerThread").value(100))
          .andExpect(jsonPath("$.expectedCount").value(1000));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return correct count for synchronized counter")
    void shouldReturnCorrectCountForSynchronizedCounter() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-synchronized")
              .param("threads", "5")
              .param("iterations", "50"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.expectedCount").value(250))
          .andExpect(jsonPath("$.actualCount").value(250))
          .andExpect(jsonPath("$.lostUpdates").value(0))
          .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-synchronized"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/demo/race-condition/safe-atomic")
  class SafeAtomicCounterTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return atomic counter result with default parameters")
    void shouldReturnAtomicCounterResultWithDefaults() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-atomic"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.counterType").value("Atomic Counter"))
          .andExpect(jsonPath("$.threads").value(100))
          .andExpect(jsonPath("$.iterationsPerThread").value(1000))
          .andExpect(jsonPath("$.expectedCount").value(100000))
          .andExpect(jsonPath("$.actualCount").exists())
          .andExpect(jsonPath("$.lostUpdates").exists())
          .andExpect(jsonPath("$.executionTimeMs").exists())
          .andExpect(jsonPath("$.correct").exists())
          .andExpect(jsonPath("$.lostPercentage").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return atomic counter result with custom parameters")
    void shouldReturnAtomicCounterResultWithCustomParams() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-atomic")
              .param("threads", "10")
              .param("iterations", "100"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.counterType").value("Atomic Counter"))
          .andExpect(jsonPath("$.threads").value(10))
          .andExpect(jsonPath("$.iterationsPerThread").value(100))
          .andExpect(jsonPath("$.expectedCount").value(1000));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return correct count for atomic counter")
    void shouldReturnCorrectCountForAtomicCounter() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-atomic")
              .param("threads", "5")
              .param("iterations", "50"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.expectedCount").value(250))
          .andExpect(jsonPath("$.actualCount").value(250))
          .andExpect(jsonPath("$.lostUpdates").value(0))
          .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-atomic"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/demo/race-condition/compare-all")
  class CompareAllTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return comparison result with default parameters")
    void shouldReturnComparisonResultWithDefaults() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/compare-all"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.unsafe").exists())
          .andExpect(jsonPath("$.unsafe.counterType").value("Unsafe Counter"))
          .andExpect(jsonPath("$.unsafe.threads").value(100))
          .andExpect(jsonPath("$.unsafe.iterationsPerThread").value(1000))
          .andExpect(jsonPath("$.synchronizedCounter").exists())
          .andExpect(jsonPath("$.synchronizedCounter.counterType").value("Synchronized Counter"))
          .andExpect(jsonPath("$.atomic").exists())
          .andExpect(jsonPath("$.atomic.counterType").value("Atomic Counter"))
          .andExpect(jsonPath("$.recommendation").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return comparison result with custom parameters")
    void shouldReturnComparisonResultWithCustomParams() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/compare-all")
              .param("threads", "10")
              .param("iterations", "100"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.unsafe.threads").value(10))
          .andExpect(jsonPath("$.unsafe.iterationsPerThread").value(100))
          .andExpect(jsonPath("$.unsafe.expectedCount").value(1000))
          .andExpect(jsonPath("$.synchronizedCounter.threads").value(10))
          .andExpect(jsonPath("$.synchronizedCounter.expectedCount").value(1000))
          .andExpect(jsonPath("$.atomic.threads").value(10))
          .andExpect(jsonPath("$.atomic.expectedCount").value(1000));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return correct counts for safe counters in comparison")
    void shouldReturnCorrectCountsForSafeCountersInComparison() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/compare-all")
              .param("threads", "5")
              .param("iterations", "50"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.synchronizedCounter.actualCount").value(250))
          .andExpect(jsonPath("$.synchronizedCounter.correct").value(true))
          .andExpect(jsonPath("$.atomic.actualCount").value(250))
          .andExpect(jsonPath("$.atomic.correct").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return recommendation in comparison result")
    void shouldReturnRecommendationInComparisonResult() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/compare-all")
              .param("threads", "5")
              .param("iterations", "50"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.recommendation").isString())
          .andExpect(jsonPath("$.recommendation").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return comparison result for admin user")
    void shouldReturnComparisonResultForAdmin() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/compare-all")
              .param("threads", "5")
              .param("iterations", "50"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.unsafe").exists())
          .andExpect(jsonPath("$.synchronizedCounter").exists())
          .andExpect(jsonPath("$.atomic").exists());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/compare-all"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("RaceConditionResult Tests")
  class RaceConditionResultTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should calculate correct percentage with no lost updates")
    void shouldCalculateCorrectPercentageWithNoLostUpdates() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-atomic")
              .param("threads", "5")
              .param("iterations", "20"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.lostUpdates").value(0))
          .andExpect(jsonPath("$.lostPercentage").value(0.0));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return execution time in milliseconds")
    void shouldReturnExecutionTimeInMilliseconds() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-atomic")
              .param("threads", "2")
              .param("iterations", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.executionTimeMs").isNumber());
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should handle single thread")
    void shouldHandleSingleThread() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/unsafe")
              .param("threads", "1")
              .param("iterations", "100"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.threads").value(1))
          .andExpect(jsonPath("$.expectedCount").value(100))
          .andExpect(jsonPath("$.actualCount").value(100))
          .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should handle single iteration")
    void shouldHandleSingleIteration() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-atomic")
              .param("threads", "10")
              .param("iterations", "1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.iterationsPerThread").value(1))
          .andExpect(jsonPath("$.expectedCount").value(10))
          .andExpect(jsonPath("$.actualCount").value(10));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should handle minimum values")
    void shouldHandleMinimumValues() throws Exception {
      mockMvc.perform(get("/api/demo/race-condition/safe-synchronized")
              .param("threads", "1")
              .param("iterations", "1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.expectedCount").value(1))
          .andExpect(jsonPath("$.actualCount").value(1))
          .andExpect(jsonPath("$.correct").value(true));
    }
  }
}
