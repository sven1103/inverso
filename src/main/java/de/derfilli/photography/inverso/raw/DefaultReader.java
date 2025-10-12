package de.derfilli.photography.inverso.raw;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.libraw.global.LibRaw;
import org.bytedeco.libraw.libraw_data_t;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
class DefaultReader implements MetadataReader, SensorImageReader {


  @Override
  public Mono<ByteArrayInputStream> thumbnailFromRawFile(@NotNull File file)
      throws MetadataReaderException {
    return Mono.fromCallable(() -> {
          try (libraw_data_t libRawData = LibRaw.libraw_init(0)) {
            int returnCode = LibRaw.libraw_open_file(libRawData, file.getAbsolutePath());
            if (returnCode != 0) {
              throw new MetadataReaderException("Error opening file: " + file.getAbsolutePath());
            }

            returnCode = LibRaw.libraw_unpack_thumb(libRawData);
            if (returnCode != 0 || libRawData.thumbnail().tlength() <= 0) {
              throw new MetadataReaderException("Error unpacking thumbnail: " + file.getAbsolutePath());
            }
            long len = libRawData.thumbnail().tlength();
            BytePointer buf = libRawData.thumbnail().thumb(); // JPEG bytes
            byte[] bytes = new byte[(int) len];
            buf.get(bytes);

            return new ByteArrayInputStream(bytes);
          }
        })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<RawDataResult> loadRawData(Path file)
      throws SensorImageReaderException {
    return Mono.fromCallable(() -> {
          try (libraw_data_t libRawData = LibRaw.libraw_init(0)) {
            int returnCode = LibRaw.libraw_open_file(libRawData, file.toString());
            if (returnCode != 0) {
              throw new SensorImageReaderException("open raw file failed");
            }
            returnCode = LibRaw.libraw_unpack(libRawData);
            if (returnCode != 0) {
              throw new SensorImageReaderException("unpacking raw data from file filed");
            }

            var size = libRawData.sizes();
            int rawWidth = size.raw_width();
            int rawHeight = size.raw_height();
            int colors = libRawData.idata().colors();

          }
          return new RawDataResult(null, file, 0, 0, "RGB");
        })
        .subscribeOn(Schedulers.boundedElastic());
  }
}

