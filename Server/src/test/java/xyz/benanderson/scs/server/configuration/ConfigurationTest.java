package xyz.benanderson.scs.server.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

//end-to-end integration test of the `Configuration` submodule
public class ConfigurationTest {

    private Path configFile;

    @BeforeEach
    public void setupConfigFile() throws IOException {
        this.configFile = Paths.get(System.getProperty("user.dir")).resolve("server.properties");
        if (Files.exists(this.configFile)) {
            Files.delete(this.configFile);
        }
    }

    @Test
    public void testConfigurationAndWrapper() {
        Path configFile = Paths.get(System.getProperty("user.dir")).resolve("server.properties");
        assertFalse(Files.exists(configFile));

        assertEquals(ConfigurationWrapper.getInstance().getServerAddress(), "127.0.0.1");
        assertEquals(ConfigurationWrapper.getInstance().getServerPort(), 8192);
        assertEquals(ConfigurationWrapper.getInstance().getMaxConnections(), 5);
        assertEquals(ConfigurationWrapper.getInstance().getVideoDuration(), Duration.ofMinutes(30));
    }

    @AfterEach
    public void deleteConfigFile() throws IOException {
        if (Files.exists(this.configFile)) {
            Files.delete(this.configFile);
        }
    }

}
