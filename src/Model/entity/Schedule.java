package Model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Entitas jadwal tayang yang sesuai dengan tabel schedules di database.
 * filmTitle dan studioName adalah data denormalisasi untuk kemudahan tampilan.
 */
public final class Schedule {
    private final long id;
    private final long filmId;
    private final long studioId;
    private final LocalDate showDate;
    private final LocalTime showTime;
    private final BigDecimal ticketPrice;
    private final int totalSeats;
    private final int availableSeats;

    // Denormalized display fields (nullable saat insert, diisi saat query JOIN)
    private final String filmTitle;
    private final String studioName;

    public Schedule(
            long id,
            long filmId,
            long studioId,
            LocalDate showDate,
            LocalTime showTime,
            BigDecimal ticketPrice,
            int totalSeats,
            int availableSeats,
            String filmTitle,
            String studioName) {
        if (filmId <= 0) {
            throw new IllegalArgumentException("ID film harus positif.");
        }
        if (studioId <= 0) {
            throw new IllegalArgumentException("ID studio harus positif.");
        }
        if (ticketPrice == null || ticketPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Harga tiket harus lebih dari nol.");
        }
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("Total kursi harus lebih dari nol.");
        }
        if (availableSeats < 0 || availableSeats > totalSeats) {
            throw new IllegalArgumentException("Jumlah kursi tersedia tidak valid.");
        }
        this.id = id;
        this.filmId = filmId;
        this.studioId = studioId;
        this.showDate = Objects.requireNonNull(showDate, "Tanggal tayang wajib diisi.");
        this.showTime = Objects.requireNonNull(showTime, "Jam tayang wajib diisi.");
        this.ticketPrice = ticketPrice;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.filmTitle = filmTitle;
        this.studioName = studioName;
    }

    public long getId() {
        return id;
    }

    public long getFilmId() {
        return filmId;
    }

    public long getStudioId() {
        return studioId;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public LocalTime getShowTime() {
        return showTime;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public String getFilmTitle() {
        return filmTitle;
    }

    public String getStudioName() {
        return studioName;
    }

    @Override
    public String toString() {
        return (filmTitle != null ? filmTitle : "Film #" + filmId)
                + " — " + showDate + " " + showTime
                + " (" + (studioName != null ? studioName : "Studio #" + studioId) + ")";
    }
}
