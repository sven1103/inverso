package de.derfilli.photography.inverso.settings;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
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

  public Thumbnail(Image image, double fitWidth, double fitHeight) {
    getStyleClass().add("thumbnail");
    setAlignment(Pos.TOP_CENTER);
    imageView.setImage(image);
    imageView.setFitHeight(fitHeight);
    imageView.setFitWidth(fitWidth);

    setMinWidth(0);
    setMaxWidth(Double.MAX_VALUE);
    setPrefWidth(fitWidth > 0 ? fitWidth : Region.USE_COMPUTED_SIZE);

    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    imageView.setCache(true);
    imageView.fitWidthProperty().bind(widthProperty()); // image follows tile width

    if (fitHeight > 0) {
      imageView.setFitHeight(fitHeight);
    }

    getChildren().add(imageView);
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
