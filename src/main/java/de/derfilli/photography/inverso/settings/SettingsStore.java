package de.derfilli.photography.inverso.settings;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class SettingsStore {

  private final Map<Path, FileSetting> settingsByPath = new ConcurrentHashMap<>();

  private final Sinks.Many<FileSettingEvent> bus = Sinks.many().multicast().directBestEffort();

  public FileSetting getFileSettings(Path path) {
    return settingsByPath.computeIfAbsent(path, p ->  {
      var settings = new FileSetting(path);
      settings.changes().subscribe(bus::tryEmitNext);
      return settings;
    });
  }

  public Flux<FileSettingEvent> changes() {
    return bus.asFlux();
  }
}
