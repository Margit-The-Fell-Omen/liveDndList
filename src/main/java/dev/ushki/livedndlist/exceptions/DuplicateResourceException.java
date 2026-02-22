package dev.ushki.livedndlist.exceptions;

/**
 * Exception thrown when attempting to create a resource that already exists. Typically used for
 * unique constraint violations.
 *
 * <p>Common scenarios:
 * <ul>
 *   <li>Registering with an existing username or email</li>
 *   <li>Creating a spell with a duplicate name</li>
 *   <li>Any operation violating database unique constraints</li>
 * </ul>
 *
 * <p>This is a runtime exception, so it doesn't require explicit handling.
 * It should be caught by global exception handlers to return appropriate
 * HTTP 409 Conflict responses to clients.
 *
 * @see dev.ushki.livedndlist.exceptions.GlobalExceptionHandler
 */
public class DuplicateResourceException extends RuntimeException {

  /**
   * Constructs a new DuplicateResourceException with the specified detail message.
   *
   * @param message the detail message explaining which resource is duplicated
   */
  public DuplicateResourceException(String message) {
    super(message);
  }
}
