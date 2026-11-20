package Model.dao;

import Controler.ConnectionProvider;
import Controler.DBConnection;
import Model.entity.Studio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * DAO untuk operasi CRUD pada tabel studios.
 */
public class StudioDao {

    private final ConnectionProvider connectionProvider;

    public StudioDao() {
        this(DBConnection.getInstance()::getConnection);
    }

    public StudioDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider);
    }

    /**
     * Mengambil seluruh daftar studio.
     */
    public List<Studio> findAll() throws SQLException {
        String sql = "SELECT id, name, seat_rows, seat_cols FROM studios ORDER BY name";
        List<Studio> studios = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                studios.add(mapStudio(rs));
            }
        }
        return studios;
    }

    /**
     * Mencari studio berdasarkan ID.
     */
    public Optional<Studio> findById(long id) throws SQLException {
        String sql = "SELECT id, name, seat_rows, seat_cols FROM studios WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapStudio(rs));
            }
        }
    }

    /**
     * Menyimpan studio baru. Mengembalikan studio dengan ID yang di-generate.
     * Melempar SQLException jika nama sudah ada (uq_studios_name).
     */
    public Studio insert(String name, int seatRows, int seatCols) throws SQLException {
        String sql = "INSERT INTO studios (name, seat_rows, seat_cols) VALUES (?, ?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setInt(2, seatRows);
            stmt.setInt(3, seatCols);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Gagal menyimpan studio.");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Gagal mendapatkan ID studio baru.");
                }
                long newId = keys.getLong(1);
                return new Studio(newId, name, seatRows, seatCols);
            }
        }
    }

    /**
     * Memperbarui data studio. Mengembalikan true jika baris berhasil diperbarui.
     * Jika dimensi (baris atau kolom) diubah, studio tidak boleh sedang digunakan oleh jadwal.
     */
    public boolean update(long id, String name, int seatRows, int seatCols) throws SQLException {
        Optional<Studio> currentOpt = findById(id);
        if (currentOpt.isPresent()) {
            Studio current = currentOpt.get();
            if ((current.getSeatRows() != seatRows || current.getSeatCols() != seatCols)
                    && isUsedBySchedule(id)) {
                throw new IllegalStateException(
                        "Dimensi kursi studio (baris/kolom) tidak dapat diubah karena studio sudah digunakan oleh jadwal tayang.");
            }
        }

        String sql = "UPDATE studios SET name = ?, seat_rows = ?, seat_cols = ? WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, seatRows);
            stmt.setInt(3, seatCols);
            stmt.setLong(4, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Menghapus studio berdasarkan ID.
     * Akan melempar SQLException jika studio masih digunakan oleh jadwal
     * (FK constraint fk_schedules_studio ON DELETE RESTRICT).
     */
    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM studios WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Memeriksa apakah studio masih digunakan oleh jadwal aktif.
     */
    public boolean isUsedBySchedule(long studioId) throws SQLException {
        String sql = "SELECT 1 FROM schedules WHERE studio_id = ? LIMIT 1";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Memeriksa apakah nama studio sudah digunakan oleh studio lain.
     */
    public boolean nameExistsForOther(String name, long excludeId) throws SQLException {
        String sql = "SELECT 1 FROM studios WHERE name = ? AND id != ? LIMIT 1";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setLong(2, excludeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Studio mapStudio(ResultSet rs) throws SQLException {
        return new Studio(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getInt("seat_rows"),
                rs.getInt("seat_cols"));
    }
}
