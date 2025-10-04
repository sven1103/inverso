package de.derfilli.photography.inverso;


import de.derfilli.photography.inverso.raw.MetadataReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Objects;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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
public class EditorController {

  private static final Double THUMBNAIL_MAX_WIDTH = 220.0;

  @FXML
  private VBox editorWrapper;

  @FXML
  private SplitPane editorView;

  @FXML
  private StackPane thumbnailPane;

  @FXML
  private AnchorPane viewer;

  @FXML
  private ImageView thumbnailView;

  @FXML
  private AnchorPane controls;

  private byte[] thumbnailImage = new byte[0];

  private File file;

  private MetadataReader metadataReader;

  public EditorController(@NotNull File imageFile, @NotNull MetadataReader metadataReader) {
    this.file = Objects.requireNonNull(imageFile);
    this.metadataReader = Objects.requireNonNull(metadataReader);
  }

  @FXML
  private void initialize() {
    metadataReader.thumbnailFromRawFile(file).ifPresent(this::setThumbnail);

    thumbnailView.setPreserveRatio(true);
    thumbnailView.setSmooth(true);
    thumbnailView.setCache(true);
    thumbnailView.fitWidthProperty().bind(thumbnailPane.widthProperty());

    thumbnailPane.widthProperty().addListener(
        (observable, oldValue, newValue) -> Platform.runLater(this::maybeApplyThumbnail));

    SplitPane.setResizableWithParent(thumbnailPane, false); // user dragging won't expand it

  }

  private void setThumbnail(@NotNull ByteArrayInputStream stream) {
    thumbnailImage = stream.readAllBytes();
  }

  private void maybeApplyThumbnail() {
    if (thumbnailImage == null || thumbnailImage.length == 0) {
      return;
    }
    var paneWidth = thumbnailPane.getWidth();
    if (paneWidth <= 0) {
      return;
    }
    var paneHeight = thumbnailPane.getHeight();
    if (paneHeight <= 0) {
      return;
    }

    thumbnailView.setImage(
        new Image(
            new ByteArrayInputStream(thumbnailImage),
            Math.min(THUMBNAIL_MAX_WIDTH, paneWidth),
            Math.min(THUMBNAIL_MAX_WIDTH, paneHeight),
            true,
            true));
  }


}
