package Model.dao;

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

    /**
     * Mengambil seluruh jadwal dengan nama film dan studio.
     */
    public List<Schedule> findAll() throws SQLException {
        String sql = SELECT_FULL + " ORDER BY s.show_date, s.show_time";
        List<Schedule> schedules = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
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
        try (Connection conn = DBConnection.getInstance().getConnection();
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
        try (Connection conn = DBConnection.getInstance().getConnection();
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
     * Memperbarui data jadwal. Hanya boleh dijalankan jika jadwal belum memiliki booking aktif.
     */
    public boolean update(
            long id,
            long filmId,
            long studioId,
            LocalDate showDate,
            LocalTime showTime,
            BigDecimal ticketPrice,
            int totalSeats) throws SQLException {
        String sql = """
                UPDATE schedules
                SET film_id = ?, studio_id = ?, show_date = ?, show_time = ?,
                    ticket_price = ?, total_seats = ?
                WHERE id = ?
                """;
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, filmId);
            stmt.setLong(2, studioId);
            stmt.setDate(3, Date.valueOf(showDate));
            stmt.setTime(4, Time.valueOf(showTime));
            stmt.setBigDecimal(5, ticketPrice);
            stmt.setInt(6, totalSeats);
            stmt.setLong(7, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Menghapus jadwal. Akan melempar SQLException jika masih ada booking
     * (FK constraint fk_bookings_schedule ON DELETE RESTRICT).
     */
    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
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
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, scheduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Memeriksa apakah ada konflik jadwal pada studio yang sama di tanggal dan jam yang sama.
     * Mengecualikan ID jadwal yang diedit (excludeId = 0 untuk insert baru).
     * Database sudah memiliki UNIQUE KEY uq_schedules_studio_start (studio_id, show_date, show_time),
     * sehingga insert/update yang konflik akan melempar SQLException error code 1062.
     */
    public boolean hasConflict(long studioId, LocalDate showDate, LocalTime showTime, long excludeId)
            throws SQLException {
        String sql = """
                SELECT 1 FROM schedules
                WHERE studio_id = ? AND show_date = ? AND show_time = ? AND id != ?
                LIMIT 1
                """;
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studioId);
            stmt.setDate(2, Date.valueOf(showDate));
            stmt.setTime(3, Time.valueOf(showTime));
            stmt.setLong(4, excludeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Mengambil daftar film yang tersedia untuk dropdown jadwal.
     * Hanya membaca kolom minimal dari tabel films.
     * Film DAO milik Tian tidak tersedia di repository saat ini;
     * ScheduleDao membaca langsung dari tabel films untuk kebutuhan dropdown.
     */
    public List<FilmSummary> findAvailableFilms() throws SQLException {
        String sql = "SELECT id, title FROM films WHERE status IN ('NOW_SHOWING', 'COMING_SOON') ORDER BY title";
        List<FilmSummary> films = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
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
