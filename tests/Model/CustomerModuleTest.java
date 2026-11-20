package Model;

import Model.entity.Booking;
import Model.entity.BookingReceipt;
import Model.entity.Film;
import Model.entity.Seat;
import Model.enums.BookingStatus;
import Model.enums.PaymentMethod;
import Model.interfaces.Displayable;
import Model.payment.PaymentResult;
import Model.service.TicketExportService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Customer Module & OOP Syllabus Tests")
class CustomerModuleTest {

    @Test
    @DisplayName("Film Encapsulation dan Constructor Overloading")
    void testFilmEncapsulationAndConstructors() {
        // Constructor overloading 1 (Sederhana)
        Film film1 = new Film(1L, "Dune: Part Two", "Sci-Fi", 166, "13+", "Sinopsis Dune", "NOW_SHOWING");
        assertEquals(1L, film1.getId());
        assertEquals("Dune: Part Two", film1.getTitle());
        assertEquals("Sci-Fi", film1.getGenre());
        assertEquals(166, film1.getDurationMinutes());
        assertEquals("13+", film1.getAgeRating());
        assertEquals("NOW_SHOWING", film1.getStatus());

        // Constructor overloading 2 (Lengkap dengan poster & custom timestamp)
        LocalDateTime now = LocalDateTime.now();
        Film film2 = new Film(2L, "Agak Laen", "Comedy", 119, "13+", "Sinopsis Agak Laen", "/posters/agak.jpg", "NOW_SHOWING", now);
        assertEquals("/posters/agak.jpg", film2.getPosterPath());
        assertEquals(now, film2.getCreatedAt());

        // Exception check: durasi <= 0
        assertThrows(IllegalArgumentException.class, () ->
                new Film(3L, "Judul", "Genre", 0, "SU", "Sinopsis", "NOW_SHOWING"));
    }

    @Test
    @DisplayName("Polymorphism via Interface Displayable")
    void testDisplayablePolymorphism() {
        Film film = new Film(1L, "Interstellar", "Sci-Fi", 169, "SU", "Luar angkasa", "NOW_SHOWING");
        assertTrue(film instanceof Displayable);

        Displayable displayable = film;
        assertEquals("Interstellar (SU) - 169 Menit", displayable.getDisplayText());
    }

    @Test
    @DisplayName("Method Overloading dan Kondisi Usia pada Film")
    void testAgeRatingOverloading() {
        Film filmSU = new Film(1L, "Animasi", "Animation", 90, "SU", "", "NOW_SHOWING");
        Film film13 = new Film(2L, "Dune", "Sci-Fi", 166, "13+", "", "NOW_SHOWING");
        Film film17 = new Film(3L, "Deadpool", "Action", 120, "17+", "", "NOW_SHOWING");

        // Overloading 1: parameter int age (Kondisi percabangan)
        assertTrue(filmSU.isSuitableForAge(10));
        assertFalse(film13.isSuitableForAge(10));
        assertTrue(film13.isSuitableForAge(15));
        assertFalse(film17.isSuitableForAge(15));
        assertTrue(film17.isSuitableForAge(18));

        // Overloading 2: parameter String category
        assertTrue(film13.isSuitableForAge("13+"));
        assertFalse(film13.isSuitableForAge("17+"));
    }

    @Test
    @DisplayName("TicketExportService String Formatting dan Stream API")
    void testTicketFormattingWithStreams() {
        Booking booking = new Booking(
                10L, "BK-TEST-1234", 1L, 5L, BookingStatus.ACTIVE, 2,
                new BigDecimal("100000.00"), LocalDateTime.now(), null);

        List<Seat> seats = List.of(
                new Seat(5L, "B2"),
                new Seat(5L, "B1")
        );

        PaymentResult payment = new PaymentResult(
                PaymentMethod.CASH,
                new BigDecimal("100000.00"),
                new BigDecimal("100000.00"),
                BigDecimal.ZERO);

        BookingReceipt receipt = new BookingReceipt(
                booking, seats, payment, "Dune: Part Two", "Studio 1 Premiere",
                LocalDateTime.of(2026, 11, 20, 19, 30));

        TicketExportService exportService = new TicketExportService();
        String ticketText = exportService.generateTicketText(receipt);

        assertNotNull(ticketText);
        assertTrue(ticketText.contains("BK-TEST-1234"));
        assertTrue(ticketText.contains("Dune: Part Two"));
        assertTrue(ticketText.contains("Studio 1 Premiere"));
        // Memastikan Stream API mengurutkan B1, B2
        assertTrue(ticketText.contains("B1, B2"));
        assertTrue(ticketText.contains("CASH"));
    }

    @Test
    @DisplayName("TicketExportService File I/O ke Disk")
    void testTicketExportToFile(@TempDir Path tempDir) throws IOException {
        Booking booking = new Booking(
                11L, "BK-FILE-9999", 1L, 5L, BookingStatus.ACTIVE, 1,
                new BigDecimal("50000.00"), LocalDateTime.now(), null);

        List<Seat> seats = List.of(new Seat(5L, "C3"));

        PaymentResult payment = new PaymentResult(
                PaymentMethod.QRIS,
                new BigDecimal("50000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO);

        BookingReceipt receipt = new BookingReceipt(
                booking, seats, payment, "Interstellar", "Studio IMAX",
                LocalDateTime.of(2026, 11, 21, 14, 0));

        TicketExportService exportService = new TicketExportService();
        File exportedFile = exportService.exportToFile(receipt, tempDir);

        assertTrue(exportedFile.exists());
        assertTrue(exportedFile.getName().contains("BK-FILE-9999"));

        String content = Files.readString(exportedFile.toPath());
        assertTrue(content.contains("BK-FILE-9999"));
        assertTrue(content.contains("Interstellar"));
        assertTrue(content.contains("C3"));
    }
}
