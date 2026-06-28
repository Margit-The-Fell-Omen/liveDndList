package dev.ushki.livedndlist.client;

import dev.ushki.livedndlist.config.Open5eRateLimitConfig;
import dev.ushki.livedndlist.dto.open5e.response.Open5ePaginatedResponse;
import dev.ushki.livedndlist.exceptions.Open5eApiException;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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
    return executeRawPathWithRetry(path, responseType);
  }

  public <T> T getBySlug(String resource, String slug, Class<T> responseType) {
    String uriTemplate = "/{resource}/{slug}/";
    return executeTemplateWithRetry(uriTemplate, responseType, resource, slug);
  }

  public <T> List<T> fetchAll(
      String initialPath,
      ParameterizedTypeReference<Open5ePaginatedResponse<T>> responseType) {

    List<T> allResults = new ArrayList<>();
    String currentPath = initialPath;
    int pageCount = 0;

    while (currentPath != null) {
      pageCount++;
      log.info("Fetching page {} from {}", pageCount, currentPath);

      Open5ePaginatedResponse<T> response = executeRawPathWithRetry(currentPath, responseType);

      if (response == null || response.getResults() == null) {
        break;
      }

      allResults.addAll(response.getResults());
      currentPath = extractNextPath(response.getNext());
    }

    log.info("Fetched {} total items across {} pages from {}", allResults.size(), pageCount,
        initialPath);
    return allResults;
  }

  private <T> T executeTemplateWithRetry(
      String uriTemplate,
      Class<T> responseType,
      Object... uriVariables) {

    int attempt = 0;
    Exception lastException = null;

    while (attempt < rateLimitConfig.getMaxRetries()) {
      try {
        rateLimitDelay();

        URI finalUri = UriComponentsBuilder.fromPath(uriTemplate)
            .buildAndExpand(uriVariables)
            .toUri();

        log.info("Requesting Open5e template URI: {}", finalUri);

        return open5eRestClient.get()
            .uri(finalUri)
            .retrieve()
            .body(responseType);

      } catch (HttpClientErrorException.NotFound e) {
        log.warn("Resource not found (404): {}", uriTemplate);
        throw e;
      } catch (HttpClientErrorException e) {
        log.error("Client error ({}): {}", e.getStatusCode(), e.getMessage());
        throw new Open5eApiException("Client error during API request", e);
      } catch (Exception e) {
        lastException = e;
        attempt++;
        log.warn("Transient API error (attempt {}/{}): {}",
            attempt, rateLimitConfig.getMaxRetries(), e.getMessage());

        if (attempt < rateLimitConfig.getMaxRetries()) {
          sleep(rateLimitConfig.getRetryDelayMs() * attempt);
        }
      }
    }

    throw new Open5eApiException("Request failed after retries", lastException);
  }

  private <T> T executeRawPathWithRetry(String path, Class<T> responseType) {
    int attempt = 0;
    Exception lastException = null;

    while (attempt < rateLimitConfig.getMaxRetries()) {
      try {
        validatePath(path);
        rateLimitDelay();

        URI finalUri = URI.create(path); // IMPORTANT

        log.info("Requesting Open5e raw URI: {}", finalUri);

        return open5eRestClient.get()
            .uri(finalUri)
            .retrieve()
            .body(responseType);

      } catch (HttpClientErrorException.NotFound e) {
        log.warn("Resource not found (404): {}", path);
        throw e;
      } catch (HttpClientErrorException e) {
        log.error("Client error ({}): {}", e.getStatusCode(), e.getMessage());
        throw new Open5eApiException("Client error during API request", e);
      } catch (Exception e) {
        lastException = e;
        attempt++;
        log.warn("Transient API error (attempt {}/{}): {}",
            attempt, rateLimitConfig.getMaxRetries(), e.getMessage());

        if (attempt < rateLimitConfig.getMaxRetries()) {
          sleep(rateLimitConfig.getRetryDelayMs() * attempt);
        }
      }
    }

    throw new Open5eApiException("Request failed after retries", lastException);
  }

  private <T> T executeRawPathWithRetry(String path, ParameterizedTypeReference<T> responseType) {
    int attempt = 0;
    Exception lastException = null;

    while (attempt < rateLimitConfig.getMaxRetries()) {
      try {
        validatePath(path);
        rateLimitDelay();

        URI finalUri = URI.create(path); // IMPORTANT

        log.info("Requesting Open5e raw URI: {}", finalUri);

        return open5eRestClient.get()
            .uri(finalUri)
            .retrieve()
            .body(responseType);

      } catch (HttpClientErrorException.NotFound e) {
        log.warn("Resource not found (404): {}", path);
        throw e;
      } catch (HttpClientErrorException e) {
        log.error("Client error ({}): {}", e.getStatusCode(), e.getMessage());
        throw new Open5eApiException("Client error during API request", e);
      } catch (Exception e) {
        lastException = e;
        attempt++;
        log.warn("Transient API error (attempt {}/{}): {}",
            attempt, rateLimitConfig.getMaxRetries(), e.getMessage());

        if (attempt < rateLimitConfig.getMaxRetries()) {
          sleep(rateLimitConfig.getRetryDelayMs() * attempt);
        }
      }
    }

    throw new Open5eApiException("Request failed after retries", lastException);
  }

  public String extractNextPath(String nextFullUrl) {
    if (nextFullUrl == null) {
      return null;
    }
    try {
      URI uri = new URI(nextFullUrl);
      String path = uri.getPath();
      String query = uri.getQuery();
      return query != null ? path + "?" + query : path;
    } catch (Exception e) {
      log.error("Failed to parse next URL: {}", nextFullUrl, e);
      throw new Open5eApiException("Invalid next URL format: " + nextFullUrl, e);
    }
  }

  private void validatePath(String path) {
    if (path == null || !path.startsWith("/")) {
      throw new IllegalArgumentException("Invalid path: must start with '/'");
    }

    String pathOnly = path.contains("?") ? path.substring(0, path.indexOf('?')) : path;

    try {
      Path normalized = Path.of(pathOnly).normalize();
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
}
