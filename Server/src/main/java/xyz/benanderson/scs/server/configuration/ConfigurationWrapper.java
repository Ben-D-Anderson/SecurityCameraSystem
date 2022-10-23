package xyz.benanderson.scs.server.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class ConfigurationWrapper {

    private static ConfigurationWrapper instance;
    private final Configuration configuration;

    //only access provided to object through public static getInstance() method
    public static ConfigurationWrapper getInstance() {
        if (instance == null) instance = new ConfigurationWrapper();
        return instance;
    }

    //private constructor to restrict access to public static getInstance() method
    private ConfigurationWrapper() {
        //System.getProperty("user.dir") returns current working directory
        //therefore creates configuration files in current working directory
        this.configuration = new Configuration(Paths.get(System.getProperty("user.dir")));
    }

    /**
     * @return Max allowed concurrent connections to the server
     */
    public int getMaxConnections() {
        return configuration.getInt("server.max-connections").orElse(Integer.MAX_VALUE);
    }

    /**
     * @return TCP port for the server to run on, or 0 if no port was specified in the config
     */
    public int getServerPort() {
        return configuration.getInt("server.port").orElse(0);
    }

    /**
     * @return TCP address for the server to run on, or 127.0.0.1 (localhost) if no address was specified in the config
     */
    public String getServerAddress() {
        return configuration.getString("server.address").orElse("127.0.0.1");
    }

    /**
     * @return {@code Path} denoting directory to save recorded videos to
     */
    public Path getVideoSaveDirectory() {
        return Paths.get(configuration.getRequiredString("video.save-directory"));
    }

    /**
     * @return {@code Duration} denoting preferred length of recorded videos
     */
    public Duration getVideoDuration() {
        int durationNumber = configuration.getInt("video.duration.number").orElse(30);
        TemporalUnit durationUnit = ChronoUnit.valueOf(configuration.getString("video.duration.unit")
                .orElse("minutes").toUpperCase());
        return Duration.of(durationNumber, durationUnit);
    }

}
