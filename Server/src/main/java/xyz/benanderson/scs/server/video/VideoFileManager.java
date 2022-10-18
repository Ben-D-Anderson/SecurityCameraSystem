package xyz.benanderson.scs.server.video;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class VideoFileManager {

    private final Path saveDirectory;
    @Getter
    private final Duration videoDuration;
    private Path currentSaveFile;
    private LocalDateTime currentSaveFileExpiration;

    public VideoFileManager(Path saveDirectory, Duration videoDuration) {
        this.saveDirectory = saveDirectory;
        this.videoDuration = videoDuration;
        nextSaveFile();
    }

    private void nextSaveFile() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        currentSaveFile = saveDirectory.resolve(currentDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        try {
            Files.createFile(currentSaveFile);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to create video save file.");
            e.printStackTrace();
        }
        currentSaveFileExpiration = currentDateTime.plus(videoDuration);
    }

    public Optional<Path> getCurrentSaveFile() {
        if (currentSaveFileExpiration.isBefore(LocalDateTime.now())) {
            nextSaveFile();
        }
        return Files.exists(currentSaveFile) ? Optional.of(currentSaveFile) : Optional.empty();
    }

}
