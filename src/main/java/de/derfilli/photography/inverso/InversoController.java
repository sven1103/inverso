package de.derfilli.photography.inverso;

import de.derfilli.photography.inverso.settings.InversoSettings;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

public class InversoController {

  private Stage stage;

  private final InversoSettings settings = new InversoSettings();

  private static final String LAST_DIRECTORY = "directory.last";

  private static void initialDirectory(@NotNull FileChooser chooser, @NotNull Path directory)
      throws NullPointerException {
    Objects.requireNonNull(chooser);
    Objects.requireNonNull(directory);
    if (Files.exists(directory) && Files.isDirectory(directory)) {
      chooser.setInitialDirectory(directory.toFile());
    } else {
      throw new IllegalArgumentException(
          "Path " + directory + " does not exist or is not a directory");
    }
  }

  @FXML
  protected void openImage() {
    if (stage == null) {
      throw new IllegalArgumentException("Stage must not be null.");
    }

    var fileChooser = new FileChooser();
    fileChooser.setTitle("Open image file");
    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.PEF"));

    var lastDirectory = settings.get(LAST_DIRECTORY);
    lastDirectory.ifPresent(
        lastDirectoryPath -> initialDirectory(fileChooser, Paths.get(lastDirectoryPath)));

    var selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile != null) {
      var parentDirectory = selectedFile.getParent();
      settings.add(LAST_DIRECTORY,
          parentDirectory == null ? FileSystems.getDefault().getSeparator() : parentDirectory);
    }
  }

  private void saveAsLastDirectory(@NotNull Path lastDirectory) {
    if (Files.exists(lastDirectory) && Files.isDirectory(lastDirectory)) {
      settings.add(LAST_DIRECTORY, lastDirectory.toString());
    }
  }

  protected void setStage(@NotNull Stage stage) {
    this.stage = Objects.requireNonNull(stage);
  }


}
