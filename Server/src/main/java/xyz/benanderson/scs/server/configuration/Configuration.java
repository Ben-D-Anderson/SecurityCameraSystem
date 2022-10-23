package xyz.benanderson.scs.server.configuration;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Configuration {

    private final Properties properties;
    private final String configFileName = "server.properties";
    private final Path configFile;

    public Configuration(Path configFolder) {
        configFile = configFolder.resolve(configFileName);
        Properties internalProperties = new Properties();
        loadInternalProperties(internalProperties);
        this.properties = new Properties(internalProperties);
        if (createFileIfNotExists()) {
            loadFromFile();
        }
    }

    private void loadInternalProperties(Properties internalProperties) {
        try {
            internalProperties.load(getClass().getClassLoader().getResourceAsStream(configFileName));
        } catch (IOException e) {
            System.err.println("[WARNING] Failed to read internal configuration file.");
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            this.properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("[WARNING] Failed to read external configuration file - resorting to internal configuration.");
            e.printStackTrace();
        }
    }

    private boolean createFileIfNotExists() {
        if (!Files.exists(configFile)) {
            try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileName)) {
                Files.copy(Objects.requireNonNull(inputStream), configFile);
            } catch (Exception e) {
                System.err.println("[WARNING] Failed to write default config to configuration file '" + configFile + "'.");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public Optional<String> getString(String key) {
        String env = System.getenv(key);
        if (env == null || env.strip().length() == 0)
            return Optional.ofNullable(this.properties.getProperty(key));
        return Optional.of(env);
    }

    public String getRequiredString(String key) {
        return getString(key).orElseThrow(() -> new NoSuchElementException("'" + key + "' cannot be empty"));
    }

    public Optional<Integer> getInt(String key) {
        return getString(key).map(Integer::parseInt);
    }

    public int getRequiredInt(String key) {
        return getInt(key).orElseThrow(() -> new NoSuchElementException("'" + key + "' must be an integer"));
    }

    public Optional<Double> getDouble(String key) {
        return getString(key).map(Double::parseDouble);
    }

    public double getRequiredDouble(String key) {
        return getDouble(key).orElseThrow(() -> new NoSuchElementException("'" + key + "' must be a double"));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        AtomicBoolean valBool = new AtomicBoolean(defaultValue);
        getString(key).ifPresent(val -> valBool.set(Boolean.parseBoolean(val)));
        return valBool.get();
    }

}