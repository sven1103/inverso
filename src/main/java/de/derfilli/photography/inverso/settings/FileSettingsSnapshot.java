package de.derfilli.photography.inverso.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public record FileSettingsSnapshot(
    @JsonProperty(required = true, value = "version") String version,
    @JsonProperty(required = true, value = "filePath") Path filePath,
    @JsonProperty(required = true, value = "rotation") double rotation,
    @JsonProperty(required = true, value = "bakedRotation") double bakedRotation
    ) {

  private static final String VERSION_TAG = "v1";

  public static FileSettingsSnapshot create(Path filePath, double rotation, double rotationBaked) {
    return new FileSettingsSnapshot(VERSION_TAG, filePath, rotation, rotationBaked);
  }

}
