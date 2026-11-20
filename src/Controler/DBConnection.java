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

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Driver may also be registered via ServiceLoader in modern JDBC
        }
    }

    private DBConnection() {
    }

    public static DBConnection getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        Properties properties = loadProperties();
        String url = getSetting(properties, "db.url", "BIOSKOP_DB_URL", false);
        String username = getSetting(properties, "db.username", "BIOSKOP_DB_USERNAME", false);
        String password = getSetting(properties, "db.password", "BIOSKOP_DB_PASSWORD", true);

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            int errorCode = ex.getErrorCode();
            String sqlState = ex.getSQLState();
            if (errorCode == 1045 || "28000".equals(sqlState)) {
                throw new SQLException("Autentikasi MySQL gagal untuk user '" + username
                        + "'. Periksa username dan password pada config.properties.", ex);
            } else if (errorCode == 1049 || "42000".equals(sqlState)) {
                throw new SQLException("Database pada URL belum dibuat. Jalankan database/schema.sql pada MySQL.", ex);
            } else if ("08S01".equals(sqlState) || "08001".equals(sqlState)
                    || ex.getCause() instanceof java.net.ConnectException
                    || (ex.getMessage() != null && ex.getMessage().contains("Communications link failure"))) {
                throw new SQLException("Tidak dapat menghubungi server MySQL. Pastikan service MySQL (Laragon/XAMPP) sedang aktif.", ex);
            }
            throw ex;
        }
    }

    private Properties loadProperties() throws SQLException {
        Properties properties = new Properties();

        // 1. Check custom path via system property
        String customPath = System.getProperty("config.file");
        if (customPath != null && !customPath.isBlank()) {
            Path path = Path.of(customPath);
            if (Files.exists(path)) {
                try (InputStream input = Files.newInputStream(path)) {
                    properties.load(input);
                    return properties;
                } catch (IOException ex) {
                    throw new SQLException("Tidak dapat membaca konfigurasi database dari: " + customPath, ex);
                }
            }
        }

        // 2. Check current working directory
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                properties.load(input);
                return properties;
            } catch (IOException exception) {
                throw new SQLException("Tidak dapat membaca konfigurasi database dari config.properties.", exception);
            }
        }

        // 3. Check parent directory (when executing inside sub-directory or IDE output)
        Path parentConfig = Path.of("..", "config.properties");
        if (Files.exists(parentConfig)) {
            try (InputStream input = Files.newInputStream(parentConfig)) {
                properties.load(input);
                return properties;
            } catch (IOException exception) {
                throw new SQLException("Tidak dapat membaca konfigurasi database dari ../config.properties.", exception);
            }
        }

        // 4. Check ClassLoader classpath resources
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                return properties;
            }
        } catch (IOException ignored) {
        }

        return properties;
    }

    private String getSetting(Properties properties, String propertyName, String environmentName, boolean allowEmpty)
            throws SQLException {
        String value = System.getenv(environmentName);
        if (value == null) {
            value = properties.getProperty(propertyName);
        }
        if (value == null || (!allowEmpty && value.isBlank())) {
            throw new SQLException(
                    "Konfigurasi " + propertyName + " belum diisi. Atur " + environmentName
                            + " atau salin config.properties.example menjadi config.properties.");
        }
        return value.trim();
    }
}
