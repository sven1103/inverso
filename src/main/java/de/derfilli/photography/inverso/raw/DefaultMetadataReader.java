package de.derfilli.photography.inverso.raw;

import java.io.File;
import java.util.Optional;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
class DefaultMetadataReader implements MetadataReader {

  @Override
  public Optional<Image> thumbnailFromRawFile(@NotNull File file) throws MetadataReaderException {
    return Optional.empty();
  }
}

