package de.derfilli.photography.inverso;

import de.derfilli.photography.inverso.raw.MetadataReader;
import de.derfilli.photography.inverso.raw.SensorImageReader.SensorImageReaderFactory;
import de.derfilli.photography.inverso.settings.InversoSettings;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

public class InversoController {

  private Stage stage;

  private final InversoSettings settings = new InversoSettings();

  private static final String LAST_DIRECTORY = "directory.last";

  private static void setInitialDirectory(@NotNull FileChooser chooser, @NotNull Path directory)
      throws NullPointerException, IllegalArgumentException {
    Objects.requireNonNull(chooser);
    Objects.requireNonNull(directory);
    if (Files.exists(directory) && Files.isDirectory(directory)) {
      chooser.setInitialDirectory(directory.toFile());
    } else {
      throw new IllegalArgumentException(
          "path " + directory + " does not exist or is not a directory");
    }
  }

  @FXML
  protected void openFiles() {
    if (stage == null) {
      throw new IllegalArgumentException("stage must not be null.");
    }

    // 1. set up file chooser
    var fileChooser = new FileChooser();
    fileChooser.setTitle("Open image file");
    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Raw files", "*.PEF"));

    // 2. start file search in the last visited directory (if available)
    var lastDirectory = settings.get(LAST_DIRECTORY);
    lastDirectory.ifPresent(
        lastDirectoryPath -> setInitialDirectory(fileChooser, Paths.get(lastDirectoryPath)));

    // 3. wait for the user selection
    var selectedFile = fileChooser.showOpenMultipleDialog(stage);

    // 4. continue to load the editor
    if (selectedFile != null && !selectedFile.isEmpty()) {
      var parentDirectory = selectedFile.getFirst().getParent();
      // we store the selected file directory for the next file search (persistent)
      settings.add(LAST_DIRECTORY,
          parentDirectory == null ? FileSystems.getDefault().getSeparator() : parentDirectory);
      try {
        // the user will be presented with the editor stage
        loadEditorStage(selectedFile);
      } catch (IOException e) {
        throw new IllegalStateException("should not have happened", e);
      }
    }
  }

  protected void setStage(@NotNull Stage stage) {
    this.stage = Objects.requireNonNull(stage);
  }

  private void loadEditorStage(List<File> images) throws IOException {
    var reader = MetadataReader.MetadataReaderFactory.getDefaultReader();
    var sensorReader = SensorImageReaderFactory.getDefaultReader();
    FXMLLoader loader = new FXMLLoader(EditorController.class.getResource("inverso-editor.fxml"));
    loader.setControllerFactory(type -> {
      if (type == EditorController.class) {
        return new EditorController(images, reader, sensorReader);
      }
      try {
        return type.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new ApplicationException("cannot create " + type, e);
      }
    });

    Parent root = loader.load();

    var editorStage = new Stage();
    editorStage.initOwner(stage);
    editorStage.setTitle("Inverso — Editor");
    editorStage.setScene(new Scene(root, 1200, 800));
    editorStage.show();

    WindowUtil.centerOnActiveDisplay(editorStage);
  }

}
