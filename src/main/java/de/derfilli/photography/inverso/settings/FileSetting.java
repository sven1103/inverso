package de.derfilli.photography.inverso.settings;

import java.nio.file.Path;
import java.util.Objects;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import reactor.core.publisher.Flux;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public final class FileSetting {

  private final Path filePath;

  private DoubleProperty rotation;

  private DoubleProperty bakedRotation;

  private boolean initialized = false;

  public FileSetting(Path file) {
    this.filePath = Objects.requireNonNull(file);
    //  Set the initial rotation to 0 degrees
    this.rotation = new SimpleDoubleProperty(0.0);
    this.bakedRotation = new SimpleDoubleProperty(0.0);
  }

  public Path filePath() {
    return filePath;
  }

  public DoubleProperty rotationProperty() {
    return rotation;
  }

  private void actionAndEnsureInit(Runnable action) {
    action.run();
    if (!initialized) {
      initialized = true;
    }
  }

  public void setRotationIfAbsent(double rotation) {
    if (!initialized) {
      actionAndEnsureInit(() -> this.rotation.set(rotation));
    }
  }

  public void setRotation(double rotation) {
    actionAndEnsureInit(() -> this.rotation.set(rotation));
  }

  public void setBakedRotation(double bakedRotation) {
    actionAndEnsureInit(() -> this.bakedRotation.set(bakedRotation));
  }

  public double getRotation() {
    return rotation.get();
  }

  public DoubleProperty bakedRotationProperty() {
    return bakedRotation;
  }

  public FileSettingsSnapshot snapshot() {
    return FileSettingsSnapshot.create(filePath, rotationProperty().get(),
        bakedRotationProperty().get());
  }

  public Flux<FileSettingEvent> changes() {
    return Flux.create(sink -> {
      rotation.addListener((v, o, n) -> {
        sink.next(new FileSettingEvent(filePath, this));
      });
      bakedRotation.addListener((v, o, n) -> {
        sink.next(new FileSettingEvent(filePath, this));
      });
    });
  }
}
