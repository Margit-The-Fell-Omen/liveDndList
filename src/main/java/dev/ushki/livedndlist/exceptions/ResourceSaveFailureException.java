package dev.ushki.livedndlist.exceptions;

/**
 * This exception is used only for failure demonstration of method without @Transactional.
 *
 */
public class ResourceSaveFailureException extends RuntimeException {

  /**
   * Constructs a new ResourceSaveFailureException with the specified detail message.
   *
   * @param message the detail message explaining which resource is not saved properly
   */
  public ResourceSaveFailureException(String message) {
    super(message);
  }
}
