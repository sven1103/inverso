package de.derfilli.photography.inverso.settings;

import java.util.Optional;
import java.util.prefs.Preferences;
import javafx.application.Platform;

/**
 * Enables user-based settings for the application, e.g. last visited directory, last worked on
 * file, or similar.
 *
 * @author Sven Fillinger
 * @since 1.0.0
 */
public final class InversoSettings {

  private final Preferences preferences = Preferences.userNodeForPackage(InversoSettings.class);

  private static final int MAX_KEY_LENGTH = Preferences.MAX_KEY_LENGTH;
  private static final int MAX_VALUE_LENGTH = Preferences.MAX_VALUE_LENGTH;

  /**
   * Adds a new preference with a given key and a provided value for the user.
   *
   * @param key   key with which the stored value is associated with
   * @param value value to be associated with the given key
   * @throws SettingsException in case one of the parameters is {@code null}, the key exceeds the
   *                           {@code MAX_KEY_LENGTH} or the value exceeds the
   *                           {@code MAX_VALUE_LENGTH}.
   * @since 1.0.0
   */
  public void add(String key, String value) throws SettingsException {
    Platform.runLater(() -> {
      try {
        preferences.put(key, value);
      } catch (IllegalArgumentException | NullPointerException | IllegalStateException e) {
        throw new SettingsException("Addition of settings failed.", e);
      }
    });
  }

  /**
   * Returns the value for a given key, if the key exists.
   *
   * @param key the key to look up the associated value
   * @return the value wrapped in an {@link Optional} if the key exists, otherwise returns
   * {@link Optional#empty()}
   * @since 1.0.0
   */
  public Optional<String> get(String key) {
    return Optional.ofNullable(preferences.get(key, null));
  }

  /**
   * Wrapper exception class to simplify public method signatures of the
   * {@code InversoSettings.class}.
   *
   * @since 1.0.0
   */
  public static class SettingsException extends RuntimeException {

    SettingsException(String message) {
      super(message);
    }

    SettingsException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
