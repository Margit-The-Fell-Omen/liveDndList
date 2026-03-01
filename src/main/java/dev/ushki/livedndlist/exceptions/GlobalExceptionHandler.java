package dev.ushki.livedndlist.exceptions;

import dev.ushki.livedndlist.dto.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the application. Catches exceptions thrown by controllers and
 * converts them to appropriate HTTP responses.
 *
 * <p>This centralizes error handling and ensures consistent error response format
 * across all API endpoints using {@link ApiResponse}.
 *
 * <p>HTTP status codes used:
 * <ul>
 *   <li>400 BAD_REQUEST - Validation errors</li>
 *   <li>401 UNAUTHORIZED - Authentication failures</li>
 *   <li>403 FORBIDDEN - Authorization failures</li>
 *   <li>404 NOT_FOUND - Resource not found</li>
 *   <li>409 CONFLICT - Duplicate resources</li>
 *   <li>500 INTERNAL_SERVER_ERROR - Unexpected errors</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles ResourceNotFoundException. Returns HTTP 404 when a requested resource doesn't exist.
   *
   * @param ex the exception containing details about the missing resource
   * @return error response with 404 status
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException ex) {
    return ApiResponse.error(ex.getMessage());
  }

  /**
   * Handles DuplicateResourceException. Returns HTTP 409 when attempting to create a resource that
   * already exists.
   *
   * @param ex the exception containing details about the duplicate
   * @return error response with 409 status
   */
  @ExceptionHandler(DuplicateResourceException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<Void> handleDuplicate(DuplicateResourceException ex) {
    return ApiResponse.error(ex.getMessage());
  }

  /**
   * Handles UnauthorizedException. Returns HTTP 403 when a user lacks permission to access a
   * resource.
   *
   * @param ex the exception containing details about the authorization failure
   * @return error response with 403 status
   */
  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiResponse<Void> handleUnauthorized(UnauthorizedException ex) {
    return ApiResponse.error(ex.getMessage());
  }

  /**
   * Handles BadCredentialsException from Spring Security. Returns HTTP 401 when login credentials
   * are invalid.
   *
   * @return error response with 401 status and generic message
   */
  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponse<Void> handleBadCredentials() {
    return ApiResponse.error("Invalid username or password");
  }

  /**
   * Handles validation errors from {@code @Valid} annotations. Returns HTTP 400 with a map of field
   * names to error messages.
   *
   * <p>Example response data:
   * <pre>{@code
   * {
   *   "username": "Username must be between 3 and 50 characters",
   *   "email": "Invalid email format"
   * }
   * }</pre>
   *
   * @param ex the validation exception containing all field errors
   * @return error response with 400 status and field-level error details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Map<String, String>> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return ApiResponse.error("Validation failed", errors);
  }

  /**
   * Handles all uncaught exceptions. Returns HTTP 500 for unexpected errors that aren't handled by
   * specific handlers.
   *
   * <p>This is a catch-all to prevent unhandled exceptions from exposing
   * internal details to clients.
   *
   * @param ex the uncaught exception
   * @return error response with 500 status
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Void> handleGenericException(Exception ex) {
    return ApiResponse.error("An unexpected error occurred: " + ex.getMessage());
  }
}
