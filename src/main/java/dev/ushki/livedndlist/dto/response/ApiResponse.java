package dev.ushki.livedndlist.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper for all API responses. Provides a consistent response structure across all
 * endpoints.
 *
 * <p>Response structure:
 * <pre>{@code
 * {
 *   "success": true,
 *   "message": "Operation completed",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * }</pre>
 *
 * <p>Null fields are excluded from the JSON response.
 *
 * @param <T> the type of data contained in the response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private boolean success;
  private String message;
  private T data;

  @Builder.Default
  private LocalDateTime timestamp = LocalDateTime.now();

  /**
   * Creates a successful response with data only.
   *
   * @param data the response data
   * @param <T>  the type of data
   * @return a successful API response containing the data
   */
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Creates a successful response with a message and data.
   *
   * @param message the success message
   * @param data    the response data
   * @param <T>     the type of data
   * @return a successful API response containing the message and data
   */
  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Creates a successful response with a message only.
   *
   * @param message the success message
   * @param <T>     the type of data (typically Void)
   * @return a successful API response containing only the message
   */
  public static <T> ApiResponse<T> success(String message) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Creates an error response with a message.
   *
   * @param message the error message
   * @param <T>     the type of data (typically Void)
   * @return an error API response containing the error message
   */
  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Creates an error response with a message and additional data.
   *
   * @param message the error message
   * @param data    additional error details (e.g., validation errors)
   * @param <T>     the type of error data
   * @return an error API response containing the message and error details
   */
  public static <T> ApiResponse<T> error(String message, T data) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }
}

