package Controler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Pengelola koneksi terpusat ke database MySQL.
 * Menggunakan konfigurasi langsung tanpa ketergantungan pada config.properties.
 * Default lingkungan: Laragon / XAMPP (localhost:3306, user 'root', password kosong).
 */
public final class DBConnection {
    private static final DBConnection INSTANCE = new DBConnection();

    // Konstanta koneksi default aplikasi utama
    public static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/bioskop_db?serverTimezone=Asia/Jakarta&useSSL=false&allowPublicKeyRetrieval=true";
    public static final String DEFAULT_USER = "root";
    public static final String DEFAULT_PASSWORD = "";

    // Driver MySQL JDBC
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Driver modern otomatis didukung lewat SPI / ServiceLoader
        }
    }

    private DBConnection() {
    }

    public static DBConnection getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return getConnection(DEFAULT_URL, DEFAULT_USER, DEFAULT_PASSWORD);
    }

    public Connection getConnection(String url, String username, String password) throws SQLException {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            int errorCode = ex.getErrorCode();
            String sqlState = ex.getSQLState();
            if (errorCode == 1045 || "28000".equals(sqlState)) {
                throw new SQLException("Autentikasi MySQL gagal untuk user '" + username
                        + "'. Periksa username dan password pada database MySQL Anda.", ex);
            } else if (errorCode == 1049 || "42000".equals(sqlState)) {
                throw new SQLException("Database belum dibuat. Jalankan database/schema.sql pada MySQL.", ex);
            } else if ("08S01".equals(sqlState) || "08001".equals(sqlState)
                    || ex.getCause() instanceof java.net.ConnectException
                    || (ex.getMessage() != null && ex.getMessage().contains("Communications link failure"))) {
                throw new SQLException("Tidak dapat menghubungi server MySQL. Pastikan service MySQL (Laragon/XAMPP) sedang aktif.", ex);
            }
            throw ex;
        }
    }
}
