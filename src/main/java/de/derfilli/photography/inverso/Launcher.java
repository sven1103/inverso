package de.derfilli.photography.inverso;

import javafx.application.Application;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.libraw.global.LibRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

  private static final Logger log = LoggerFactory.getLogger(Launcher.class);

  public static void main(String[] args) {
    if (log.isDebugEnabled()) {
      log.debug("LibRaw version: {}", libRawVersion());
    }
    Application.launch(InversoApplication.class, args);
  }

  private static String libRawVersion() {
    try (BytePointer bp = LibRaw.libraw_version()) {
      return bp.getString();
    }
  }
}
