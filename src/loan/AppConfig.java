package loan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Reads database settings from environment variables or a local
 * config.properties file, so no credentials live in source code.
 *
 * Environment variables take priority over the file:
 *   LOAN_DB_URL, LOAN_DB_USER, LOAN_DB_PASSWORD
 * The file location can be changed with LOAN_CONFIG (default: ./config.properties).
 */
public final class AppConfig {

    private static final String CONFIG_PATH_ENV = "LOAN_CONFIG";
    private static final String DEFAULT_CONFIG_FILE = "config.properties";

    private static Properties fileProperties;

    private AppConfig() { }

    public static String dbUrl() {
        return required("db.url", "LOAN_DB_URL");
    }

    public static String dbUser() {
        return required("db.user", "LOAN_DB_USER");
    }

    public static String dbPassword() {
        String value = lookup("db.password", "LOAN_DB_PASSWORD");
        return value == null ? "" : value;
    }

    private static String required(String key, String envName) {

        String value = lookup(key, envName);

        if (value == null || value.isBlank()) {
            throw new ConfigException(
                    "Missing database setting '" + key + "'. Set the " + envName
                    + " environment variable or add it to config.properties "
                    + "(see config.properties.example).");
        }

        return value.trim();
    }

    private static String lookup(String key, String envName) {

        String fromEnvironment = System.getenv(envName);

        if (fromEnvironment != null && !fromEnvironment.isEmpty()) {
            return fromEnvironment;
        }

        return properties().getProperty(key);
    }

    private static synchronized Properties properties() {

        if (fileProperties == null) {

            Properties loaded = new Properties();

            String custom = System.getenv(CONFIG_PATH_ENV);
            Path path = Paths.get(
                    custom != null && !custom.isBlank() ? custom : DEFAULT_CONFIG_FILE);

            if (Files.isRegularFile(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    loaded.load(in);
                } catch (IOException e) {
                    throw new ConfigException(
                            "Could not read configuration file: " + path, e);
                }
            }

            fileProperties = loaded;
        }

        return fileProperties;
    }
}
