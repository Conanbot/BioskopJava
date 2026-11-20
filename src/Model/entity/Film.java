package Model.entity;

import Model.interfaces.Displayable;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entitas Film yang merepresentasikan data dari tabel films.
 * Menerapkan prinsip Enkapsulasi, Constructor Overloading, dan Interface Displayable.
 */
public class Film implements Displayable {
    private final long id;
    private final String title;
    private final String genre;
    private final int durationMinutes;
    private final String ageRating;
    private final String synopsis;
    private final String posterPath;
    private final String status; // NOW_SHOWING, COMING_SOON, ARCHIVED
    private final LocalDateTime createdAt;

    /**
     * Constructor utama (Lengkap).
     */
    public Film(
            long id,
            String title,
            String genre,
            int durationMinutes,
            String ageRating,
            String synopsis,
            String posterPath,
            String status,
            LocalDateTime createdAt) {
        this.id = id;
        this.title = Objects.requireNonNull(title, "Judul film tidak boleh null.").trim();
        this.genre = Objects.requireNonNull(genre, "Genre film tidak boleh null.").trim();
        this.durationMinutes = durationMinutes;
        this.ageRating = Objects.requireNonNull(ageRating, "Rating usia tidak boleh null.").trim();
        this.synopsis = synopsis != null ? synopsis.trim() : "";
        this.posterPath = posterPath;
        this.status = Objects.requireNonNull(status, "Status film tidak boleh null.").trim();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();

        if (this.durationMinutes <= 0) {
            throw new IllegalArgumentException("Durasi film harus lebih dari 0 menit.");
        }
    }

    /**
     * Constructor Overloading (Sederhana tanpa poster & createdAt).
     */
    public Film(
            long id,
            String title,
            String genre,
            int durationMinutes,
            String ageRating,
            String synopsis,
            String status) {
        this(id, title, genre, durationMinutes, ageRating, synopsis, null, status, LocalDateTime.now());
    }

    public long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getGenre() {
        return this.genre;
    }

    public int getDurationMinutes() {
        return this.durationMinutes;
    }

    public String getAgeRating() {
        return this.ageRating;
    }

    public String getSynopsis() {
        return this.synopsis;
    }

    public String getPosterPath() {
        return this.posterPath;
    }

    public String getStatus() {
        return this.status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Implementasi Interface Displayable (Polymorphism).
     */
    @Override
    public String getDisplayText() {
        return this.title + " (" + this.ageRating + ") - " + this.durationMinutes + " Menit";
    }

    /**
     * Method Overloading: Memeriksa apakah umur penonton sesuai dengan rating film.
     * Menggunakan dasar pemrograman kondisi (if - else).
     */
    public boolean isSuitableForAge(int userAge) {
        if ("SU".equalsIgnoreCase(this.ageRating)) {
            return true;
        } else if ("13+".equalsIgnoreCase(this.ageRating)) {
            return userAge >= 13;
        } else if ("17+".equalsIgnoreCase(this.ageRating) || "D".equalsIgnoreCase(this.ageRating)) {
            return userAge >= 17;
        } else if ("21+".equalsIgnoreCase(this.ageRating)) {
            return userAge >= 21;
        }
        return true;
    }

    /**
     * Method Overloading: Memeriksa kesesuaian kategori usia berdasarkan string role/rating.
     */
    public boolean isSuitableForAge(String ratingCategory) {
        if (ratingCategory == null || ratingCategory.isBlank()) {
            return true;
        }
        return this.ageRating.equalsIgnoreCase(ratingCategory.trim());
    }

    @Override
    public String toString() {
        return getDisplayText();
    }
}
