package de.derfilli.photography.inverso;


import de.derfilli.photography.inverso.behaviour.Memento;
import de.derfilli.photography.inverso.behaviour.Originator;
import de.derfilli.photography.inverso.raw.MetadataReader;
import de.derfilli.photography.inverso.settings.Thumbnail;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
public class EditorController implements Originator {

  private final Scheduler fxScheduler = Schedulers.fromExecutor(Platform::runLater);

  private static final double CELL_MIN = 120;
  private static final double CELL_MAX = 330;

  private final Deque<Memento> undoStack = new ArrayDeque<>();
  private final Deque<Memento> redoStack = new ArrayDeque<>();

  @FXML
  private VBox editorWrapper;

  @FXML
  private SplitPane editorView;

  @FXML
  private TilePane thumbnailPane;

  @FXML
  private StackPane viewer;

  @FXML
  private ScrollPane viewerScroll;

  @FXML
  private StackPane viewerContent;

  @FXML
  private ImageView imageView;

  @FXML
  private VBox controls;

  @FXML
  private Button btnRotateLeft;

  private byte[] thumbnailImage = new byte[0];

  private byte[] mainImage = new byte[0];

  private File currentFile;

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
    editorWrapper.getStylesheets()
        .add(Objects.requireNonNull(getClass().getResource("editor.css")).toExternalForm());

    // 1. load and set-up thumbnail
    getFileAndSetCurrent(0)
        .flatMap(file -> metadataReader.thumbnailFromRawFile(file))
        .ifPresent(this::setThumbnail);

    Platform.runLater(() -> {
      var paneWidth = thumbnailPane.getWidth() - thumbnailPane.getPadding().getLeft()
          - thumbnailPane.getPadding().getRight();
      thumbnailPane.setMinWidth(0);
      thumbnailPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
      thumbnailPane.setMaxWidth(Double.MAX_VALUE);

      populateThumbnails(paneWidth);
    });

    thumbnailPane.widthProperty().addListener((observable, oldValue, newValue) ->
        fitThumbnails(newValue.doubleValue()));

    SplitPane.setResizableWithParent(thumbnailPane, true); // user dragging won't expand it

    // 2. load and set-up main image
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    imageView.setCache(true);
    viewerScroll.setFitToHeight(false);
    viewerScroll.setFitToWidth(false);
    Optional.ofNullable(currentFile)
        .flatMap(file -> metadataReader.thumbnailFromRawFile(file))
        .ifPresent(this::setImage);

    // center content
    viewerScroll.viewportBoundsProperty()
        .addListener((obs, ov, vb) ->
            viewerContent.setMinSize(vb.getWidth(), vb.getHeight()));

    // recalc when viewport or image changes
    viewerScroll.viewportBoundsProperty()
        .addListener((obs, ov, nv) -> applyFit());
    imageView.imageProperty()
        .addListener((obs, ov, nv) -> applyFit());

    imageView.setImage(new Image(new ByteArrayInputStream(mainImage)));

    editorWrapper.sceneProperty().addListener((obs, oldScene, scene) -> {
      if (scene != null) {
        registerAccelerators(scene);
      }
    });
  }

  private void populateThumbnails(double width) {
    Flux.fromIterable(files)
        .distinct()
        .flatMapSequential(file ->
            Mono.just(metadataReader.thumbnailFromRawFile(file)
                .map(inputStream -> new Thumbnail(new Image(inputStream, width, 0, true, true),
                    width, file.toPath()))
                .orElse(new Thumbnail(new Image(new ByteArrayInputStream(new byte[0])), width,
                    file.toPath()))))
        .publishOn(fxScheduler)
        .doOnNext(thumbnail -> {
          thumbnailPane.getChildren().add(thumbnail);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
  }

  private void registerAccelerators(Scene scene) {
    scene.getAccelerators()
        .put(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN), this::doUndo);
    scene.getAccelerators()
        .put(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN), this::doRedo);
    scene.getAccelerators()
        .put(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN,
            KeyCombination.SHIFT_DOWN), this::doRedo);
  }

  private Optional<File> getFileAndSetCurrent(int index) {
    try {
      File file = files.get(index);
      currentFile = file;
      return Optional.of(file);
    } catch (IndexOutOfBoundsException e) {
      return Optional.empty();
    }
  }

  /**
   * Fits the main image to the visual part of the editor by scaling it <b>down</b>.
   * <p>
   * Maximum scale factor of the original image width is {@code 1}, so the image will not grow more
   * than its physical size.
   *
   * @since 1.0.0
   */
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
    if (paneWidth <= 0) {
      return;
    }

    var padding = thumbnailPane.getPadding();
    double contentWidth = paneWidth - padding.getLeft() - padding.getRight();

    double cellW = Math.clamp(contentWidth, CELL_MIN, CELL_MAX);

    // FIXME this should be configured in the FXML only to have one source of truth
    thumbnailPane.setVgap(10);
    thumbnailPane.setPrefColumns(1);
    thumbnailPane.setPrefTileWidth(cellW);
    thumbnailPane.setOrientation(Orientation.VERTICAL);
  }

  @FXML
  private void rotateLeft() {
    undoStack.push(createMemento());
    redoStack.clear();
    rotateMainLeft();
  }

  private void doUndo() {
    if (undoStack.isEmpty()) {
      return;
    }
    var snapshot = undoStack.pop();
    // enables the user to redo actions to the current state
    redoStack.push(createMemento());
    restoreMemento(snapshot);
  }

  private void doRedo() {
    if (redoStack.isEmpty()) {
      return;
    }
    var snapshot = redoStack.pop();
    // enables the user to undo actions back to the current state
    undoStack.push(createMemento());
    restoreMemento(snapshot);
  }

  private void rotateMainLeft() {
    imageView.setRotate(imageView.getRotate() - 90.0);
    applyFit();
  }

  @Override
  public Memento createMemento() {
    return new EditorMemento(imageView.getRotate());
  }

  @Override
  public void restoreMemento(Memento memento) {
    if (memento == null) {
      return;
    }
    if (memento instanceof EditorMemento editorMemento) {
      imageView.setRotate(editorMemento.imageRotation());
    }
  }

  private record EditorMemento(double imageRotation, Instant snapshotTime) implements Memento {

    EditorMemento {
      Objects.requireNonNull(snapshotTime);
    }

    EditorMemento(double imageRotation) {
      this(imageRotation, Instant.now());
    }

    @Override
    public String originatorName() {
      return "editor";
    }

  }

}
