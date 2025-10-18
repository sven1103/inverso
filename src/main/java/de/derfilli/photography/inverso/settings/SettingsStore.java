package de.derfilli.photography.inverso.settings;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
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
  private final FileSettingsRepository fileSettingsRepository;

  public SettingsStore(@NotNull FileSettingsRepository fileSettingsRepository) {
    this.fileSettingsRepository = Objects.requireNonNull(fileSettingsRepository);
  }

  public FileSetting getFileSettings(Path path) {
    return settingsByPath.computeIfAbsent(path, p -> {
      var settings = loadSnapshot(path)
          .map(this::fromSnapshot)
          .orElse(new FileSetting(path));
      settings.changes().subscribe(bus::tryEmitNext);
      return settings;
    });
  }

  public Flux<FileSettingEvent> changes() {
    return bus.asFlux();
  }

  private Optional<FileSettingsSnapshot> loadSnapshot(Path path) {
    return fileSettingsRepository.load(path);
  }

  private FileSetting fromSnapshot(@NotNull FileSettingsSnapshot snapshot) {
    var setting =  new FileSetting(snapshot.filePath());
    setting.setRotation(snapshot.rotation());
    setting.setBakedRotation(snapshot.bakedRotation());
    return setting;
  }
}
