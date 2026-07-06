package dev.ushki.livedndlist.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.config.Open5eRateLimitConfig;
import dev.ushki.livedndlist.dto.open5e.response.Open5ePaginatedResponse;
import dev.ushki.livedndlist.exceptions.Open5eApiException;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class Open5eApiClient {

  private final RestClient open5eRestClient;
  private final Open5eRateLimitConfig rateLimitConfig;

  private volatile long lastRequestTime = 0;

  public <T> T getByPath(String path, Class<T> responseType) {
    validatePath(path);
    return executeRawPathWithRetry(path, spec -> spec.body(responseType));
  }

  public <T> T getBySlug(String resource, String slug, Class<T> responseType) {
    String uriTemplate = "/{resource}/{slug}/";
    return executeTemplateWithRetry(uriTemplate, spec -> spec.body(responseType), resource, slug);
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

      Open5ePaginatedResponse<T> response = executeRawPathWithRetry(
          currentPath, spec -> spec.body(responseType));

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

  private long parseRetryAfter(String responseBody) {
    try {
      // Quick parse without full deserialization
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(responseBody);
      JsonNode retryAfter = root.get("retry_after");
      if (retryAfter != null && retryAfter.isNumber()) {
        return Math.min(retryAfter.asLong(), 300); // cap at 5 minutes
      }
    } catch (Exception ignored) {
    }
    return 60; // safe default
  }

  private <T> T executeTemplateWithRetry(
      String uriTemplate,
      Function<RestClient.ResponseSpec, T> bodyExtractor,
      Object... uriVariables) {

    return executeWithRetry(uriTemplate, () -> {
      log.info("Requesting Open5e template URI: {}", uriTemplate);

      RestClient.ResponseSpec responseSpec = open5eRestClient.get()
          .uri(uriTemplate, uriVariables) // ← RestClient resolves template against base URL
          .retrieve();

      return bodyExtractor.apply(responseSpec);
    });
  }

  private <T> T executeRawPathWithRetry(
      String path,
      Function<RestClient.ResponseSpec, T> bodyExtractor) {

    validatePath(path);

    return executeWithRetry(path, () -> {
      log.info("Requesting Open5e raw URI: {}", path);

      RestClient.ResponseSpec responseSpec = open5eRestClient.get()
          .uri(uriBuilder -> {
            if (path.startsWith("http://") || path.startsWith("https://")) {
              return URI.create(path);
            }
            return uriBuilder.replacePath(path).build();
          })
          .retrieve();

      return bodyExtractor.apply(responseSpec);
    });
  }

  private <T> T executeWithRetry(String pathDescription, ApiCall<T> apiCall) {
    int attempt = 0;
    Exception lastException = null;

    while (attempt < rateLimitConfig.getMaxRetries()) {
      try {
        rateLimitDelay();
        return apiCall.execute();

      } catch (HttpClientErrorException.NotFound e) {
        log.warn("Resource not found (404): {}", pathDescription);
        throw e;
      } catch (HttpClientErrorException e) {
        log.error("Client error ({}): {}", e.getStatusCode(), e.getMessage());
        throw new Open5eApiException("Client error during API request", e);
      } catch (HttpServerErrorException e) {
        if (e.getStatusCode().value() == 504 || e.getStatusCode().value() == 429) {
          long retryAfterSeconds = parseRetryAfter(e.getResponseBodyAsString());
          log.warn("Server overloaded ({}), backing off for {}s as requested",
              e.getStatusCode(), retryAfterSeconds);
          sleep(retryAfterSeconds * 1000);
        }
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
      return new URI(nextFullUrl).toString();
    } catch (Exception e) {
      log.error("Failed to parse next URL: {}", nextFullUrl, e);
      throw new Open5eApiException("Invalid next URL format: " + nextFullUrl, e);
    }
  }

  private void validatePath(String path) {
    if (path == null || path.isBlank()) {
      throw new Open5eApiException("API path must not be blank", null);
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

  @FunctionalInterface
  private interface ApiCall<T> {

    T execute() throws Exception;
  }
}
