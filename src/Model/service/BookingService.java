package Model.service;

import Model.dao.BookingDao;
import Model.dao.BookingDao.PaymentSnapshot;
import Model.entity.Booking;
import Model.entity.BookingCancellationResult;
import Model.entity.BookingHistoryItem;
import Model.entity.BookingReceipt;
import Model.entity.DashboardSummary;
import Model.entity.Seat;
import Model.enums.BookingResolution;
import Model.enums.BookingStatus;
import Model.enums.PaymentMethod;
import Model.enums.Role;
import Model.exception.BookingAccessDeniedException;
import Model.exception.BookingNotCancellableException;
import Model.exception.BookingNotFoundException;
import Model.exception.InsufficientPaymentException;
import Model.exception.SeatAlreadyBookedException;
import Model.exception.ValidationException;
import Model.payment.Payment;
import Model.payment.PaymentFactory;
import Model.payment.PaymentResult;
import Controler.ConnectionProvider;
import Controler.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class BookingService {
    public static final int MAX_SEAT_PER_BOOKING = 6;
    public static final BigDecimal REFUND_PERCENT = BigDecimal.ONE;

    private static final DateTimeFormatter BOOKING_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final char[] CODE_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int BOOKING_CODE_ATTEMPTS = 10;

    private final ConnectionProvider connectionProvider;
    private final BookingDao bookingDao;
    private final Clock clock;
    private final SecureRandom secureRandom;

    public BookingService() {
        this(DBConnection.getInstance()::getConnection, new BookingDao(),
                Clock.system(ZoneId.of("Asia/Jakarta")), new SecureRandom());
    }

    public BookingService(ConnectionProvider connectionProvider, BookingDao bookingDao, Clock clock) {
        this(connectionProvider, bookingDao, clock, new SecureRandom());
    }

    BookingService(
            ConnectionProvider connectionProvider,
            BookingDao bookingDao,
            Clock clock,
            SecureRandom secureRandom) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider);
        this.bookingDao = Objects.requireNonNull(bookingDao);
        this.clock = Objects.requireNonNull(clock);
        this.secureRandom = Objects.requireNonNull(secureRandom);
    }

    public BookingReceipt createBooking(CreateBookingRequest request)
            throws SQLException, ValidationException, SeatAlreadyBookedException,
            InsufficientPaymentException {
        validateRequest(request);
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!bookingDao.userExists(connection, request.userId())) {
                    throw new ValidationException("Pengguna tidak ditemukan.");
                }

                BookingDao.ScheduleSnapshot schedule =
                        bookingDao.lockSchedule(connection, request.scheduleId());
                if (schedule == null) {
                    throw new ValidationException("Jadwal tayang tidak ditemukan.");
                }

                LocalDateTime showDateTime = LocalDateTime.of(schedule.showDate(), schedule.showTime());
                LocalDateTime now = LocalDateTime.now(clock);
                if (!showDateTime.isAfter(now)) {
                    throw new ValidationException("Jadwal tayang sudah dimulai atau terlewat.");
                }
                if (schedule.availableSeats() < request.seatCodes().size()) {
                    throw new ValidationException("Jumlah kursi tersedia tidak mencukupi.");
                }

                List<Seat> seats = validateSeats(
                        request.seatCodes(), request.scheduleId(),
                        schedule.seatRows(), schedule.seatColumns());
                BigDecimal totalPrice = schedule.ticketPrice()
                        .multiply(BigDecimal.valueOf(seats.size()))
                        .setScale(2, RoundingMode.UNNECESSARY);
                Payment payment = PaymentFactory.create(
                        request.paymentMethod(), totalPrice, request.cashReceived());
                PaymentResult paymentResult = payment.processPayment();

                Booking booking = insertBookingWithUniqueCode(
                        connection,
                        new Booking(null, generateBookingCode(now), request.userId(),
                                request.scheduleId(), BookingStatus.ACTIVE, seats.size(),
                                totalPrice, now, null));
                for (Seat seat : seats) {
                    try {
                        bookingDao.insertSeat(connection, booking.id(), seat);
                    } catch (SQLException exception) {
                        if (isDuplicateSeat(exception)) {
                            throw new SeatAlreadyBookedException(seat.seatCode());
                        }
                        throw exception;
                    }
                }
                bookingDao.insertPayment(connection, booking.id(), paymentResult);
                if (!bookingDao.decrementAvailableSeats(
                        connection, request.scheduleId(), seats.size())) {
                    throw new ValidationException("Kursi tersedia berubah. Silakan pilih ulang.");
                }
                connection.commit();
                return new BookingReceipt(booking, seats, paymentResult,
                        schedule.filmTitle(), schedule.studioName(), showDateTime);
            } catch (SQLException | ValidationException | SeatAlreadyBookedException
                     | InsufficientPaymentException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    public BookingCancellationResult cancelOrRefundBooking(
            String bookingCode, long actorUserId, Role actorRole, BookingResolution resolution)
            throws SQLException, BookingNotFoundException, BookingAccessDeniedException,
            BookingNotCancellableException, ValidationException {
        if (bookingCode == null || bookingCode.isBlank()) {
            throw new ValidationException("Kode booking wajib diisi.");
        }
        if (actorUserId <= 0 || actorRole == null || resolution == null) {
            throw new ValidationException("Identitas, peran, dan tindakan booking wajib tersedia.");
        }

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BookingDao.LockedBookingForResolution lockedBooking =
                        bookingDao.lockBookingForResolution(connection, bookingCode.trim());
                if (lockedBooking == null) {
                    throw new BookingNotFoundException(bookingCode);
                }
                Booking booking = lockedBooking.booking();
                if (actorRole != Role.ADMIN && booking.userId() != actorUserId) {
                    throw new BookingAccessDeniedException();
                }
                if (booking.status() != BookingStatus.ACTIVE) {
                    throw new BookingNotCancellableException(
                            "Booking sudah pernah dibatalkan atau di-refund.");
                }
                LocalDateTime now = LocalDateTime.now(clock);
                if (!lockedBooking.showDateTime().isAfter(now)) {
                    throw new BookingNotCancellableException(
                            "Booking tidak dapat dibatalkan setelah jadwal dimulai.");
                }

                PaymentSnapshot paymentSnapshot = lockedBooking.payment().orElseThrow(
                        () -> new BookingNotCancellableException(
                                "Data pembayaran untuk booking ini tidak ditemukan."));
                PaymentResult recordedPayment = validateRecordedPayment(paymentSnapshot, booking);
                BookingStatus resolvedStatus = resolution == BookingResolution.REFUND
                        ? BookingStatus.REFUNDED : BookingStatus.CANCELLED;
                BigDecimal refundedAmount = resolution == BookingResolution.REFUND
                        ? recordedPayment.amountPaid().multiply(REFUND_PERCENT) : BigDecimal.ZERO;
                if (!bookingDao.resolveBooking(connection, booking.id(), resolvedStatus, now)) {
                    throw new BookingNotCancellableException("Status booking sudah berubah.");
                }
                if (resolution == BookingResolution.REFUND
                        && !bookingDao.markPaymentRefunded(connection, booking.id(), now)) {
                    throw new SQLException("Status pembayaran tidak dapat diperbarui untuk refund.");
                }
                int deletedSeats = bookingDao.deleteBookingSeats(connection, booking.id());
                if (deletedSeats != booking.ticketCount()) {
                    throw new SQLException("Jumlah kursi booking tidak sesuai dengan data transaksi.");
                }
                if (!bookingDao.restoreAvailableSeats(
                        connection, booking.scheduleId(), booking.ticketCount())) {
                    throw new SQLException("Ketersediaan kursi tidak dapat dipulihkan.");
                }
                connection.commit();
                return new BookingCancellationResult(
                        booking.bookingCode(), resolvedStatus, refundedAmount);
            } catch (SQLException | BookingNotFoundException | BookingAccessDeniedException
                     | BookingNotCancellableException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    private PaymentResult validateRecordedPayment(
            PaymentSnapshot payment, Booking booking) throws BookingNotCancellableException {
        if (!"PAID".equals(payment.status())) {
            throw new BookingNotCancellableException(
                    "Booking hanya dapat diselesaikan jika pembayarannya berstatus PAID.");
        }
        if (payment.amountPaid() == null
                || payment.amountPaid().compareTo(booking.totalPrice()) != 0) {
            throw new BookingNotCancellableException(
                    "Nominal pembayaran tidak sama dengan total harga booking.");
        }
        try {
            return new PaymentResult(
                    payment.method(),
                    payment.amountPaid(),
                    payment.cashReceived(),
                    payment.changeAmount());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BookingNotCancellableException(
                    "Rincian pembayaran booking tidak konsisten.");
        }
    }

    public List<BookingHistoryItem> getBookingHistory(
            long actorUserId, long targetUserId, Role actorRole)
            throws SQLException, ValidationException, BookingAccessDeniedException {
        validateActor(actorUserId, actorRole);
        if (targetUserId <= 0) {
            throw new ValidationException("ID pengguna yang dicari harus positif.");
        }
        if (actorRole != Role.ADMIN && targetUserId != actorUserId) {
            throw new BookingAccessDeniedException();
        }
        return bookingDao.findHistoryByUser(targetUserId);
    }

    public Optional<BookingReceipt> getBookingDetails(
            String bookingCode, long actorUserId, Role actorRole)
            throws SQLException, ValidationException, BookingAccessDeniedException {
        if (bookingCode == null || bookingCode.isBlank()) {
            throw new ValidationException("Kode booking wajib diisi.");
        }
        validateActor(actorUserId, actorRole);
        Optional<BookingReceipt> receipt =
                bookingDao.findDetailsByCode(bookingCode.trim());
        if (receipt.isPresent() && actorRole != Role.ADMIN
                && receipt.get().booking().userId() != actorUserId) {
            throw new BookingAccessDeniedException();
        }
        return receipt;
    }

    public DashboardSummary getDashboardSummary(long actorUserId, Role actorRole)
            throws SQLException, ValidationException, BookingAccessDeniedException {
        validateActor(actorUserId, actorRole);
        if (actorRole != Role.ADMIN) {
            throw new BookingAccessDeniedException();
        }
        LocalDate today = LocalDate.now(clock);
        return bookingDao.getDashboardSummary(today);
    }

    private void validateActor(long actorUserId, Role actorRole) throws ValidationException {
        if (actorUserId <= 0 || actorRole == null) {
            throw new ValidationException("Identitas dan peran pengguna wajib tersedia.");
        }
    }

    private void validateRequest(CreateBookingRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("Data booking wajib diisi.");
        }
        if (request.userId() <= 0 || request.scheduleId() <= 0) {
            throw new ValidationException("ID pengguna dan jadwal harus positif.");
        }
        if (request.seatCodes() == null || request.seatCodes().isEmpty()
                || request.seatCodes().size() > MAX_SEAT_PER_BOOKING) {
            throw new ValidationException("Pilih antara 1 sampai 6 kursi.");
        }
        Set<String> uniqueSeats = new HashSet<>();
        for (String seatCode : request.seatCodes()) {
            if (!Seat.isValidCode(seatCode) || !uniqueSeats.add(seatCode)) {
                throw new ValidationException(
                        "Format kode kursi tidak valid atau kursi dipilih lebih dari sekali.");
            }
        }
        if (request.paymentMethod() == null) {
            throw new ValidationException("Metode pembayaran wajib dipilih.");
        }
        if (request.paymentMethod() == PaymentMethod.CASH && request.cashReceived() == null) {
            throw new ValidationException("Nominal uang tunai wajib diisi.");
        }
        if (request.paymentMethod() == PaymentMethod.QRIS && request.cashReceived() != null) {
            throw new ValidationException("Nominal uang tunai tidak berlaku untuk pembayaran QRIS.");
        }
        if (request.cashReceived() != null
                && (request.cashReceived().scale() > 2
                || request.cashReceived().precision() > 10
                || request.cashReceived().signum() < 0)) {
            throw new ValidationException("Nominal uang harus maksimal dua angka desimal.");
        }
    }

    private List<Seat> validateSeats(
            List<String> seatCodes, long scheduleId, int rowCount, int columnCount)
            throws ValidationException {
        List<Seat> seats = new ArrayList<>(seatCodes.size());
        for (String seatCode : seatCodes) {
            int row = rowNumber(seatCode);
            int column = seatColumn(seatCode);
            if (row < 1 || row > rowCount || column < 1 || column > columnCount) {
                throw new ValidationException("Kursi " + seatCode + " tidak tersedia di studio ini.");
            }
            seats.add(new Seat(scheduleId, seatCode));
        }
        return List.copyOf(seats);
    }

    private int rowNumber(String seatCode) throws ValidationException {
        int separator = 0;
        while (separator < seatCode.length()
                && seatCode.charAt(separator) >= 'A' && seatCode.charAt(separator) <= 'Z') {
            separator++;
        }
        if (separator == 0 || separator == seatCode.length()) {
            throw new ValidationException("Format kode kursi tidak valid: " + seatCode);
        }
        int row = 0;
        try {
            for (int index = 0; index < separator; index++) {
                row = Math.addExact(Math.multiplyExact(row, 26), seatCode.charAt(index) - 'A' + 1);
            }
        } catch (ArithmeticException exception) {
            throw new ValidationException("Baris kursi di luar batas: " + seatCode);
        }
        return row;
    }

    private int seatColumn(String seatCode) throws ValidationException {
        int separator = 0;
        while (separator < seatCode.length()
                && seatCode.charAt(separator) >= 'A' && seatCode.charAt(separator) <= 'Z') {
            separator++;
        }
        if (separator == 0 || separator == seatCode.length()) {
            throw new ValidationException("Format kode kursi tidak valid: " + seatCode);
        }
        try {
            return Integer.parseInt(seatCode.substring(separator));
        } catch (NumberFormatException exception) {
            throw new ValidationException("Kolom kursi di luar batas: " + seatCode);
        }
    }

    private Booking insertBookingWithUniqueCode(Connection connection, Booking booking)
            throws SQLException {
        for (int attempt = 0; attempt < BOOKING_CODE_ATTEMPTS; attempt++) {
            String candidate = attempt == 0 ? booking.bookingCode() : generateBookingCode(booking.createdAt());
            if (bookingDao.bookingCodeExists(connection, candidate)) {
                continue;
            }
            try {
                Booking candidateBooking = new Booking(
                        null, candidate, booking.userId(), booking.scheduleId(), booking.status(),
                        booking.ticketCount(), booking.totalPrice(), booking.createdAt(),
                        booking.cancelledAt());
                return bookingDao.insertBooking(connection, candidateBooking);
            } catch (SQLException exception) {
                if (isDuplicateBookingCode(exception)
                        && bookingDao.bookingCodeExists(connection, candidate)) {
                    continue;
                }
                throw exception;
            }
        }
        throw new SQLException("Tidak dapat membuat kode booking unik setelah beberapa percobaan.");
    }

    private String generateBookingCode(LocalDateTime createdAt) {
        StringBuilder suffix = new StringBuilder(4);
        for (int index = 0; index < 4; index++) {
            suffix.append(CODE_ALPHABET[secureRandom.nextInt(CODE_ALPHABET.length)]);
        }
        return "BKG-" + BOOKING_DATE_FORMAT.format(createdAt) + "-" + suffix;
    }

    private boolean isDuplicateSeat(SQLException exception) {
        return exception.getErrorCode() == 1062
                && exception.getMessage() != null
                && exception.getMessage().contains("uq_booking_seats_schedule_seat");
    }

    private boolean isDuplicateBookingCode(SQLException exception) {
        return exception.getErrorCode() == 1062
                && exception.getMessage() != null
                && exception.getMessage().contains("uq_bookings_code");
    }

    private void rollback(Connection connection, Exception originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
