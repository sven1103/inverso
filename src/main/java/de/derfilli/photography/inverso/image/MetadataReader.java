package de.derfilli.photography.inverso.image;

import java.io.ByteArrayInputStream;
import java.io.File;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/**
 * <b><interface short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public interface MetadataReader {

  Mono<ThumbnailResult> loadThumbnail(@NotNull File file, double renderedWith);

  Mono<ByteArrayInputStream> thumbnailFromRawFile(@NotNull File file)
      throws MetadataReaderException;

  class MetadataReaderFactory {

    private MetadataReaderFactory() {
    }

    public static MetadataReader getDefaultReader() {
      return new DefaultReader();
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

  record ThumbnailResult(Image image, double rotation) {}

}
