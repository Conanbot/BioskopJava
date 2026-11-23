package Model.dao;

import Controler.ConnectionProvider;
import Controler.DBConnection;
import Model.entity.Film;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Data Access Object (DAO) untuk tabel films.
 * Menerapkan JDBC, PreparedStatement, perulangan, dan Exception Handling.
 */
public class FilmDao {

    private final ConnectionProvider connectionProvider;

    public FilmDao() {
        this(DBConnection.getInstance()::getConnection);
    }

    public FilmDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider);
    }

    public List<Film> findAll() throws SQLException {
        String sql = "SELECT * FROM films ORDER BY status, title";
        List<Film> films = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                films.add(mapFilm(rs));
            }
        }
        return films;
    }

    public List<Film> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM films WHERE status = ? ORDER BY title";
        List<Film> films = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    films.add(mapFilm(rs));
                }
            }
        }
        return films;
    }

    public Optional<Film> findById(long id) throws SQLException {
        String sql = "SELECT * FROM films WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapFilm(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Film> searchFilms(String keyword) throws SQLException {
        String sql = "SELECT * FROM films WHERE title LIKE ? OR genre LIKE ? ORDER BY title";
        List<Film> films = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    films.add(mapFilm(rs));
                }
            }
        }
        return films;
    }

    public long insert(Film film) throws SQLException {
        String sql = """
                INSERT INTO films (title, genre, duration_minutes, age_rating, synopsis, poster_path, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, film.getTitle());
            stmt.setString(2, film.getGenre());
            stmt.setInt(3, film.getDurationMinutes());
            stmt.setString(4, film.getAgeRating());
            stmt.setString(5, film.getSynopsis());
            stmt.setString(6, film.getPosterPath());
            stmt.setString(7, film.getStatus());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Gagal mendapatkan ID film baru.");
    }

    public boolean update(Film film) throws SQLException {
        String sql = """
                UPDATE films
                SET title = ?, genre = ?, duration_minutes = ?, age_rating = ?, synopsis = ?, status = ?
                WHERE id = ?
                """;
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, film.getTitle());
            stmt.setString(2, film.getGenre());
            stmt.setInt(3, film.getDurationMinutes());
            stmt.setString(4, film.getAgeRating());
            stmt.setString(5, film.getSynopsis());
            stmt.setString(6, film.getStatus());
            stmt.setLong(7, film.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM films WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isUsedBySchedule(long filmId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM schedules WHERE film_id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, filmId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean titleExistsForOther(String title, long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM films WHERE LOWER(title) = LOWER(?) AND id <> ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title == null ? "" : title.trim());
            stmt.setLong(2, excludeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Memastikan minimal ada film dan jadwal siap tayang untuk demo/layanan customer.
     * Menggunakan JDBC Transaction.
     */
    public void ensureDemoDataAvailable() {
        try (Connection conn = connectionProvider.getConnection()) {
            // Cek apakah tabel films kosong
            boolean hasFilms = false;
            try (Statement checkStmt = conn.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM films")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasFilms = true;
                }
            }
            if (hasFilms) {
                ensureUpcomingSchedulesAvailable(conn);
                return;
            }

            conn.setAutoCommit(false);
            try {
                // Pastikan ada studio
                long studioId = 1;
                try (Statement studioStmt = conn.createStatement();
                     ResultSet studioRs = studioStmt.executeQuery("SELECT id FROM studios LIMIT 1")) {
                    if (studioRs.next()) {
                        studioId = studioRs.getLong("id");
                    } else {
                        try (PreparedStatement insStudio = conn.prepareStatement(
                                "INSERT INTO studios (name, seat_rows, seat_cols) VALUES ('Studio 1 Premiere', 5, 8)",
                                Statement.RETURN_GENERATED_KEYS)) {
                            insStudio.executeUpdate();
                            try (ResultSet gen = insStudio.getGeneratedKeys()) {
                                if (gen.next()) {
                                    studioId = gen.getLong(1);
                                }
                            }
                        }
                    }
                }

                // Tambahkan beberapa film populer
                String insertFilmSql = """
                        INSERT INTO films (title, genre, duration_minutes, age_rating, synopsis, status)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;
                long film1Id;
                long film2Id;
                long film3Id;

                try (PreparedStatement filmStmt = conn.prepareStatement(insertFilmSql, Statement.RETURN_GENERATED_KEYS)) {
                    // Film 1
                    filmStmt.setString(1, "Dune: Part Two");
                    filmStmt.setString(2, "Action, Sci-Fi, Adventure");
                    filmStmt.setInt(3, 166);
                    filmStmt.setString(4, "13+");
                    filmStmt.setString(5, "Paul Atreides bersatu dengan Chani dan Fremen untuk membalas dendam terhadap konspirator yang menghancurkan keluarganya.");
                    filmStmt.setString(6, "NOW_SHOWING");
                    filmStmt.executeUpdate();
                    try (ResultSet k = filmStmt.getGeneratedKeys()) {
                        k.next();
                        film1Id = k.getLong(1);
                    }

                    // Film 2
                    filmStmt.setString(1, "Agak Laen");
                    filmStmt.setString(2, "Comedy, Horror");
                    filmStmt.setInt(3, 119);
                    filmStmt.setString(4, "13+");
                    filmStmt.setString(5, "Empat sekawan penjaga rumah hantu di pasar malam berusaha mencari cara baru menakuti pengunjung agar tidak bangkrut.");
                    filmStmt.setString(6, "NOW_SHOWING");
                    filmStmt.executeUpdate();
                    try (ResultSet k = filmStmt.getGeneratedKeys()) {
                        k.next();
                        film2Id = k.getLong(1);
                    }

                    // Film 3
                    filmStmt.setString(1, "Interstellar");
                    filmStmt.setString(2, "Sci-Fi, Drama");
                    filmStmt.setInt(3, 169);
                    filmStmt.setString(4, "SU");
                    filmStmt.setString(5, "Tim penjelajah melintasi lubang cacing di luar angkasa dalam upaya memastikan kelangsungan hidup umat manusia.");
                    filmStmt.setString(6, "NOW_SHOWING");
                    filmStmt.executeUpdate();
                    try (ResultSet k = filmStmt.getGeneratedKeys()) {
                        k.next();
                        film3Id = k.getLong(1);
                    }

                    // Film 4 (Coming soon)
                    filmStmt.setString(1, "Deadpool & Wolverine");
                    filmStmt.setString(2, "Action, Comedy, Superhero");
                    filmStmt.setInt(3, 128);
                    filmStmt.setString(4, "17+");
                    filmStmt.setString(5, "Wade Wilson yang lesu kembali beraksi saat TVA memanggilnya untuk misi multiverse berbahaya.");
                    filmStmt.setString(6, "COMING_SOON");
                    filmStmt.executeUpdate();
                }

                // Tambahkan jadwal hari ini dan besok
                LocalDate today = LocalDate.now();
                String insertSched = """
                        INSERT INTO schedules (film_id, studio_id, show_date, show_time, ticket_price, total_seats, available_seats)
                        VALUES (?, ?, ?, ?, ?, 40, 40)
                        """;
                try (PreparedStatement schedStmt = conn.prepareStatement(insertSched)) {
                    // Jadwal 1: Dune siang
                    schedStmt.setLong(1, film1Id);
                    schedStmt.setLong(2, studioId);
                    schedStmt.setDate(3, java.sql.Date.valueOf(today));
                    schedStmt.setTime(4, java.sql.Time.valueOf(LocalTime.of(13, 0)));
                    schedStmt.setBigDecimal(5, new java.math.BigDecimal("50000.00"));
                    schedStmt.executeUpdate();

                    // Jadwal 2: Dune sore
                    schedStmt.setLong(1, film1Id);
                    schedStmt.setLong(2, studioId);
                    schedStmt.setDate(3, java.sql.Date.valueOf(today));
                    schedStmt.setTime(4, java.sql.Time.valueOf(LocalTime.of(16, 30)));
                    schedStmt.setBigDecimal(5, new java.math.BigDecimal("50000.00"));
                    schedStmt.executeUpdate();

                    // Jadwal 3: Agak Laen malam
                    schedStmt.setLong(1, film2Id);
                    schedStmt.setLong(2, studioId);
                    schedStmt.setDate(3, java.sql.Date.valueOf(today));
                    schedStmt.setTime(4, java.sql.Time.valueOf(LocalTime.of(20, 0)));
                    schedStmt.setBigDecimal(5, new java.math.BigDecimal("45000.00"));
                    schedStmt.executeUpdate();

                    // Jadwal 4: Interstellar besok
                    schedStmt.setLong(1, film3Id);
                    schedStmt.setLong(2, studioId);
                    schedStmt.setDate(3, java.sql.Date.valueOf(today.plusDays(1)));
                    schedStmt.setTime(4, java.sql.Time.valueOf(LocalTime.of(14, 0)));
                    schedStmt.setBigDecimal(5, new java.math.BigDecimal("55000.00"));
                    schedStmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
            }
        } catch (SQLException ignored) {
        }
    }

    /**
     * Memastikan selalu ada jadwal tayang mendatang (hari ini & besok) untuk film NOW_SHOWING
     * apabila seluruh jadwal lama sudah lewat tanggalnya.
     */
    private void ensureUpcomingSchedulesAvailable(Connection conn) throws SQLException {
        String countUpcomingSql = """
                SELECT COUNT(*)
                FROM schedules
                WHERE show_date > CURDATE()
                   OR (show_date = CURDATE() AND show_time >= CURTIME())
                """;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countUpcomingSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Masih ada jadwal aktif mendatang
            }
        }

        List<Long> activeFilmIds = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM films WHERE status = 'NOW_SHOWING' ORDER BY id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                activeFilmIds.add(rs.getLong("id"));
            }
        }
        if (activeFilmIds.isEmpty()) {
            return;
        }

        List<long[]> studios = new ArrayList<>(); // [id, totalSeats]
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, (seat_rows * seat_cols) AS cap FROM studios ORDER BY id")) {
            while (rs.next()) {
                studios.add(new long[]{rs.getLong("id"), rs.getInt("cap")});
            }
        }
        if (studios.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalTime[] todayTimes = {LocalTime.of(16, 0), LocalTime.of(19, 30), LocalTime.of(21, 15)};
        LocalTime[] tomorrowTimes = {LocalTime.of(13, 30), LocalTime.of(17, 0), LocalTime.of(20, 0)};
        java.math.BigDecimal[] prices = {
                new java.math.BigDecimal("50000.00"),
                new java.math.BigDecimal("55000.00"),
                new java.math.BigDecimal("45000.00"),
                new java.math.BigDecimal("50000.00")
        };

        String insertSql = """
                INSERT IGNORE INTO schedules
                    (film_id, studio_id, show_date, show_time, ticket_price, total_seats, available_seats)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
            for (int i = 0; i < activeFilmIds.size(); i++) {
                long filmId = activeFilmIds.get(i);
                long[] studio = studios.get(i % studios.size());
                long studioId = studio[0];
                int cap = (int) studio[1];
                java.math.BigDecimal price = prices[i % prices.length];

                // Slot hari ini (sore/malam agar selalu aktif)
                LocalTime t1 = todayTimes[i % todayTimes.length].plusMinutes((i / todayTimes.length) * 30L);
                ins.setLong(1, filmId);
                ins.setLong(2, studioId);
                ins.setDate(3, java.sql.Date.valueOf(today));
                ins.setTime(4, java.sql.Time.valueOf(t1));
                ins.setBigDecimal(5, price);
                ins.setInt(6, cap);
                ins.setInt(7, cap);
                ins.executeUpdate();

                // Slot besok (siang & malam)
                LocalTime t2 = tomorrowTimes[i % tomorrowTimes.length].plusMinutes((i / tomorrowTimes.length) * 30L);
                ins.setLong(1, filmId);
                ins.setLong(2, studioId);
                ins.setDate(3, java.sql.Date.valueOf(tomorrow));
                ins.setTime(4, java.sql.Time.valueOf(t2));
                ins.setBigDecimal(5, price);
                ins.setInt(6, cap);
                ins.setInt(7, cap);
                ins.executeUpdate();
            }
        }
    }

    private Film mapFilm(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String title = rs.getString("title");
        String genre = rs.getString("genre");
        int duration = rs.getInt("duration_minutes");
        String rating = rs.getString("age_rating");
        String synopsis = rs.getString("synopsis");
        String poster = rs.getString("poster_path");
        String status = rs.getString("status");
        Timestamp createdAt = rs.getTimestamp("created_at");

        return new Film(
                id,
                title,
                genre,
                duration,
                rating,
                synopsis,
                poster,
                status,
                createdAt != null ? createdAt.toLocalDateTime() : null);
    }
}
