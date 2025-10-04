package de.derfilli.photography.inverso.raw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Optional;
import javafx.scene.image.Image;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.libraw.global.LibRaw;
import org.bytedeco.libraw.libraw_data_t;
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
  public Optional<ByteArrayInputStream> thumbnailFromRawFile(@NotNull File file) throws MetadataReaderException {
    try (libraw_data_t libRawData = LibRaw.libraw_init(0)) {
      int returnCode = LibRaw.libraw_open_file(libRawData, file.getAbsolutePath());
      if (returnCode != 0) {
        return Optional.empty();
      }

      returnCode = LibRaw.libraw_unpack_thumb(libRawData);
      if (returnCode != 0 || libRawData.thumbnail().tlength() <= 0) {
        return Optional.empty();
      }
      long len = libRawData.thumbnail().tlength();
      BytePointer buf = libRawData.thumbnail().thumb(); // JPEG bytes
      byte[] bytes = new byte[(int) len];
      buf.get(bytes);

      return Optional.of(new ByteArrayInputStream(bytes));
    }
  }
}

