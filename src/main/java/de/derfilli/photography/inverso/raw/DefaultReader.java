package de.derfilli.photography.inverso.raw;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.libraw.global.LibRaw;
import org.bytedeco.libraw.libraw_data_t;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
class DefaultReader implements MetadataReader, SensorImageReader {


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

  @Override
  public Mono<RawDataResult> loadRawData(Path file)
      throws SensorImageReaderException {
    try (libraw_data_t libRawData = LibRaw.libraw_init(0)) {
      int returnCode = LibRaw.libraw_open_file(libRawData, file.toString());
      if (returnCode != 0) {
        return Mono.error(new SensorImageReaderException("open raw file failed"));
      }
      returnCode = LibRaw.libraw_unpack(libRawData);
      if (returnCode != 0) {
        return Mono.error(new SensorImageReaderException("unpacking raw data from file filed"));
      }

      var size = libRawData.sizes();
      int rawWidth = size.raw_width();
      int rawHeight = size.raw_height();
      int colors = libRawData.idata().colors();

    }
    return Mono.empty();
  }
}

