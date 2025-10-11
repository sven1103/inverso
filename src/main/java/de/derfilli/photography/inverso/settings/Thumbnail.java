package de.derfilli.photography.inverso.settings;

import java.nio.file.Path;
import java.util.Objects;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class Thumbnail extends VBox {

  private ImageView imageView = new ImageView();

  private StackPane container = new StackPane();

  private boolean selected = false;

  private final Path originalImagePath;

  public Thumbnail(Image image, double fitWidth, Path originalImage) {
    Objects.requireNonNull(image);
    originalImagePath = Objects.requireNonNull(originalImage);

    setFillWidth(false);
    getStyleClass().add("thumbnail");

    container.getChildren().add(imageView);
    container.setAlignment(Pos.CENTER);
    getChildren().add(container);

    imageView.setImage(image);
    imageView.setFitWidth(fitWidth);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    imageView.setCache(true);
    imageView.fitWidthProperty().bind(container.widthProperty()); // image follows tile width

    setAlignment(Pos.TOP_CENTER);
    setMinWidth(0);
    setMaxWidth(Double.MAX_VALUE);
    setPrefWidth(fitWidth > 0 ? fitWidth : Region.USE_COMPUTED_SIZE);

    setOnMouseClicked(e -> {
      setSelected(!selected);
    });

    addEventFilter(ThumbnailForSelectionEvent.THUMBNAIL_FOR_SELECTION, e -> {
      refreshOnSelectedEvent(e);
    });
  }

  private void refreshOnSelectedEvent(ThumbnailForSelectionEvent e) {
    Objects.requireNonNull(e);
    if (!e.getImagePath().equals(originalImagePath)) {
      setSelected(false);
    }
  }

  public void select() {
    setSelected(true);
  }

  private void setSelected(boolean state) {
    this.selected = state;
    pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), selected);
    if (selected) {
      fireEvent(new ThumbnailSelectedEvent(this, this, originalImagePath));
    }
  }


  public void setImage(Image image) {
    imageView.setImage(image);
  }

  public ImageView getImageView() {
    return imageView;
  }

  public Image getImage() {
    return imageView.getImage();
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

}
