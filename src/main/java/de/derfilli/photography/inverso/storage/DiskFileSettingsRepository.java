package de.derfilli.photography.inverso.storage;

import de.derfilli.photography.inverso.settings.FileSettingsRepository;
import de.derfilli.photography.inverso.settings.FileSettingsSnapshot;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class DiskFileSettingsRepository implements FileSettingsRepository {

  public static final String INVERSO_JSON_FILENAME_EXTENSION = ".inverso.json";

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
    var expectedSettingsFile = buildStorageFilePath(file.getParent(), file.getFileName().toString());
    if (!Files.exists(expectedSettingsFile)) {
      return Optional.empty();
    }
    if (!Files.isReadable(expectedSettingsFile)) {
      throw new StorageException("Insufficient rights. Cannot read settings file: " + expectedSettingsFile);
    }
    var objectMapper = new ObjectMapper();
    try {
      var settings = objectMapper.readValue(expectedSettingsFile, FileSettingsSnapshot.class);

      return Optional.of(settings);
    } catch (JacksonException e) {
      throw new StorageException("Cannot read settings file: " + expectedSettingsFile, e);
    }
  }
}
