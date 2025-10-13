package de.derfilli.photography.inverso.settings;

import java.nio.file.Path;
import java.util.Objects;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class Thumbnail extends VBox {

  private Rotation rotation;
  private ImageView imageView = new ImageView();

  private StackPane container = new StackPane();

  private boolean selected = false;

  private final Group rotator = new Group(imageView); // <-- rotation target

  private final Path originalImagePath;

  public static Thumbnail create(
      @NotNull Image image,
      double fitWidth,
      @NotNull Path originalImagePath) {
    return create(image, fitWidth, originalImagePath, Rotation.NORMAL);
  }

  public static Thumbnail create(
      @NotNull Image image,
      double fitWidth,
      @NotNull Path originalImagePath,
      @NotNull Thumbnail.Rotation rotation) {
    return new Thumbnail(image, fitWidth, originalImagePath, rotation);
  }

  private Thumbnail(Image image, double fitWidth, Path originalImagePath, Rotation rotation) {
    this(image, fitWidth, originalImagePath);
    setRotation(rotation);
  }

  private Thumbnail(Image image, double fitWidth, Path originalImage) {
    Objects.requireNonNull(image);
    originalImagePath = Objects.requireNonNull(originalImage);

    setFillWidth(true);
    getStyleClass().add("thumbnail");

    container.getChildren().add(rotator);
    container.setAlignment(Pos.CENTER);

    // Let TilePane compute height from this container:
    container.setMinHeight(Region.USE_PREF_SIZE);
    container.setMaxHeight(Region.USE_PREF_SIZE);

    // When rotation or fit width changes, the rotator's bounds change.
    // Reflect that into the container's preferred height so TilePane lays out correctly.
    rotator.boundsInParentProperty().addListener((obs, oldB, newB) -> {
      container.setPrefHeight(newB.getHeight());
      requestLayout(); // ask parent (TilePane) to relayout
    });

    // Also react when the image changes (first load) so we get an initial height:
    imageView.imageProperty().addListener((obs, o, n) -> {
      container.setPrefHeight(rotator.getBoundsInParent().getHeight());
      requestLayout();
    });


    getChildren().add(container);

    imageView.setImage(image);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    imageView.setCache(true);
    imageView.fitWidthProperty().bind(container.widthProperty()); // image follows tile width

    setAlignment(Pos.TOP_CENTER);
    setMinWidth(0);
    setMaxWidth(Double.MAX_VALUE);
    setPrefWidth(fitWidth > 0 ? fitWidth : Region.USE_COMPUTED_SIZE);

    setOnMouseClicked(e -> setSelected(!selected));

    addEventFilter(ThumbnailForSelectionEvent.THUMBNAIL_FOR_SELECTION, this::refreshOnSelectedEvent);
  }

  /** Call this instead of rotator.setRotate(...) so height is recomputed. */
  public void setRotation(@NotNull Rotation r) {
    this.rotation = r;
    rotator.setRotate(r.value());
    //rotator.setScaleX(1);
    //  rotator.setScaleY(1);
    // force recompute right away
    container.setPrefHeight(rotator.getBoundsInParent().getHeight());
    requestLayout();
  }

  private void refreshOnSelectedEvent(ThumbnailForSelectionEvent e) {
    Objects.requireNonNull(e);
    if (!e.getImagePath().equals(originalImagePath)) {
      setSelected(false);
    }
  }

  private void setSelected(boolean state) {
    this.selected = state;
    pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), selected);
    if (selected) {
      fireEvent(new ThumbnailSelectedEvent(this, this, originalImagePath));
    }
  }

  public static final class ThumbnailSelectedEvent extends Event {

    public static final EventType<ThumbnailSelectedEvent> THUMBNAIL_SELECTED = new EventType<>(
        Event.ANY, "THUMBNAIL_SELECTED");

    private final Path imagePath;

    public ThumbnailSelectedEvent(Object source, EventTarget target, Path imagePath) {
      super(source, target, THUMBNAIL_SELECTED);
      this.imagePath = Objects.requireNonNull(imagePath);
    }

    public Path getImagePath() {
      return imagePath;
    }
  }

  public static final class ThumbnailForSelectionEvent extends Event {

    public static final EventType<ThumbnailForSelectionEvent> THUMBNAIL_FOR_SELECTION = new EventType<>(
        Event.ANY, "THUMBNAIL_FOR_SELECTION");

    private final Path imagePath;

    public ThumbnailForSelectionEvent(Object source, EventTarget target, Path imagePath) {
      super(source, target, THUMBNAIL_FOR_SELECTION);
      this.imagePath = Objects.requireNonNull(imagePath);
    }

    public Path getImagePath() {
      return imagePath;
    }
  }

  public enum Rotation {

    NORMAL(0),
    ROTATE_90(90),
    ROTATE_180(180),
    ROTATE_270(270);

    private final double degrees;

    Rotation(double degrees) {
      this.degrees = degrees;
    }

    public double value() {
      return degrees;
    }

    public Rotation rotateLeft() {
      return switch (this) {
        case NORMAL -> ROTATE_90;
        case ROTATE_90 -> ROTATE_180;
        case ROTATE_180 -> ROTATE_270;
        case ROTATE_270 -> NORMAL;
      };
    }

    public Rotation rotateRight() {
      return switch (this) {
        case NORMAL -> ROTATE_270;
        case ROTATE_90 -> NORMAL;
        case ROTATE_180 -> ROTATE_90;
        case ROTATE_270 -> ROTATE_180;
      };
    }

    public static Rotation fromDegrees(double degrees) {
      return switch ((int) degrees) {
        case 90 -> ROTATE_90;
        case 180 -> ROTATE_180;
        case 270 -> ROTATE_270;
        default -> NORMAL;
      };
    }

  }
}
