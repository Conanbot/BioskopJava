package Model.service;

import Model.dao.UserDao;
import Model.exception.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * Service untuk provisioning akun Admin secara aman.
 * <p>
 * Menjamin:
 * 1. Password disimpan ke database.
 * 2. Proses bersifat idempoten (dapat dijalankan berulang tanpa duplikasi akun).
 * 3. Mendukung konfigurasi via environment variable, config.properties, atau CLI.
 */
public class AdminProvisioningService {

    public enum ProvisionStatus {
        SUCCESS,
        ALREADY_EXISTS,
        SKIPPED_NO_CONFIG
    }

    private static final Path CONFIG_PATH = Path.of("config.properties");
    private final UserDao userDao;

    public AdminProvisioningService() {
        this(new UserDao());
    }

    public AdminProvisioningService(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "UserDao wajib diisi.");
    }

    /**
     * Membuat akun Admin baru jika belum ada.
     *
     * @param username username admin (minimal 1 karakter, maksimal 50)
     * @param rawPassword password mentah admin (minimal 6 karakter)
     * @return Status hasil provisioning (SUCCESS atau ALREADY_EXISTS)
     * @throws ValidationException jika input tidak valid
     * @throws SQLException jika terjadi kegagalan database
     */
    public ProvisionStatus provisionAdmin(String username, String rawPassword)
            throws ValidationException, SQLException {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username Admin wajib diisi.");
        }
        String trimmedUsername = username.trim();
        if (trimmedUsername.length() > 50) {
            throw new ValidationException("Username Admin maksimal 50 karakter.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new ValidationException("Password Admin wajib diisi.");
        }
        if (rawPassword.length() < 6) {
            throw new ValidationException("Password Admin minimal 6 karakter.");
        }

        if (userDao.usernameExists(trimmedUsername)) {
            return ProvisionStatus.ALREADY_EXISTS;
        }

        boolean created = userDao.provisionAdmin(trimmedUsername, rawPassword);
        return created ? ProvisionStatus.SUCCESS : ProvisionStatus.ALREADY_EXISTS;
    }

    /**
     * Menjalankan provisioning admin berdasarkan konfigurasi environment variable
     * (BIOSKOP_ADMIN_USERNAME, BIOSKOP_ADMIN_PASSWORD) atau config.properties
     * (admin.username, admin.password).
     */
    public ProvisionStatus provisionFromConfig() throws ValidationException, SQLException {
        Properties properties = loadProperties();

        String username = System.getenv("BIOSKOP_ADMIN_USERNAME");
        if (username == null || username.isBlank()) {
            username = properties.getProperty("admin.username", "admin");
        }

        String password = System.getenv("BIOSKOP_ADMIN_PASSWORD");
        if (password == null || password.isBlank()) {
            password = properties.getProperty("admin.password");
        }

        if (password == null || password.isBlank()) {
            return ProvisionStatus.SKIPPED_NO_CONFIG;
        }

        return provisionAdmin(username, password);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        if (!Files.exists(CONFIG_PATH)) {
            return properties;
        }
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            properties.load(in);
        } catch (IOException ignored) {
        }
        return properties;
    }

    /**
     * Entry point CLI untuk provisioning akun Admin.
     * Penggunaan:
     *   java -cp ... Model.service.AdminProvisioningService [username] [password]
     * atau melalui Maven:
     *   mvn exec:java -Dexec.mainClass="Model.service.AdminProvisioningService"
     */
    public static void main(String[] args) {
        AdminProvisioningService service = new AdminProvisioningService();
        try {
            ProvisionStatus status;
            if (args.length >= 2) {
                String username = args[0];
                String password = args[1];
                status = service.provisionAdmin(username, password);
            } else if (args.length == 1) {
                System.err.println("Penggunaan: java ... AdminProvisioningService <username> <password>");
                System.exit(1);
                return;
            } else {
                // Coba baca dari environment atau config.properties
                status = service.provisionFromConfig();
                if (status == ProvisionStatus.SKIPPED_NO_CONFIG) {
                    // Cek jika konsol interaktif tersedia
                    if (System.console() != null) {
                        String username = System.console().readLine("Masukkan username admin [admin]: ");
                        if (username == null || username.isBlank()) {
                            username = "admin";
                        }
                        char[] passChars = System.console().readPassword("Masukkan password admin: ");
                        if (passChars == null || passChars.length == 0) {
                            System.err.println("Password tidak boleh kosong.");
                            System.exit(1);
                            return;
                        }
                        status = service.provisionAdmin(username, new String(passChars));
                    } else {
                        System.err.println("Tidak ada konfigurasi password admin ditemukan pada environment atau config.properties.");
                        System.err.println("Atur BIOSKOP_ADMIN_PASSWORD atau isi admin.password pada config.properties.");
                        System.exit(1);
                        return;
                    }
                }
            }

            switch (status) {
                case SUCCESS -> System.out.println("Akun Admin berhasil dibuat.");
                case ALREADY_EXISTS -> System.out.println("Akun Admin sudah ada di database. Tidak ada perubahan dilakukan.");
                case SKIPPED_NO_CONFIG -> System.out.println("Provisioning dilewati: konfigurasi password tidak ditemukan.");
            }
        } catch (Exception ex) {
            System.err.println("Gagal melakukan provisioning admin: " + ex.getMessage());
            System.exit(1);
        }
    }
}
