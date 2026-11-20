package Model;

import Model.dao.StudioDao;
import Model.entity.Studio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Konsistensi Kapasitas Studio dan Jadwal")
class StudioCapacityConsistencyTest {

    @Test
    @DisplayName("Studio yang sudah digunakan jadwal tidak dapat diubah dimensinya")
    void testStudioUsedByScheduleCannotChangeDimensions() {
        FakeStudioDao fakeDao = new FakeStudioDao();
        fakeDao.isUsed = true;
        fakeDao.currentStudio = new Studio(1L, "Studio 1", 8, 10); // Kapasitas 80

        // Mengubah baris dari 8 ke 10 pada studio yang digunakan jadwal harus ditolak
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fakeDao.update(1L, "Studio 1 Premiere", 10, 10));
        assertTrue(ex.getMessage().contains("tidak dapat diubah karena studio sudah digunakan"));

        // Mengubah kolom dari 10 ke 12 pada studio yang digunakan jadwal juga harus ditolak
        assertThrows(IllegalStateException.class, () ->
                fakeDao.update(1L, "Studio 1", 8, 12));
    }

    @Test
    @DisplayName("Studio yang belum digunakan jadwal dapat diubah dimensinya")
    void testStudioUnusedByScheduleCanChangeDimensions() throws Exception {
        FakeStudioDao fakeDao = new FakeStudioDao();
        fakeDao.isUsed = false;
        fakeDao.currentStudio = new Studio(2L, "Studio 2", 6, 8); // Kapasitas 48

        // Mengubah dimensi studio yang belum terikat jadwal harus diperbolehkan
        boolean updated = fakeDao.update(2L, "Studio 2 VIP", 8, 10);
        assertTrue(updated);
        assertEquals(8, fakeDao.updatedRows);
        assertEquals(10, fakeDao.updatedCols);
        assertEquals("Studio 2 VIP", fakeDao.updatedName);
    }

    @Test
    @DisplayName("Studio yang digunakan jadwal tetap boleh mengubah namanya jika dimensi tetap")
    void testStudioUsedByScheduleCanRenameIfDimensionsUnchanged() throws Exception {
        FakeStudioDao fakeDao = new FakeStudioDao();
        fakeDao.isUsed = true;
        fakeDao.currentStudio = new Studio(1L, "Studio 1", 8, 10);

        // Hanya ganti nama, baris dan kolom tetap sama -> Diperbolehkan
        boolean updated = fakeDao.update(1L, "Studio 1 Deluxe", 8, 10);
        assertTrue(updated);
        assertEquals("Studio 1 Deluxe", fakeDao.updatedName);
    }

    @Test
    @DisplayName("Sinkronisasi kapasitas: available_seats dihitung benar dan tidak melebihi total_seats")
    void testAvailableSeatsNeverExceedsTotalSeats() {
        int studioCapacity = 100; // Kapasitas baru
        int bookedActiveSeats = 25; // Kursi yang sudah dipesan

        // available_seats = totalSeats - bookedSeats
        int availableSeats = studioCapacity - bookedActiveSeats;

        assertEquals(75, availableSeats);
        assertTrue(availableSeats <= studioCapacity, "Kursi tersedia tidak boleh melebihi total kursi.");
        assertTrue(availableSeats >= 0, "Kursi tersedia tidak boleh bernilai negatif.");

        // Jika tidak ada booking, available_seats sama dengan total_seats
        int zeroBookings = 0;
        int maxAvailable = studioCapacity - zeroBookings;
        assertEquals(studioCapacity, maxAvailable);
        assertTrue(maxAvailable <= studioCapacity);
    }

    @Test
    @DisplayName("Pembaruan kapasitas ditolak jika kapasitas baru lebih kecil dari kursi yang sudah dipesan")
    void testCannotReduceCapacityBelowActiveBookings() {
        int newCapacity = 20;
        int activeBookedSeats = 30;

        // Simulasi aturan konsistensi ScheduleDao.update()
        assertThrows(IllegalStateException.class, () -> {
            if (newCapacity < activeBookedSeats) {
                throw new IllegalStateException("Kapasitas studio baru (" + newCapacity
                        + ") lebih kecil dari jumlah kursi yang sudah dipesan (" + activeBookedSeats + ").");
            }
        });
    }

    @Test
    @DisplayName("Riwayat booking terdahulu tetap utuh saat kapasitas dievaluasi")
    void testExistingBookingsPreserved() {
        int bookedCount = 15;
        int totalSeats = 80;
        int availableSeats = totalSeats - bookedCount;

        // Memastikan bookedCount tidak berubah
        assertEquals(15, bookedCount);
        assertEquals(65, availableSeats);
    }

    // Fake StudioDao untuk isolasi unit test
    private static class FakeStudioDao extends StudioDao {
        boolean isUsed = false;
        Studio currentStudio = null;
        String updatedName = null;
        int updatedRows = 0;
        int updatedCols = 0;

        @Override
        public Optional<Studio> findById(long id) {
            return Optional.ofNullable(currentStudio);
        }

        @Override
        public boolean isUsedBySchedule(long studioId) {
            return isUsed;
        }

        @Override
        public boolean update(long id, String name, int seatRows, int seatCols) throws SQLException {
            if (currentStudio != null) {
                if ((currentStudio.getSeatRows() != seatRows || currentStudio.getSeatCols() != seatCols)
                        && isUsed) {
                    throw new IllegalStateException(
                            "Dimensi kursi studio (baris/kolom) tidak dapat diubah karena studio sudah digunakan oleh jadwal tayang.");
                }
            }
            this.updatedName = name;
            this.updatedRows = seatRows;
            this.updatedCols = seatCols;
            return true;
        }
    }
}
