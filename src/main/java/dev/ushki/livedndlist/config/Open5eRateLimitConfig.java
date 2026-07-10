package dev.ushki.livedndlist.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "open5e.rate-limit")
@Getter
@Setter
public class Open5eRateLimitConfig {

  private long delayMs = 200;
  private int maxRetries = 3;
  private long retryDelayMs = 120000;
}
