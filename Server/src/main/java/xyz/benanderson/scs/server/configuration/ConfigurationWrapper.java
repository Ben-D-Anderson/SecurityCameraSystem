package xyz.benanderson.scs.server.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class ConfigurationWrapper {

    private static ConfigurationWrapper instance;
    private final Configuration configuration;

    public static ConfigurationWrapper getInstance() {
        if (instance == null) instance = new ConfigurationWrapper();
        return instance;
    }

    private ConfigurationWrapper() {
        this.configuration = new Configuration(Paths.get(System.getProperty("user.dir")));
    }

    public int getMaxConnections() {
        return configuration.getInt("server.max-connections").orElse(Integer.MAX_VALUE);
    }

    public int getServerPort() {
        return configuration.getInt("server.port").orElse(0);
    }

    public String getServerAddress() {
        return configuration.getString("server.address").orElse("127.0.0.1");
    }

    public Path getVideoSaveDirectory() {
        return Paths.get(configuration.getRequiredString("video.save-directory"));
    }

    public Duration getVideoDuration() {
        int durationNumber = configuration.getInt("video.duration.number").orElse(30);
        TemporalUnit durationUnit = ChronoUnit.valueOf(configuration.getString("video.duration.unit")
                .orElse("minutes").toUpperCase());
        return Duration.of(durationNumber, durationUnit);
    }

}
