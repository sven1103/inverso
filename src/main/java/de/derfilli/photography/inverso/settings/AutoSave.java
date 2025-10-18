package de.derfilli.photography.inverso.settings;

import de.derfilli.photography.inverso.settings.FileSettingsRepository.StorageException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class AutoSave {


  private final @NotNull SettingsStore settingsStore;

  private static final Logger log = LoggerFactory.getLogger(AutoSave.class);
  private final @NotNull FileSettingsRepository fileSettingsRepository;

  public AutoSave(
      @NotNull SettingsStore settingsStore,
      @NotNull FileSettingsRepository fileSettingsRepository) {
    this.settingsStore = Objects.requireNonNull(settingsStore);
    this.fileSettingsRepository = Objects.requireNonNull(fileSettingsRepository);
    subscribeToEvents(this.settingsStore);
  }

  private void subscribeToEvents(@NotNull SettingsStore settingsStore) {
    settingsStore.changes()
        .sampleTimeout(event -> Mono.delay(Duration.ofMillis(300)))
        .subscribe(changes -> {
          var snapshot = changes.setting().snapshot();
          fileSettingsRepository.save(snapshot);
          log.info("(Autosave) Setting changes successfully for: " + snapshot.filePath());
        }, error -> {
          log.error("(Autosave) Error while saving changes", error);
        });
  }
}
