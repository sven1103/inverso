package de.derfilli.photography.inverso.raw;

import java.nio.file.Path;
import reactor.core.publisher.Mono;

/**
 * <b><interface short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public interface SensorImageReader {

  Mono<RawDataResult> loadRawData(Path file) throws SensorImageReaderException;

  class SensorImageReaderFactory {

    private SensorImageReaderFactory() {
    }

    public static SensorImageReader getDefaultReader() {
      return new DefaultReader();
    }
  }
  class SensorImageReaderException extends RuntimeException {
    public SensorImageReaderException(String message) {
      super(message);
    }
    public SensorImageReaderException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
