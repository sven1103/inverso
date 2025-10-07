package de.derfilli.photography.inverso.settings;

import java.nio.file.Path;
import java.util.Objects;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
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

  public Thumbnail(Image image, double fitWidth, Path originalImage) {

    setFillWidth(false);
    Objects.requireNonNull(image);
    Objects.requireNonNull(originalImage);

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

    setOnMouseClicked(e -> setSelected(!selected));
  }

  private void setSelected(boolean state) {
    this.selected = state;
    pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), state);
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

}
