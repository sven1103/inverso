package de.derfilli.photography.inverso;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class ApplicationException extends RuntimeException {
  public ApplicationException(final String message) {
    super(message);
  }

  public ApplicationException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
