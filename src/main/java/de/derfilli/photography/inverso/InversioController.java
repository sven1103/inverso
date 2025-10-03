package de.derfilli.photography.inverso;

import java.util.Objects;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class InversioController {

  private Stage stage;

  @FXML
  protected void openImage() {
    if (stage == null) {
      throw new IllegalArgumentException("Stage must not be null.");
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open image file");
    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files","*pef"));
    fileChooser.showOpenDialog(stage);
  }

  protected void setStage(Stage stage) {
    this.stage = Objects.requireNonNull(stage);
  }
}
