package dev.ushki.livedndlist.client;

import dev.ushki.livedndlist.config.Open5eRateLimitConfig;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
@RequiredArgsConstructor
public class Open5eApiClient {

  private final RestClient open5eRestClient;
  private final Open5eRateLimitConfig rateLimitConfig;

  private volatile long lastRequestTime = 0;

  public <T> T getByPath(String path, Class<T> responseType) {
    validatePath(path);
    return executeWithRetry(path, responseType);
  }

  public <T> T getBySlug(String resource, String slug, Class<T> responseType) {
    String uriTemplate = "/{resource}/{slug}/";
    return executeWithRetry(uriTemplate, responseType, resource, slug);
  }

  private <T> T executeWithRetry(String uriTemplate, Class<T> responseType,
      Object... uriVariables) {
    int attempt = 0;
    Exception lastException = null;

    while (attempt < rateLimitConfig.getMaxRetries()) {
      try {
        rateLimitDelay();

        URI finalUri = UriComponentsBuilder.fromPath(uriTemplate).build(uriVariables);
        log.debug("API request (attempt {}/{}): {}", attempt + 1, rateLimitConfig.getMaxRetries(),
            finalUri);

        T response = open5eRestClient.get()
            .uri(finalUri)
            .retrieve()
            .body(responseType);

        if (response != null) {
          return response;
        }
        log.warn("Null response from API for URI: {}", finalUri);

      } catch (ResourceAccessException e) {
        lastException = e;
        attempt++;
        log.warn("API connection error (attempt {}/{}): {} - {}",
            attempt, rateLimitConfig.getMaxRetries(), uriTemplate, e.getMessage());
        if (attempt < rateLimitConfig.getMaxRetries()) {
          long backoffDelay = rateLimitConfig.getRetryDelayMs() * attempt * 2L;
          log.info("Waiting {}ms before retry...", backoffDelay);
          sleep(backoffDelay);
        }
      } catch (Exception e) {
        lastException = e;
        attempt++;
        log.warn("API request failed (attempt {}/{}): {} - {}",
            attempt, rateLimitConfig.getMaxRetries(), uriTemplate, e.getMessage());
        if (attempt < rateLimitConfig.getMaxRetries()) {
          sleep(rateLimitConfig.getRetryDelayMs() * attempt);
        }
      }
    }

    throw new Open5eApiException(
        "Request failed after " + rateLimitConfig.getMaxRetries() + " attempts: " + uriTemplate,
        lastException);
  }

  public String extractNextPath(String nextFullUrl) {
    if (nextFullUrl == null) {
      return null;
    }
    try {
      URI uri = new URI(nextFullUrl);
      String path = uri.getPath();
      String query = uri.getQuery();
      if (query != null) {
        return path + "?" + query;
      }
      return path;
    } catch (Exception e) {
      log.error("Failed to parse next URL: {}", nextFullUrl, e);
      throw new Open5eApiException("Invalid next URL format: " + nextFullUrl, e);
    }
  }

  private void validatePath(String path) {
    if (path == null || !path.startsWith("/")) {
      throw new IllegalArgumentException("Invalid path: must start with '/'");
    }
    try {
      Path normalized = Path.of(path).normalize();
      if (normalized.startsWith("..")) {
        throw new IllegalArgumentException("Invalid path: potential path traversal attempt");
      }
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException("Invalid path characters detected", e);
    }
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
