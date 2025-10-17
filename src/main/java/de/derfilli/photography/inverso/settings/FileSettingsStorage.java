package de.derfilli.photography.inverso.settings;

import java.nio.file.Path;
import java.util.Optional;

/**
 * <b><interface short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public interface FileSettingsStorage {

  Path save(FileSettingsSnapshot snapshot) throws StorageException;

  Optional<FileSettingsSnapshot> load(Path file) throws StorageException;

  class StorageException extends RuntimeException {
    public StorageException(String message) {
      super(message);
    }
    public StorageException(String message, Throwable cause) {
      super(message, cause);
    }
  }


}


