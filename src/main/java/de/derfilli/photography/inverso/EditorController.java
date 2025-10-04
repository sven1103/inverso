package de.derfilli.photography.inverso;


import de.derfilli.photography.inverso.raw.MetadataReader;
import de.derfilli.photography.inverso.settings.Thumbnail;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class EditorController {

  private final Scheduler fxScheduler = Schedulers.fromExecutor(Platform::runLater);
  @FXML

  private static final double CELL_MIN = 120;
  private static final double CELL_MAX = 330;

  @FXML
  private VBox editorWrapper;

  @FXML
  private SplitPane editorView;

  @FXML
  private TilePane thumbnailPane;

  private byte[] thumbnailImage = new byte[0];

  @FXML
  private StackPane viewer;

  @FXML
  private ScrollPane viewerScroll;

  @FXML
  private StackPane viewerContent;

  @FXML
  private ImageView imageView;

  private byte[] mainImage = new byte[0];

  @FXML
  private AnchorPane controls;

  private ObservableList<File> files = FXCollections.observableList(new ArrayList<>());

  private MetadataReader metadataReader;

  private Comparator<File> comparator = Comparator.comparing(File::getName);

  public EditorController(@NotNull List<File> imageFiles, @NotNull MetadataReader metadataReader) {
    this.files.addAll(Objects.requireNonNull(imageFiles.stream().sorted(comparator).toList()));
    this.metadataReader = Objects.requireNonNull(metadataReader);
  }

  @FXML
  private void initialize() {
    viewerScroll.getStylesheets().add(
        Objects.requireNonNull(getClass().getResource("editor.css")).toExternalForm());

    // 1. load and set-up thumbnail
    metadataReader.thumbnailFromRawFile(files.getFirst()).ifPresent(this::setThumbnail);
    Flux.fromIterable(files)
        .distinct()
        .flatMapSequential(file ->
            Mono.just(metadataReader.thumbnailFromRawFile(file)
                .map(inputStream -> new Thumbnail(new Image(inputStream, 460, 0, true, true), 200,
                    0))
                .orElse(new Thumbnail(new Image(new ByteArrayInputStream(new byte[0])), 0, 0))))
        .publishOn(fxScheduler)
        .doOnNext(thumbnail -> {
          thumbnailPane.getChildren().add(thumbnail);
        })
        .doAfterTerminate(() -> Platform.runLater(() -> fitThumbnails(thumbnailPane.getWidth())))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();

    thumbnailPane.widthProperty().addListener((observable, oldValue, newValue) ->
             fitThumbnails(newValue.doubleValue()));
    thumbnailPane.setMinWidth(0);
    thumbnailPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
    thumbnailPane.setMaxWidth(Double.MAX_VALUE);

    SplitPane.setResizableWithParent(thumbnailPane, true); // user dragging won't expand it

    // 2. load and set-up main image
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    imageView.setCache(true);
    viewerScroll.setFitToHeight(false);
    viewerScroll.setFitToWidth(false);
    metadataReader.thumbnailFromRawFile(files.getFirst()).ifPresent(this::setImage);

    // center content
    viewerScroll.viewportBoundsProperty().addListener((obs, ov, vb) ->
        viewerContent.setMinSize(vb.getWidth(), vb.getHeight()));

    // recalc when viewport or image changes
    viewerScroll.viewportBoundsProperty().addListener((obs, ov, nv) -> applyFit());
    imageView.imageProperty().addListener((obs, ov, nv) -> applyFit());

    imageView.setImage(new Image(new ByteArrayInputStream(mainImage)));

  }

  private void applyFit() {
    var image = imageView.getImage();
    if (image == null) {
      return;
    }

    double imageWidth = image.getWidth();
    double imageHeight = image.getHeight();

    if (imageWidth <= 0 || imageHeight <= 0) {
      return;
    }

    double viewportWidth = viewerScroll.getViewportBounds().getWidth();
    double viewportHeight = viewerScroll.getViewportBounds().getHeight();
    if (viewportWidth <= 0 || viewportHeight <= 0) {
      return;
    }

    double scale = Math.min(viewportWidth / imageWidth, viewportHeight / imageHeight);
    scale = Math.min(scale, 1.0);

    imageView.setFitWidth(imageWidth * scale);
    imageView.setFitHeight(imageHeight * scale);
  }

  private void setImage(@NotNull ByteArrayInputStream inputStream) {
    mainImage = inputStream.readAllBytes();
  }

  private void setThumbnail(@NotNull ByteArrayInputStream stream) {
    thumbnailImage = stream.readAllBytes();
  }

  private void fitThumbnails(double paneWidth) {
    if (paneWidth <= 0) return;

    var padding = thumbnailPane.getPadding();
    double contentWidth = paneWidth - padding.getLeft() - padding.getRight();

    double cellW = Math.clamp(contentWidth, CELL_MIN, CELL_MAX);

    thumbnailPane.setVgap(10);
    thumbnailPane.setPrefColumns(1);
    thumbnailPane.setPrefTileWidth(cellW);
    thumbnailPane.setOrientation(Orientation.VERTICAL);
  }
}
