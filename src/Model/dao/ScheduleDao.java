package Model.dao;

import Controler.ConnectionProvider;
import Controler.DBConnection;
import Model.entity.Schedule;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * DAO untuk operasi CRUD pada tabel schedules.
 * Menggunakan JOIN ke films dan studios untuk mendapatkan nama film dan studio.
 */
public class ScheduleDao {

    private static final String SELECT_FULL = """
            SELECT s.id, s.film_id, s.studio_id, s.show_date, s.show_time,
                   s.ticket_price, s.total_seats, s.available_seats,
                   f.title AS film_title, st.name AS studio_name
            FROM schedules s
            JOIN films f ON f.id = s.film_id
            JOIN studios st ON st.id = s.studio_id
            """;

    private final ConnectionProvider connectionProvider;

    public ScheduleDao() {
        this(DBConnection.getInstance()::getConnection);
    }

    public ScheduleDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider);
    }

    /**
     * Mengambil seluruh jadwal dengan nama film dan studio.
     */
    public List<Schedule> findAll() throws SQLException {
        String sql = SELECT_FULL + " ORDER BY s.show_date, s.show_time";
        List<Schedule> schedules = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                schedules.add(mapSchedule(rs));
            }
        }
        return schedules;
    }

    /**
     * Mencari jadwal berdasarkan ID.
     */
    public Optional<Schedule> findById(long id) throws SQLException {
        String sql = SELECT_FULL + " WHERE s.id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapSchedule(rs));
            }
        }
    }

    /**
     * Menyimpan jadwal baru. Total kursi dihitung dari dimensi studio (seatRows × seatCols).
     * Mengembalikan jadwal dengan ID yang di-generate.
     */
    public Schedule insert(
            long filmId,
            long studioId,
            LocalDate showDate,
            LocalTime showTime,
            BigDecimal ticketPrice,
            int totalSeats) throws SQLException {
        String sql = """
                INSERT INTO schedules
                    (film_id, studio_id, show_date, show_time, ticket_price, total_seats, available_seats)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, filmId);
            stmt.setLong(2, studioId);
            stmt.setDate(3, Date.valueOf(showDate));
            stmt.setTime(4, Time.valueOf(showTime));
            stmt.setBigDecimal(5, ticketPrice);
            stmt.setInt(6, totalSeats);
            stmt.setInt(7, totalSeats); // available_seats sama dengan total_seats saat baru dibuat
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Gagal menyimpan jadwal.");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Gagal mendapatkan ID jadwal baru.");
                }
                long newId = keys.getLong(1);
                return findById(newId).orElseThrow(
                        () -> new SQLException("Jadwal baru tidak dapat ditemukan setelah disimpan."));
            }
        }
    }

    /**
     * Memperbarui data jadwal secara transaksional.
     * <p>
     * Konsistensi yang dijamin:
     * 1. Total kapasitas divalidasi terhadap dimensi studio.
     * 2. Jumlah kursi yang sudah dipesan (status ACTIVE) dihitung.
     * 3. Kapasitas baru tidak boleh lebih kecil dari kursi yang sudah dipesan.
     * 4. available_seats dihitung ulang secara konsisten: available_seats = totalSeats - activeBookedSeats.
     * 5. available_seats tidak pernah melebihi total_seats.
     * 6. Seluruh pembaruan dibungkus transaksi database; kegagalan memicu rollback.
     */
    public boolean update(
            long id,
            long filmId,
            long studioId,
            LocalDate showDate,
            LocalTime showTime,
            BigDecimal ticketPrice,
            int totalSeats) throws SQLException {
        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Validasi kapasitas konsisten dengan studio
                String studioSql = "SELECT seat_rows * seat_cols AS capacity FROM studios WHERE id = ?";
                int studioCapacity;
                try (PreparedStatement studioStmt = conn.prepareStatement(studioSql)) {
                    studioStmt.setLong(1, studioId);
                    try (ResultSet rs = studioStmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Studio dengan ID " + studioId + " tidak ditemukan.");
                        }
                        studioCapacity = rs.getInt("capacity");
                    }
                }

                if (totalSeats != studioCapacity) {
                    totalSeats = studioCapacity;
                }

                // 2. Hitung kursi yang sedang aktif dibooking pada jadwal ini
                String bookedSeatsSql = """
                        SELECT COUNT(*) AS booked_count
                        FROM booking_seats bs
                        JOIN bookings b ON b.id = bs.booking_id
                        WHERE bs.schedule_id = ? AND b.status = 'ACTIVE'
                        """;
                int bookedSeats = 0;
                try (PreparedStatement bookedStmt = conn.prepareStatement(bookedSeatsSql)) {
                    bookedStmt.setLong(1, id);
                    try (ResultSet rs = bookedStmt.executeQuery()) {
                        if (rs.next()) {
                            bookedSeats = rs.getInt("booked_count");
                        }
                    }
                }

                if (totalSeats < bookedSeats) {
                    throw new IllegalStateException(
                            "Kapasitas studio baru (" + totalSeats + ") lebih kecil dari jumlah kursi yang sudah dipesan ("
                            + bookedSeats + "). Perubahan jadwal dibatalkan.");
                }

                int availableSeats = totalSeats - bookedSeats;
                if (availableSeats < 0 || availableSeats > totalSeats) {
                    throw new IllegalStateException("Perhitungan kursi tersedia tidak valid: " + availableSeats);
                }

                // 3. Update jadwal secara konsisten
                String updateSql = """
                        UPDATE schedules
                        SET film_id = ?, studio_id = ?, show_date = ?, show_time = ?,
                            ticket_price = ?, total_seats = ?, available_seats = ?
                        WHERE id = ?
                        """;
                boolean updated;
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setLong(1, filmId);
                    stmt.setLong(2, studioId);
                    stmt.setDate(3, Date.valueOf(showDate));
                    stmt.setTime(4, Time.valueOf(showTime));
                    stmt.setBigDecimal(5, ticketPrice);
                    stmt.setInt(6, totalSeats);
                    stmt.setInt(7, availableSeats);
                    stmt.setLong(8, id);
                    updated = stmt.executeUpdate() > 0;
                }

                conn.commit();
                return updated;
            } catch (Exception ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
                }
                if (ex instanceof RuntimeException rtEx) {
                    throw rtEx;
                }
                throw new SQLException("Gagal memperbarui jadwal: " + ex.getMessage(), ex);
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Menghapus jadwal.
     * Akan menolak jika jadwal memiliki riwayat booking dalam status apa pun
     * (ACTIVE, CANCELLED, REFUNDED) demi melindungi riwayat transaksi dan FK constraint.
     */
    public boolean delete(long id) throws SQLException {
        if (hasAnyBookings(id)) {
            throw new IllegalStateException(
                    "Jadwal tidak dapat dihapus karena memiliki riwayat transaksi/booking.");
        }
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Memeriksa apakah jadwal masih memiliki booking aktif (status ACTIVE).
     */
    public boolean hasActiveBookings(long scheduleId) throws SQLException {
        String sql = "SELECT 1 FROM bookings WHERE schedule_id = ? AND status = 'ACTIVE' LIMIT 1";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, scheduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Memeriksa apakah jadwal memiliki riwayat booking dalam status apa pun
     * (ACTIVE, CANCELLED, atau REFUNDED).
     */
    public boolean hasAnyBookings(long scheduleId) throws SQLException {
        String sql = "SELECT 1 FROM bookings WHERE schedule_id = ? LIMIT 1";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, scheduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Mengambil durasi film (dalam menit) dari tabel films.
     */
    public int getFilmDurationMinutes(long filmId) throws SQLException {
        String sql = "SELECT duration_minutes FROM films WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, filmId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Film dengan ID " + filmId + " tidak ditemukan.");
                }
                return rs.getInt("duration_minutes");
            }
        }
    }

    /**
     * Memeriksa apakah ada konflik jadwal berdasarkan interval waktu penayangan film.
     * Formula interval: existing_start < new_end AND existing_end > new_start
     */
    public boolean hasConflict(long filmId, long studioId, LocalDate showDate, LocalTime showTime, long excludeId)
            throws SQLException {
        int durationMinutes = getFilmDurationMinutes(filmId);
        return hasConflict(studioId, showDate, showTime, durationMinutes, excludeId);
    }

    /**
     * Memeriksa konflik jadwal di studio dan tanggal yang sama dengan durasi film baru dalam menit.
     */
    public boolean hasConflict(long studioId, LocalDate showDate, LocalTime showTime, int durationMinutes, long excludeId)
            throws SQLException {
        String sql = """
                SELECT 1
                FROM schedules s
                JOIN films f ON f.id = s.film_id
                WHERE s.studio_id = ?
                  AND s.show_date = ?
                  AND s.id != ?
                  AND s.show_time < ADDTIME(?, SEC_TO_TIME(? * 60))
                  AND ADDTIME(s.show_time, SEC_TO_TIME(f.duration_minutes * 60)) > ?
                LIMIT 1
                """;
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studioId);
            stmt.setDate(2, Date.valueOf(showDate));
            stmt.setLong(3, excludeId);
            stmt.setTime(4, Time.valueOf(showTime));
            stmt.setInt(5, durationMinutes);
            stmt.setTime(6, Time.valueOf(showTime));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Backward-compatible overload: memeriksa konflik jika durasi tidak diketahui (mengasumsikan 0 menit durasi baru).
     */
    public boolean hasConflict(long studioId, LocalDate showDate, LocalTime showTime, long excludeId)
            throws SQLException {
        return hasConflict(studioId, showDate, showTime, 0, excludeId);
    }

    /**
     * Logika murni pemeriksaan tumpang tindih interval waktu penayangan (untuk unit testing & validasi domain).
     * Aturan: existing_start < new_end AND existing_end > new_start.
     * Jadwal berurutan (misal: selesai 16.00 dan jadwal baru mulai 16.00) TIDAK bertabrakan.
     */
    public static boolean isIntervalConflicting(
            LocalTime start1, int duration1Minutes,
            LocalTime start2, int duration2Minutes) {
        int s1 = start1.toSecondOfDay();
        int e1 = s1 + (duration1Minutes * 60);
        int s2 = start2.toSecondOfDay();
        int e2 = s2 + (duration2Minutes * 60);
        return s1 < e2 && e1 > s2;
    }

    /**
     * Mengambil daftar film yang tersedia untuk dropdown jadwal.
     */
    public List<FilmSummary> findAvailableFilms() throws SQLException {
        String sql = "SELECT id, title FROM films WHERE status IN ('NOW_SHOWING', 'COMING_SOON') ORDER BY title";
        List<FilmSummary> films = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                films.add(new FilmSummary(rs.getLong("id"), rs.getString("title")));
            }
        }
        return films;
    }

    private Schedule mapSchedule(ResultSet rs) throws SQLException {
        return new Schedule(
                rs.getLong("id"),
                rs.getLong("film_id"),
                rs.getLong("studio_id"),
                rs.getDate("show_date").toLocalDate(),
                rs.getTime("show_time").toLocalTime(),
                rs.getBigDecimal("ticket_price"),
                rs.getInt("total_seats"),
                rs.getInt("available_seats"),
                rs.getString("film_title"),
                rs.getString("studio_name"));
    }

    /**
     * Record ringkasan film untuk dropdown pemilihan film di form jadwal.
     */
    public record FilmSummary(long id, String title) {
        @Override
        public String toString() {
            return title;
        }
    }
}
