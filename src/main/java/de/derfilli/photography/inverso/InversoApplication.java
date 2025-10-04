package de.derfilli.photography.inverso;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class InversoApplication extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(
        InversoApplication.class.getResource("inverso-startup.fxml"));
    Scene scene = new Scene(fxmlLoader.load());

    InversoController inversoController = fxmlLoader.getController();
    inversoController.setStage(stage);

    stage.setTitle("Inverso - Raw Image Inversion");
    stage.setScene(scene);
    stage.show();

    // Centers the application window in the active display
    WindowUtil.centerOnActiveDisplay(stage);

    // Brings up the application window on the foreground on application start
    stage.setAlwaysOnTop(true);

    // The application window can be put to background again later, no need to
    // disturb the user's navigation flow
    Platform.runLater(() -> stage.setAlwaysOnTop(false));
  }
}
