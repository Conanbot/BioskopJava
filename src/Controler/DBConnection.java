package Controler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private static final DBConnection INSTANCE = new DBConnection();
    private static final Path CONFIG_PATH = Path.of("config.properties");

    private DBConnection() {
    }

    public static DBConnection getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        Properties properties = loadProperties();
        String url = getSetting(properties, "db.url", "BIOSKOP_DB_URL");
        String username = getSetting(properties, "db.username", "BIOSKOP_DB_USERNAME");
        String password = getSetting(properties, "db.password", "BIOSKOP_DB_PASSWORD");

        return DriverManager.getConnection(url, username, password);
    }

    private Properties loadProperties() throws SQLException {
        Properties properties = new Properties();
        if (!Files.exists(CONFIG_PATH)) {
            return properties;
        }

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new SQLException("Tidak dapat membaca konfigurasi database.", exception);
        }
    }

    private String getSetting(Properties properties, String propertyName, String environmentName)
            throws SQLException {
        String value = System.getenv(environmentName);
        if (value == null || value.isBlank()) {
            value = properties.getProperty(propertyName);
        }
        if (value == null || value.isBlank()) {
            throw new SQLException(
                    "Konfigurasi " + propertyName + " belum diisi. Atur " + environmentName
                            + " atau salin config.properties.example menjadi config.properties.");
        }
        return value.trim();
    }
}
