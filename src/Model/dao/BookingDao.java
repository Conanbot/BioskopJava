package Model.dao;

import Model.entity.Booking;
import Model.entity.BookingHistoryItem;
import Model.entity.BookingReceipt;
import Model.entity.DashboardSummary;
import Model.entity.Seat;
import Model.enums.BookingStatus;
import Model.enums.PaymentMethod;
import Model.payment.PaymentResult;
import Controler.ConnectionProvider;
import Controler.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BookingDao {
    private final ConnectionProvider connectionProvider;

    public BookingDao() {
        this(DBConnection.getInstance()::getConnection);
    }

    public BookingDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider);
    }

    public ScheduleSnapshot lockSchedule(Connection connection, long scheduleId) throws SQLException {
        String sql = """
                SELECT film_id, studio_id, ticket_price, available_seats, show_date, show_time
                FROM schedules
                WHERE id = ?
                FOR UPDATE
                """;
        long filmId;
        long studioId;
        BigDecimal ticketPrice;
        int availableSeats;
        LocalDate showDate;
        LocalTime showTime;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, scheduleId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                filmId = result.getLong("film_id");
                studioId = result.getLong("studio_id");
                ticketPrice = result.getBigDecimal("ticket_price");
                availableSeats = result.getInt("available_seats");
                showDate = result.getDate("show_date").toLocalDate();
                showTime = result.getTime("show_time").toLocalTime();
            }
        }

        String detailsSql = """
                SELECT st.seat_rows, st.seat_cols, f.title, st.name
                FROM studios st
                JOIN films f ON f.id = ?
                WHERE st.id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(detailsSql)) {
            statement.setLong(1, filmId);
            statement.setLong(2, studioId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Film atau studio jadwal tidak ditemukan.");
                }
                return new ScheduleSnapshot(
                        ticketPrice,
                        availableSeats,
                        showDate,
                        showTime,
                        result.getInt("seat_rows"),
                        result.getInt("seat_cols"),
                        result.getString("title"),
                        result.getString("name"));
            }
        }
    }

    public boolean userExists(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT 1 FROM users WHERE id = ?")) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean bookingCodeExists(Connection connection, String bookingCode) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT 1 FROM bookings WHERE booking_code = ?")) {
            statement.setString(1, bookingCode);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public Booking insertBooking(Connection connection, Booking booking) throws SQLException {
        String sql = """
                INSERT INTO bookings
                    (booking_code, user_id, schedule_id, status, ticket_count, total_price, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, booking.bookingCode());
            statement.setLong(2, booking.userId());
            statement.setLong(3, booking.scheduleId());
            statement.setString(4, booking.status().name());
            statement.setInt(5, booking.ticketCount());
            statement.setBigDecimal(6, booking.totalPrice());
            statement.setTimestamp(7, Timestamp.valueOf(booking.createdAt()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database tidak mengembalikan ID booking.");
                }
                return booking.withId(keys.getLong(1));
            }
        }
    }

    public void insertSeat(Connection connection, long bookingId, Seat seat) throws SQLException {
        String sql = """
                INSERT INTO booking_seats (booking_id, schedule_id, seat_code)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookingId);
            statement.setLong(2, seat.scheduleId());
            statement.setString(3, seat.seatCode());
            statement.executeUpdate();
        }
    }

    public void insertPayment(Connection connection, long bookingId, PaymentResult payment)
            throws SQLException {
        String sql = """
                INSERT INTO payments
                    (booking_id, method, status, amount_paid, cash_received, change_amount)
                VALUES (?, ?, 'PAID', ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookingId);
            statement.setString(2, payment.method().name());
            statement.setBigDecimal(3, payment.amountPaid());
            statement.setBigDecimal(4, payment.cashReceived());
            statement.setBigDecimal(5, payment.changeAmount());
            statement.executeUpdate();
        }
    }

    public boolean decrementAvailableSeats(Connection connection, long scheduleId, int count)
            throws SQLException {
        String sql = """
                UPDATE schedules
                SET available_seats = available_seats - ?
                WHERE id = ? AND available_seats >= ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, count);
            statement.setLong(2, scheduleId);
            statement.setInt(3, count);
            return statement.executeUpdate() == 1;
        }
    }

    public LockedBooking lockBookingByCode(Connection connection, String bookingCode)
            throws SQLException {
        String sql = """
                SELECT b.id, b.booking_code, b.user_id, b.schedule_id, b.status,
                       b.ticket_count, b.total_price, b.created_at, b.cancelled_at,
                       s.show_date, s.show_time, p.method, f.title, st.name
                FROM bookings b
                JOIN schedules s ON s.id = b.schedule_id
                JOIN films f ON f.id = s.film_id
                JOIN studios st ON st.id = s.studio_id
                JOIN payments p ON p.booking_id = b.id
                WHERE b.booking_code = ?
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bookingCode);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                Booking booking = mapBooking(result);
                LocalDateTime showDateTime = LocalDateTime.of(
                        result.getDate("show_date").toLocalDate(),
                        result.getTime("show_time").toLocalTime());
                return new LockedBooking(
                        booking,
                        showDateTime,
                        PaymentMethod.valueOf(result.getString("method")),
                        result.getString("title"),
                        result.getString("name"));
            }
        }
    }

    public LockedBookingForResolution lockBookingForResolution(
            Connection connection, String bookingCode) throws SQLException {
        String sql = """
                SELECT b.id, b.booking_code, b.user_id, b.schedule_id, b.status,
                       b.ticket_count, b.total_price, b.created_at, b.cancelled_at,
                       s.show_date, s.show_time,
                       p.booking_id AS payment_booking_id, p.method, p.status AS payment_status,
                       p.amount_paid, p.cash_received, p.change_amount
                FROM bookings b
                JOIN schedules s ON s.id = b.schedule_id
                LEFT JOIN payments p ON p.booking_id = b.id
                WHERE b.booking_code = ?
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bookingCode);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                Booking booking = mapBooking(result);
                LocalDateTime showDateTime = LocalDateTime.of(
                        result.getDate("show_date").toLocalDate(),
                        result.getTime("show_time").toLocalTime());
                Optional<PaymentSnapshot> payment = Optional.empty();
                if (result.getObject("payment_booking_id") != null) {
                    PaymentMethod method = PaymentMethod.valueOf(result.getString("method"));
                    BigDecimal cashReceived = result.getBigDecimal("cash_received");
                    if (method == PaymentMethod.QRIS && cashReceived == null) {
                        cashReceived = BigDecimal.ZERO;
                    }
                    payment = Optional.of(new PaymentSnapshot(
                            method,
                            result.getString("payment_status"),
                            result.getBigDecimal("amount_paid"),
                            cashReceived,
                            result.getBigDecimal("change_amount")));
                }
                return new LockedBookingForResolution(booking, showDateTime, payment);
            }
        }
    }

    public boolean resolveBooking(
            Connection connection,
            long bookingId,
            BookingStatus status,
            LocalDateTime resolvedAt) throws SQLException {
        if (status != BookingStatus.CANCELLED && status != BookingStatus.REFUNDED) {
            throw new IllegalArgumentException("Status penyelesaian booking tidak valid.");
        }
        String sql = """
                UPDATE bookings
                SET status = ?, cancelled_at = ?
                WHERE id = ? AND status = 'ACTIVE'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setTimestamp(2, Timestamp.valueOf(resolvedAt));
            statement.setLong(3, bookingId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean markPaymentRefunded(Connection connection, long bookingId, LocalDateTime refundTime)
            throws SQLException {
        String sql = """
                UPDATE payments
                SET status = 'REFUNDED', refunded_at = ?
                WHERE booking_id = ? AND status = 'PAID'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(refundTime));
            statement.setLong(2, bookingId);
            return statement.executeUpdate() == 1;
        }
    }

    public int deleteBookingSeats(Connection connection, long bookingId) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM booking_seats WHERE booking_id = ?")) {
            statement.setLong(1, bookingId);
            return statement.executeUpdate();
        }
    }

    public boolean restoreAvailableSeats(Connection connection, long scheduleId, int count)
            throws SQLException {
        String sql = """
                UPDATE schedules
                SET available_seats = available_seats + ?
                WHERE id = ? AND available_seats + ? <= total_seats
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, count);
            statement.setLong(2, scheduleId);
            statement.setInt(3, count);
            return statement.executeUpdate() == 1;
        }
    }

    public Optional<BookingReceipt> findDetailsByCode(String bookingCode) throws SQLException {
        String sql = """
                SELECT b.id, b.booking_code, b.user_id, b.schedule_id, b.status,
                       b.ticket_count, b.total_price, b.created_at, b.cancelled_at,
                       s.show_date, s.show_time, f.title, st.name,
                       p.method, p.amount_paid, p.cash_received, p.change_amount
                FROM bookings b
                JOIN schedules s ON s.id = b.schedule_id
                JOIN films f ON f.id = s.film_id
                JOIN studios st ON st.id = s.studio_id
                JOIN payments p ON p.booking_id = b.id
                WHERE b.booking_code = ?
                """;
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bookingCode);
            Booking booking;
            LocalDateTime showDateTime;
            PaymentResult payment;
            String filmTitle;
            String studioName;
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                booking = mapBooking(result);
                showDateTime = LocalDateTime.of(
                        result.getDate("show_date").toLocalDate(),
                        result.getTime("show_time").toLocalTime());
                payment = new PaymentResult(
                        PaymentMethod.valueOf(result.getString("method")),
                        result.getBigDecimal("amount_paid"),
                        cashReceived(result),
                        result.getBigDecimal("change_amount"));
                filmTitle = result.getString("title");
                studioName = result.getString("name");
            }
            List<Seat> seats = findSeats(connection, booking);
            return Optional.of(new BookingReceipt(
                    booking, seats, payment, filmTitle, studioName, showDateTime));
        }
    }

    public List<BookingHistoryItem> findHistoryByUser(long userId) throws SQLException {
        String sql = """
                SELECT b.id, b.booking_code, b.user_id, b.schedule_id, b.status,
                       b.ticket_count, b.total_price, b.created_at, b.cancelled_at,
                       s.show_date, s.show_time, f.title, st.name, p.method
                FROM bookings b
                JOIN schedules s ON s.id = b.schedule_id
                JOIN films f ON f.id = s.film_id
                JOIN studios st ON st.id = s.studio_id
                JOIN payments p ON p.booking_id = b.id
                WHERE b.user_id = ?
                ORDER BY b.created_at DESC, b.id DESC
                """;
        List<BookingHistoryItem> history = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Booking booking = mapBooking(result);
                    LocalDateTime showDateTime = LocalDateTime.of(
                            result.getDate("show_date").toLocalDate(),
                            result.getTime("show_time").toLocalTime());
                    history.add(new BookingHistoryItem(
                            booking,
                            result.getString("title"),
                            result.getString("name"),
                            showDateTime,
                            PaymentMethod.valueOf(result.getString("method"))));
                }
            }
        }
        return List.copyOf(history);
    }

    public DashboardSummary getDashboardSummary() throws SQLException {
        return getDashboardSummary(LocalDate.now(java.time.ZoneId.of("Asia/Jakarta")));
    }

    public DashboardSummary getDashboardSummary(LocalDate today) throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM films) AS total_films,
                    (SELECT COUNT(*) FROM schedules WHERE show_date = ?) AS total_schedules_today,
                    (SELECT COUNT(*) FROM bookings) AS total_bookings,
                    (SELECT COALESCE(SUM(amount_paid), 0) FROM payments WHERE status = 'PAID') AS total_revenue
                """;
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(today));
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return new DashboardSummary(0L, 0L, 0L, BigDecimal.ZERO);
                }
                return new DashboardSummary(
                        result.getLong("total_films"),
                        result.getLong("total_schedules_today"),
                        result.getLong("total_bookings"),
                        result.getBigDecimal("total_revenue"));
            }
        }
    }

    private List<Seat> findSeats(Connection connection, Booking booking) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = """
                SELECT schedule_id, seat_code
                FROM booking_seats
                WHERE booking_id = ?
                ORDER BY seat_code
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, booking.id());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    seats.add(new Seat(result.getLong("schedule_id"), result.getString("seat_code")));
                }
            }
        }
        return List.copyOf(seats);
    }

    private Booking mapBooking(ResultSet result) throws SQLException {
        Timestamp cancelledAt = result.getTimestamp("cancelled_at");
        return new Booking(
                result.getLong("id"),
                result.getString("booking_code"),
                result.getLong("user_id"),
                result.getLong("schedule_id"),
                BookingStatus.valueOf(result.getString("status")),
                result.getInt("ticket_count"),
                result.getBigDecimal("total_price"),
                result.getTimestamp("created_at").toLocalDateTime(),
                cancelledAt == null ? null : cancelledAt.toLocalDateTime());
    }

    private BigDecimal cashReceived(ResultSet result) throws SQLException {
        PaymentMethod method = PaymentMethod.valueOf(result.getString("method"));
        BigDecimal cashReceived = result.getBigDecimal("cash_received");
        return method == PaymentMethod.QRIS && cashReceived == null
                ? BigDecimal.ZERO
                : cashReceived;
    }

    public record PaymentSnapshot(
            PaymentMethod method,
            String status,
            BigDecimal amountPaid,
            BigDecimal cashReceived,
            BigDecimal changeAmount) {
    }

    public record LockedBookingForResolution(
            Booking booking,
            LocalDateTime showDateTime,
            Optional<PaymentSnapshot> payment) {
        public LockedBookingForResolution {
            Objects.requireNonNull(booking);
            Objects.requireNonNull(showDateTime);
            Objects.requireNonNull(payment);
        }
    }

    public record ScheduleSnapshot(
            BigDecimal ticketPrice,
            int availableSeats,
            LocalDate showDate,
            LocalTime showTime,
            int seatRows,
            int seatColumns,
            String filmTitle,
            String studioName) {
    }

    public record LockedBooking(
            Booking booking,
            LocalDateTime showDateTime,
            PaymentMethod paymentMethod,
            String filmTitle,
            String studioName) {
    }
}
