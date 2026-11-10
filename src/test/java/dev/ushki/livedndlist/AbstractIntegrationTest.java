package dev.ushki.livedndlist;

import java.time.Duration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

  private static final PostgreSQLContainer<?> POSTGRES = startPostgres();

  private static final GenericContainer<?> REDIS = startRedis();

  private static PostgreSQLContainer<?> startPostgres() {
    PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:18-alpine")
        .withDatabaseName("livedndlist_test")
        .withUsername("test")
        .withPassword("test")
        .withStartupTimeout(Duration.ofMinutes(2));
    container.start();
    return container;
  }

  private static GenericContainer<?> startRedis() {
    GenericContainer<?> container = new GenericContainer<>("redis:8-alpine")
        .withExposedPorts(6379)
        .withStartupTimeout(Duration.ofMinutes(2))
        .waitingFor(Wait.forListeningPort());
    container.start();
    return container;
  }

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    registry.add("spring.data.redis.password", () -> "");
  }
}