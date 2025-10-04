package de.derfilli.photography.inverso.raw;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * <b><interface short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public interface MetadataReader {

  Optional<ByteArrayInputStream> thumbnailFromRawFile(@NotNull File file)
      throws MetadataReaderException;

  class MetadataReaderFactory {

    private MetadataReaderFactory() {
    }

    public static MetadataReader getDefaultReader() {
      return new DefaultMetadataReader();
    }
  }

  class MetadataReaderException extends RuntimeException {

    MetadataReaderException(String message) {
      super(message);
    }

    MetadataReaderException(String message, Throwable cause) {
      super(message, cause);
    }
  }

}
