package de.derfilli.photography.inverso.raw;

import java.nio.file.Path;
import javafx.scene.image.Image;

/**
 * <b><record short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public record RawDataResult(
    Image image,
    Path originalFile,
    int width,
    int height,
    String colorSpace) {
}
