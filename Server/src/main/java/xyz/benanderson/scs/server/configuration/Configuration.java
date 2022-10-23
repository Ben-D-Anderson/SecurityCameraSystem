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

@Getter
public class Configuration {

    private final Properties properties;
    private final String configFileName = "server.properties";
    private final Path configFile;

    public Configuration(Path configFolder) {
        //get path to config file
        configFile = configFolder.resolve(configFileName);
        //load internal default config
        Properties internalProperties = new Properties();
        loadInternalProperties(internalProperties);
        //create properties with defaults
        this.properties = new Properties(internalProperties);
        //load properties from file if it exists, otherwise create file with defaults
        if (createFileIfNotExists()) {
            loadFromFile();
        }
    }

    //loads properties into the given properties object from the
    //included default config file embedded in the application
    private void loadInternalProperties(Properties internalProperties) {
        try {
            internalProperties.load(getClass().getClassLoader().getResourceAsStream(configFileName));
        } catch (IOException e) {
            System.err.println("[WARNING] Failed to read internal configuration file.");
            e.printStackTrace();
        }
    }

    //loads properties from the configFile into the properties attribute of this object
    private void loadFromFile() {
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            this.properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("[WARNING] Failed to read external configuration file - resorting to internal configuration.");
            e.printStackTrace();
        }
    }

    //copies the default config file that is embedded in the application to the location where the config file is stored
    //on disk. this only occurs if the file does not exist on disk already.
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

    /**
     * Get an {@code Optional} denoting a {@code String} value from the config.
     *
     * @param key configuration entry key
     * @return configuration entry value as an {@code Optional}
     */
    public Optional<String> getString(String key) {
        //first check system environment variables to allow for dynamic entry inclusion
        String env = System.getenv(key);
        //if key was not a system environment variable, return the Optional wrapped result from the properties table
        if (env == null || env.strip().length() == 0)
            return Optional.ofNullable(this.properties.getProperty(key));
        return Optional.of(env);
    }

    /**
     * Convenience method which throws a {@code NoSuchElementException} if the {@code Optional} returned by
     * {@link Configuration#getString(String)} is empty.
     *
     * @param key configuration entry key
     * @return configuration entry value (not null)
     */
    public String getRequiredString(String key) throws NoSuchElementException {
        return getString(key).orElseThrow(() -> new NoSuchElementException("'" + key + "' cannot be empty"));
    }

    /**
     * Get an {@code Optional} denoting an {@code Integer} value from the config.
     *
     * @param key configuration entry key
     * @return configuration entry value as an {@code Optional}
     */
    public Optional<Integer> getInt(String key) {
        return getString(key).map(Integer::parseInt);
    }

    /**
     * Convenience method which throws a {@code NoSuchElementException} if the {@code Optional} returned by
     * {@link Configuration#getInt(String)} is empty.
     *
     * @param key configuration entry key
     * @return configuration entry value (not null)
     */
    public int getRequiredInt(String key) throws NoSuchElementException {
        return getInt(key).orElseThrow(() -> new NoSuchElementException("'" + key + "' must be an integer"));
    }

    /**
     * Get an {@code Optional} denoting a {@code Double} value from the config.
     *
     * @param key configuration entry key
     * @return configuration entry value as an {@code Optional}
     */
    public Optional<Double> getDouble(String key) {
        return getString(key).map(Double::parseDouble);
    }

    /**
     * Convenience method which throws a {@code NoSuchElementException} if the {@code Optional} returned by
     * {@link Configuration#getDouble(String)} is empty.
     *
     * @param key configuration entry key
     * @return configuration entry value (not null)
     */
    public double getRequiredDouble(String key) throws NoSuchElementException {
        return getDouble(key).orElseThrow(() -> new NoSuchElementException("'" + key + "' must be a double"));
    }

    /**
     * Get a {@code Boolean} value from the config, or return the provided default value if the key is not present
     * in the config or they entry's value is not a boolean.
     *
     * @param key configuration entry key
     * @return configuration entry value or the default value as a {@code boolean}
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return getString(key).map(Boolean::parseBoolean).orElse(defaultValue);
    }

}