package Model.entity;

import Model.payment.PaymentResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record BookingReceipt(
        Booking booking,
        List<Seat> seats,
        PaymentResult payment,
        String filmTitle,
        String studioName,
        LocalDateTime showDateTime) {

    public BookingReceipt {
        Objects.requireNonNull(booking, "Data booking wajib diisi.");
        seats = List.copyOf(Objects.requireNonNull(seats, "Daftar kursi wajib diisi."));
        Objects.requireNonNull(payment, "Data pembayaran wajib diisi.");
        Objects.requireNonNull(filmTitle, "Judul film wajib diisi.");
        Objects.requireNonNull(studioName, "Nama studio wajib diisi.");
        Objects.requireNonNull(showDateTime, "Waktu tayang wajib diisi.");
    }
}
