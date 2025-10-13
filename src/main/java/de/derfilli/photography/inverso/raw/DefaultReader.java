package de.derfilli.photography.inverso.raw;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.libraw.global.LibRaw;
import org.bytedeco.libraw.global.LibRaw.LibRaw_image_formats;
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
  public Mono<ThumbnailResult> loadThumbnail(@NotNull File file, double renderedWidth) {
    return Mono.fromCallable(() -> {
          try (libraw_data_t libRawData = LibRaw.libraw_init(0)) {
            int returnCode = LibRaw.libraw_open_file(libRawData, file.getAbsolutePath());
            if (returnCode != 0) {
              throw new MetadataReaderException("Error opening file: " + file.getAbsolutePath());
            }
            int orientation = libRawData.sizes().flip();
            returnCode = LibRaw.libraw_unpack_thumb(libRawData);
            if (returnCode != 0 || libRawData.thumbnail().tlength() <= 0) {
              throw new MetadataReaderException("Error unpacking thumbnail: " + file.getAbsolutePath());
            }
            long len = libRawData.thumbnail().tlength();
            BytePointer buf = libRawData.thumbnail().thumb(); // JPEG bytes
            byte[] bytes = new byte[(int) len];
            buf.get(bytes);

            return new ThumbnailResult(new Image(new ByteArrayInputStream(bytes), renderedWidth, 0, true, true),
                mapOrientation(orientation));
          }
        })
        .subscribeOn(Schedulers.boundedElastic());
  }

  private static double mapOrientation(int orientation) {
    return switch (orientation) {
      case 1 -> 0;
      case 3 -> 180;
      case 5 -> 270;
      case 6 -> 90;
      case 8 -> 270;
      default -> 0;
    };
  }


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
  public Mono<RawDataResult> loadRawPreview(Path file)
      throws SensorImageReaderException {
    return Mono.fromCallable(() -> {
          try (libraw_data_t rawData = LibRaw.libraw_init(0)) {
            int returnCode = LibRaw.libraw_open_file(rawData, file.toString());
            if (returnCode != 0) {
              throw new SensorImageReaderException("open raw file failed");
            }

            // fast settings
            var parameters = rawData.params();
            parameters.output_bps(8); // 8-bit per channel
            parameters.output_color(1); // SRGB
            parameters.use_camera_wb(1);
            parameters.half_size(1);

            int rotation = rawData.sizes().flip();

            returnCode = LibRaw.libraw_unpack(rawData);
            if (returnCode != 0) {
              throw new SensorImageReaderException("unpacking raw data from file failed");
            }

            returnCode = LibRaw.libraw_dcraw_process(rawData);
            if (returnCode != 0) {
              throw new SensorImageReaderException("processing raw data failed");
            }

            try (IntPointer error = new IntPointer(1)) {
              var img = LibRaw.libraw_dcraw_make_mem_image(rawData, error);
              int errorCode = error.get(0);
              if (img == null || errorCode != 0) {
                throw new SensorImageReaderException("creating in-memory image failed");
              }
              try {
                if (img.type().value != LibRaw_image_formats.LIBRAW_IMAGE_BITMAP.value ||
                    img.bits() != 8 || img.colors() != 3) {
                  throw new SensorImageReaderException("Unexpected format: type=" + img.type() +
                      " bits=" + img.bits() + " colors=" + img.colors());
                }
                int w = img.width();
                int h = img.height();
                int stride = w * 3;                 // RGB888
                int size = stride * h;

                byte[] rgb = new byte[size];
                img.data().get(rgb);                // copy from native

                WritableImage fx = new WritableImage(w, h);
                fx.getPixelWriter().setPixels(
                    0, 0, w, h,
                    PixelFormat.getByteRgbInstance(),
                    rgb, 0, stride
                );

                return new RawDataResult(fx, file, w, h, "RGB", mapOrientation(rotation));
              } finally {
                LibRaw.libraw_dcraw_clear_mem(img);
              }
            }
          }
        })
        .subscribeOn(Schedulers.boundedElastic());
  }
}

