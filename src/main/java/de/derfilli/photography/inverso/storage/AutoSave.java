package de.derfilli.photography.inverso.storage;

import de.derfilli.photography.inverso.settings.FileSettingsSnapshot;
import de.derfilli.photography.inverso.settings.FileSettingsStorage;
import de.derfilli.photography.inverso.settings.SettingsStore;
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
public class AutoSave implements FileSettingsStorage {

  public static final String INVERSO_JSON_FILENAME_EXTENSION = ".inverso.json";
  private final @NotNull SettingsStore settingsStore;

  private static final Logger log = LoggerFactory.getLogger(AutoSave.class);

  public AutoSave(@NotNull SettingsStore settingsStore) {
    this.settingsStore = Objects.requireNonNull(settingsStore);
    subscribeToEvents(this.settingsStore);
  }

  private void subscribeToEvents(@NotNull SettingsStore settingsStore) {
    settingsStore.changes()
        .sampleTimeout(event -> Mono.delay(Duration.ofMillis(300)))
        .subscribe(changes -> {
          var snapshot = changes.setting().snapshot();
          save(snapshot);
          log.info("(Autosave) Setting changes successfully for: " + snapshot.filePath());
        }, error -> {
          log.error("(Autosave) Error while saving changes", error);
        });
  }

  @Override
  public Path save(FileSettingsSnapshot snapshot) throws StorageException {
    var targetDirectory = snapshot.filePath().getParent();
    if (targetDirectory == null || !targetDirectory.toFile().exists()) {
      throw new StorageException(
          "Cannot save file settings since the target directory is not existing.");
    }
    if (!targetDirectory.toFile().canWrite()) {
      throw new StorageException(
          "Cannot save file settings since the target directory is not writable: "
              + targetDirectory);
    }

    var settingsFileName = buildStorageFilePath(targetDirectory,
        snapshot.filePath().getFileName().toString());
    var mapper = new JsonMapper();
    try (var writer = Files.newBufferedWriter(settingsFileName)) {
      mapper
          .writerWithDefaultPrettyPrinter()
          .writeValue(writer, snapshot);
    } catch (JacksonException | IOException e) {
      throw new StorageException("Autosave failed for: " + snapshot.filePath(), e);
    }

    return settingsFileName;
  }

  private static Path buildStorageFilePath(Path targetDir, String fileName) {
    return targetDir.resolve(fileName + INVERSO_JSON_FILENAME_EXTENSION);
  }

  @Override
  public Optional<FileSettingsSnapshot> load(Path file) throws StorageException {
    // TODO implement
    throw new RuntimeException("Not yet implemented");
  }

}
