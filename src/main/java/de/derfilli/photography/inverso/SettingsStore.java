package de.derfilli.photography.inverso;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class SettingsStore {

  private final Map<Path, FileSetting> settingsByPath = new HashMap<>();

  public FileSetting getFileSettings(Path path) {
    return settingsByPath.computeIfAbsent(path, FileSetting::new);
  }
}
