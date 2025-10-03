package de.derfilli.photography.inverso;

import java.io.IOException;
import javafx.application.Application;
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

    WindowUtil.centerOnActiveDisplay(stage);
    stage.setAlwaysOnTop(true);
  }
}
