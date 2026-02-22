package dev.ushki.livedndlist.exceptions;

/**
 * Exception thrown when a user attempts to access a resource they don't have permission for. Used
 * for HTTP 403 Forbidden scenarios.
 *
 * <p>This is a runtime exception, so it doesn't require explicit handling.
 * It should be caught by global exception handlers to return appropriate HTTP 403 responses to
 * clients.
 *
 * @see dev.ushki.livedndlist.exceptions.GlobalExceptionHandler
 */
public class UnauthorizedException extends RuntimeException {

  /**
   * Constructs a new UnauthorizedException with the specified detail message.
   *
   * @param message the detail message explaining why access was denied
   */
  public UnauthorizedException(String message) {
    super(message);
  }
}
