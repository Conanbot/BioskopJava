package Model.entity;

import Model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record BookingHistoryItem(
        Booking booking,
        String filmTitle,
        String studioName,
        LocalDateTime showDateTime,
        PaymentMethod paymentMethod) {

    public BookingHistoryItem {
        Objects.requireNonNull(booking, "Data booking wajib diisi.");
        Objects.requireNonNull(filmTitle, "Judul film wajib diisi.");
        Objects.requireNonNull(studioName, "Nama studio wajib diisi.");
        Objects.requireNonNull(showDateTime, "Waktu tayang wajib diisi.");
        Objects.requireNonNull(paymentMethod, "Metode pembayaran wajib diisi.");
    }

    public BigDecimal totalPrice() {
        return booking.totalPrice();
    }
}
