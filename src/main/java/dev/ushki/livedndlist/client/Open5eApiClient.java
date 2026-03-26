package dev.ushki.livedndlist.client;

import dev.ushki.livedndlist.config.Open5eRateLimitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class Open5eApiClient {

  private final RestClient open5eRestClient;
  private final Open5eRateLimitConfig rateLimitConfig;

  private volatile long lastRequestTime = 0;

  public <T> T get(String uri, Class<T> responseType) {
    int attempt = 0;
    Exception lastException = null;

    while (attempt < rateLimitConfig.getMaxRetries()) {
      try {
        rateLimitDelay();
        log.debug("API request (attempt {}/{}): {}", attempt + 1, rateLimitConfig.getMaxRetries(),
            uri);

        T response = open5eRestClient.get()
            .uri(uri)
            .retrieve()
            .body(responseType);

        if (response != null) {
          return response;
        }

        log.warn("Null response from API: {}", uri);

      } catch (ResourceAccessException e) {
        lastException = e;
        attempt++;
        log.warn("API connection error (attempt {}/{}): {} - {}",
            attempt, rateLimitConfig.getMaxRetries(), uri, e.getMessage());

        if (attempt < rateLimitConfig.getMaxRetries()) {
          long backoffDelay = rateLimitConfig.getRetryDelayMs() * attempt * 2L;
          log.info("Waiting {}ms before retry...", backoffDelay);
          sleep(backoffDelay);
        }

      } catch (Exception e) {
        lastException = e;
        attempt++;
        log.warn("API request failed (attempt {}/{}): {} - {}",
            attempt, rateLimitConfig.getMaxRetries(), uri, e.getMessage());

        if (attempt < rateLimitConfig.getMaxRetries()) {
          sleep(rateLimitConfig.getRetryDelayMs() * attempt);
        }
      }
    }

    throw new Open5eApiException(
        "Request failed after " + rateLimitConfig.getMaxRetries() + " attempts: " + uri,
        lastException);
  }

  public String extractNextPath(String nextFullUrl) {
    if (nextFullUrl == null) {
      return null;
    }
    return nextFullUrl.replace("https://api.open5e.com/v1", "");
  }

  private void rateLimitDelay() {
    long now = System.currentTimeMillis();
    long timeSinceLastRequest = now - lastRequestTime;

    if (timeSinceLastRequest < rateLimitConfig.getDelayMs()) {
      long sleepTime = rateLimitConfig.getDelayMs() - timeSinceLastRequest;
      sleep(sleepTime);
    }

    lastRequestTime = System.currentTimeMillis();
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new Open5eApiException("Request interrupted", e);
    }
  }

  public static class Open5eApiException extends RuntimeException {

    public Open5eApiException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
