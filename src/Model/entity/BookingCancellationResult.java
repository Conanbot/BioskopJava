package Model.entity;

import Model.enums.BookingStatus;

import java.math.BigDecimal;
import java.util.Objects;

public record BookingCancellationResult(
        String bookingCode,
        BookingStatus status,
        BigDecimal refundedAmount) {

    public BookingCancellationResult {
        if (bookingCode == null || bookingCode.isBlank()) {
            throw new IllegalArgumentException("Kode booking wajib diisi.");
        }
        Objects.requireNonNull(status, "Status booking wajib diisi.");
        Objects.requireNonNull(refundedAmount, "Nominal refund wajib diisi.");
        if (refundedAmount.signum() < 0) {
            throw new IllegalArgumentException("Nominal refund tidak boleh negatif.");
        }
    }
}
