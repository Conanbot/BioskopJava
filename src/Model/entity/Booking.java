package Model.entity;

import Model.enums.BookingStatus;
import Model.interfaces.Displayable;
import Model.interfaces.Refundable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record Booking(
        Long id,
        String bookingCode,
        long userId,
        long scheduleId,
        BookingStatus status,
        int ticketCount,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt) implements Displayable, Refundable {

    public Booking {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("ID booking harus positif.");
        }
        if (bookingCode == null || bookingCode.isBlank()) {
            throw new IllegalArgumentException("Kode booking wajib diisi.");
        }
        if (userId <= 0 || scheduleId <= 0) {
            throw new IllegalArgumentException("ID pengguna dan jadwal harus positif.");
        }
        Objects.requireNonNull(status, "Status booking wajib diisi.");
        if (ticketCount < 1 || ticketCount > 6) {
            throw new IllegalArgumentException("Jumlah tiket harus antara 1 dan 6.");
        }
        Objects.requireNonNull(totalPrice, "Total harga wajib diisi.");
        if (totalPrice.signum() <= 0) {
            throw new IllegalArgumentException("Total harga harus lebih dari nol.");
        }
        Objects.requireNonNull(createdAt, "Waktu booking wajib diisi.");
    }

    public Booking withId(long generatedId) {
        return new Booking(generatedId, bookingCode, userId, scheduleId, status, ticketCount,
                totalPrice, createdAt, cancelledAt);
    }

    public Booking refundedAt(LocalDateTime refundTime) {
        return new Booking(id, bookingCode, userId, scheduleId, BookingStatus.REFUNDED,
                ticketCount, totalPrice, createdAt, Objects.requireNonNull(refundTime));
    }

    @Override
    public String getDisplayText() {
        return bookingCode + " | " + status + " | " + ticketCount + " tiket | Rp " + totalPrice;
    }

    @Override
    public BigDecimal calculateRefund(BigDecimal refundPercent) {
        Objects.requireNonNull(refundPercent, "Persentase refund wajib diisi.");
        if (refundPercent.signum() < 0 || refundPercent.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Persentase refund harus antara 0 dan 1.");
        }
        return totalPrice.multiply(refundPercent);
    }
}
