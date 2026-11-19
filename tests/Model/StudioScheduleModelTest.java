package Model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Model Domain Studio & Schedule")
class StudioScheduleModelTest {

    @Test
    @DisplayName("Studio menghitung kapasitas total dengan benar")
    void testStudioCapacity() {
        Studio studio = new Studio(1L, "Studio 1 Premiere", 8, 12);
        assertEquals(1L, studio.getId());
        assertEquals("Studio 1 Premiere", studio.getName());
        assertEquals(8, studio.getSeatRows());
        assertEquals(12, studio.getSeatCols());
        assertEquals(96, studio.getTotalCapacity());
    }

    @Test
    @DisplayName("Studio menolak nama kosong atau dimensi tidak positif")
    void testStudioValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Studio(1L, "", 8, 12));
        assertThrows(IllegalArgumentException.class, () -> new Studio(1L, "Studio A", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Studio(1L, "Studio A", 5, -1));
    }

    @Test
    @DisplayName("Schedule validasi nilai harga dan kapasitas")
    void testScheduleValidation() {
        LocalDate date = LocalDate.of(2026, 12, 1);
        LocalTime time = LocalTime.of(14, 30);

        // Harga tidak boleh <= 0
        assertThrows(IllegalArgumentException.class, () ->
                new Schedule(1L, 1L, 1L, date, time, BigDecimal.ZERO, 50, 50, "Film", "Studio"));

        // Kursi tersedia tidak boleh melebihi total kursi
        assertThrows(IllegalArgumentException.class, () ->
                new Schedule(1L, 1L, 1L, date, time, new BigDecimal("50000"), 50, 60, "Film", "Studio"));

        // Valid schedule
        Schedule sched = new Schedule(1L, 2L, 3L, date, time, new BigDecimal("45000"), 80, 80, "Inception", "Studio IMAX");
        assertEquals(1L, sched.getId());
        assertEquals(2L, sched.getFilmId());
        assertEquals(3L, sched.getStudioId());
        assertEquals(date, sched.getShowDate());
        assertEquals(time, sched.getShowTime());
        assertEquals(new BigDecimal("45000"), sched.getTicketPrice());
        assertEquals(80, sched.getTotalSeats());
        assertEquals(80, sched.getAvailableSeats());
        assertEquals("Inception", sched.getFilmTitle());
        assertEquals("Studio IMAX", sched.getStudioName());
    }
}
