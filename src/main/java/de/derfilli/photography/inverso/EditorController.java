package de.derfilli.photography.inverso;


import de.derfilli.photography.inverso.raw.MetadataReader;
import java.io.File;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
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

  @FXML
  private ImageView imageView;

  @FXML
  private VBox vBox;

  private File file;

  private MetadataReader metadataReader;

  public EditorController(@NotNull File imageFile, @NotNull MetadataReader metadataReader) {
    this.file = Objects.requireNonNull(imageFile);
    this.metadataReader = Objects.requireNonNull(metadataReader);
  }

  @FXML
  private void initialize() {
    System.out.println("Initializing EditorController");
  }


}
