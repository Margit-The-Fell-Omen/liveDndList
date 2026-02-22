package dev.ushki.livedndlist.exceptions;

/**
 * Exception thrown when a requested resource cannot be found. Used for HTTP 404 Not Found
 * scenarios.
 *
 * <p>This is a runtime exception, so it doesn't require explicit handling.
 * It should be caught by global exception handlers to return appropriate HTTP 404 responses to
 * clients.
 *
 * @see dev.ushki.livedndlist.exceptions.GlobalExceptionHandler
 */
public class ResourceNotFoundException extends RuntimeException {

  /**
   * Constructs a new ResourceNotFoundException with a custom message.
   *
   * @param message the detail message explaining what resource was not found
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs a new ResourceNotFoundException with a formatted message. Creates a standardized
   * error message in the format: "{Resource} not found with {field}: '{value}'"
   *
   * @param resource the type of resource (e.g., "User", "Character", "Spell")
   * @param field    the field used for lookup (e.g., "id", "username", "name")
   * @param value    the value that was searched for
   */
  public ResourceNotFoundException(String resource, String field, Object value) {
    super(String.format("%s not found with %s: '%s'", resource, field, value));
  }
}

