package de.derfilli.photography.inverso;

import java.awt.MouseInfo;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class InversoApplication extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(
        InversoApplication.class.getResource("inverso-startup.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 640, 480);

    InversoController inversoController = fxmlLoader.getController();
    inversoController.setStage(stage);

    stage.setTitle("Inversio - Raw Image Inversion");
    stage.setScene(scene);
    stage.show();

    centerOnActiveDisplay(stage);
    stage.setAlwaysOnTop(true);
  }

  private static void centerOnActiveDisplay(Stage stage) {
    var location = MouseInfo.getPointerInfo().getLocation();
    var posX = location.getX();
    var posY = location.getY();

    var target = Screen.getScreensForRectangle(posX, posY, 1, 1)
        .stream().findAny()
        .orElse(Screen.getPrimary());

    var visualBounds = target.getVisualBounds();
    if (Double.isNaN(stage.getWidth())) {
      Platform.runLater(() -> centerInBounds(stage, visualBounds));
    } else {
      centerInBounds(stage, visualBounds);
    }
  }

  private static void centerInBounds(Stage stage, Rectangle2D bounds) {
    Objects.requireNonNull(stage);
    Objects.requireNonNull(bounds);
    stage.setX(bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2);
    stage.setY(bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2);
  }
}
