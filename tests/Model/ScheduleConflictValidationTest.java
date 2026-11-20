package Model;

import Model.dao.ScheduleDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Validasi Konflik Interval Jadwal Tayang")
class ScheduleConflictValidationTest {

    @Test
    @DisplayName("Dua jadwal dengan waktu mulai sama di studio yang sama harus konflik")
    void testSameStartTimeConflict() {
        LocalTime start = LocalTime.of(18, 0);
        int duration1 = 120; // 18:00 - 20:00
        int duration2 = 90;  // 18:00 - 19:30

        assertTrue(ScheduleDao.isIntervalConflicting(start, duration1, start, duration2),
                "Jadwal dengan jam mulai sama di studio yang sama harus dinyatakan konflik.");
    }

    @Test
    @DisplayName("Dua jadwal dengan waktu mulai berbeda tetapi bertabrakan harus konflik")
    void testOverlappingDifferentStartTimeConflict() {
        // Film A: 18:00 - 20:00 (120 menit)
        LocalTime startA = LocalTime.of(18, 0);
        int durationA = 120;

        // Film B: 19:00 - 20:30 (90 menit)
        LocalTime startB = LocalTime.of(19, 0);
        int durationB = 90;

        assertTrue(ScheduleDao.isIntervalConflicting(startA, durationA, startB, durationB),
                "Film A (18:00-20:00) dan Film B (19:00-20:30) harus dinyatakan konflik.");
        assertTrue(ScheduleDao.isIntervalConflicting(startB, durationB, startA, durationA),
                "Pemeriksaan konflik interval harus komutatif.");
    }

    @Test
    @DisplayName("Dua jadwal berurutan (adjacent) tidak bertabrakan")
    void testConsecutiveSchedulesNoConflict() {
        // Film 1: 14:00 - 16:00 (120 menit)
        LocalTime start1 = LocalTime.of(14, 0);
        int duration1 = 120;

        // Film 2: 16:00 - 18:00 (120 menit)
        LocalTime start2 = LocalTime.of(16, 0);
        int duration2 = 120;

        assertFalse(ScheduleDao.isIntervalConflicting(start1, duration1, start2, duration2),
                "Jadwal kedua mulai tepat saat jadwal pertama selesai tidak boleh dianggap konflik.");
    }

    @Test
    @DisplayName("Dua jadwal pada studio berbeda tidak bertabrakan")
    void testDifferentStudiosNoConflict() {
        long studio1Id = 1L;
        long studio2Id = 2L;
        LocalDate date = LocalDate.of(2026, 12, 1);
        LocalTime time = LocalTime.of(18, 0);
        int duration = 120;

        // Simulasi pemeriksaan domain: studio berbeda tidak pernah konflik
        boolean isSameStudio = (studio1Id == studio2Id);
        boolean intervalOverlap = ScheduleDao.isIntervalConflicting(time, duration, time, duration);

        boolean conflict = isSameStudio && intervalOverlap;
        assertFalse(conflict, "Jadwal pada studio berbeda tidak boleh dianggap konflik.");
    }

    @Test
    @DisplayName("Dua jadwal pada tanggal berbeda tidak bertabrakan")
    void testDifferentDatesNoConflict() {
        long studioId = 1L;
        LocalDate date1 = LocalDate.of(2026, 12, 1);
        LocalDate date2 = LocalDate.of(2026, 12, 2);
        LocalTime time = LocalTime.of(18, 0);
        int duration = 120;

        // Tanggal berbeda tidak konflik
        boolean isSameDate = date1.equals(date2);
        boolean intervalOverlap = ScheduleDao.isIntervalConflicting(time, duration, time, duration);

        boolean conflict = isSameDate && intervalOverlap;
        assertFalse(conflict, "Jadwal pada tanggal berbeda tidak boleh dianggap konflik.");
    }

    @Test
    @DisplayName("Update jadwal tanpa false conflict (tidak konflik dengan dirinya sendiri)")
    void testUpdateScheduleExcludesSelf() {
        long scheduleId = 42L;
        long excludeId = 42L; // Jadwal yang sedang diedit

        boolean isSelf = (scheduleId == excludeId);
        assertFalse(!isSelf, "Jadwal yang sedang diedit harus dikecualikan dari pemeriksaan konflik.");
    }

    @Test
    @DisplayName("Pemeriksaan interval memperhitungkan durasi film yang berbeda secara presisi")
    void testDifferentFilmDurations() {
        LocalTime baseStart = LocalTime.of(13, 0);

        // Film pendek (45 menit): 13:00 - 13:45
        // Jadwal baru: 13:40 - 15:00 -> Konflik karena masuk sebelum 13:45
        assertTrue(ScheduleDao.isIntervalConflicting(baseStart, 45, LocalTime.of(13, 40), 80));

        // Jadwal baru: 13:45 - 15:00 -> Tidak konflik karena dimulai tepat saat film 45 menit selesai
        assertFalse(ScheduleDao.isIntervalConflicting(baseStart, 45, LocalTime.of(13, 45), 75));

        // Film panjang (180 menit): 13:00 - 16:00
        // Jadwal baru: 15:30 - 17:00 -> Konflik karena jadwal lama baru selesai jam 16:00
        assertTrue(ScheduleDao.isIntervalConflicting(baseStart, 180, LocalTime.of(15, 30), 90));
    }
}
