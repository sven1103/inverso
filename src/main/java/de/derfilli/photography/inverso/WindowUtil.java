package de.derfilli.photography.inverso;

import java.awt.MouseInfo;
import java.util.Objects;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class WindowUtil {

  private WindowUtil() {}

  public static void centerOnActiveDisplay(@NotNull Stage stage) {
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

  private static void centerInBounds(@NotNull Stage stage, @NotNull Rectangle2D bounds) {
    Objects.requireNonNull(stage);
    Objects.requireNonNull(bounds);
    stage.setX(bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2);
    stage.setY(bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2);
  }

}
