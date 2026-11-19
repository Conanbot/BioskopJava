package Model.dao;

import Controler.DBConnection;
import Model.entity.User;
import Model.enums.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * DAO untuk operasi pengguna pada tabel users.
 * Semua query menggunakan PreparedStatement untuk mencegah SQL injection.
 */
public class UserDao {

    /**
     * Mencari pengguna berdasarkan username.
     * Mengembalikan Optional yang memuat password_hash untuk verifikasi.
     */
    public Optional<UserCredential> findCredentialByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                long id = rs.getLong("id");
                String dbUsername = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                Role role = Role.valueOf(rs.getString("role"));
                return Optional.of(new UserCredential(id, dbUsername, passwordHash, role));
            }
        }
    }

    /**
     * Mencari entitas User berdasarkan ID (tanpa password_hash).
     */
    public Optional<User> findById(long userId) throws SQLException {
        String sql = "SELECT id, username, role, created_at FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUser(rs));
            }
        }
    }

    /**
     * Memeriksa apakah username sudah terdaftar.
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Menyimpan pengguna baru. Hanya boleh membuat pengguna dengan role CUSTOMER
     * melalui registrasi publik. Admin hanya dibuat melalui seed.sql.
     * Mengembalikan entitas User dengan ID yang di-generate database.
     */
    public User insertCustomer(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, 'CUSTOMER')";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Gagal menyimpan pengguna baru.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Gagal mendapatkan ID pengguna baru.");
                }
                long newId = generatedKeys.getLong(1);
                return findById(newId).orElseThrow(
                        () -> new SQLException("Pengguna baru tidak dapat ditemukan setelah disimpan."));
            }
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String username = rs.getString("username");
        Role role = Role.valueOf(rs.getString("role"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new User(id, username, role,
                createdAt != null ? createdAt.toLocalDateTime() : java.time.LocalDateTime.now());
    }

    /**
     * Record untuk membawa data kredensial login (termasuk password_hash).
     * Hanya digunakan di lapisan autentikasi; jangan di-expose ke View.
     */
    public record UserCredential(long id, String username, String passwordHash, Role role) {
    }
}
