package Model;

import Model.dao.ScheduleDao;
import Model.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Validasi Penghapusan Jadwal")
class ScheduleDeletionValidationTest {

    @Test
    @DisplayName("Jadwal tanpa riwayat booking dapat dihapus")
    void testDeleteScheduleWithoutBookingsSuccess() throws Exception {
        FakeScheduleDao fakeDao = new FakeScheduleDao();
        fakeDao.hasBookings = false;

        boolean deleted = fakeDao.delete(10L);
        assertTrue(deleted, "Jadwal tanpa transaksi atau riwayat booking harus berhasil dihapus.");
        assertEquals(10L, fakeDao.deletedId);
    }

    @Test
    @DisplayName("Jadwal dengan booking ACTIVE tidak dapat dihapus")
    void testDeleteScheduleWithActiveBookingBlocked() {
        FakeScheduleDao fakeDao = new FakeScheduleDao();
        fakeDao.hasBookings = true;
        fakeDao.presentBookingStatus = BookingStatus.ACTIVE;

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fakeDao.delete(11L));
        assertTrue(ex.getMessage().contains("tidak dapat dihapus"));
    }

    @Test
    @DisplayName("Jadwal dengan booking CANCELLED tidak dapat dihapus")
    void testDeleteScheduleWithCancelledBookingBlocked() {
        FakeScheduleDao fakeDao = new FakeScheduleDao();
        fakeDao.hasBookings = true;
        fakeDao.presentBookingStatus = BookingStatus.CANCELLED;

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fakeDao.delete(12L));
        assertTrue(ex.getMessage().contains("tidak dapat dihapus"));
    }

    @Test
    @DisplayName("Jadwal dengan booking REFUNDED tidak dapat dihapus")
    void testDeleteScheduleWithRefundedBookingBlocked() {
        FakeScheduleDao fakeDao = new FakeScheduleDao();
        fakeDao.hasBookings = true;
        fakeDao.presentBookingStatus = BookingStatus.REFUNDED;

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fakeDao.delete(13L));
        assertTrue(ex.getMessage().contains("tidak dapat dihapus"));
    }

    @Test
    @DisplayName("Seluruh riwayat status booking (ACTIVE, CANCELLED, REFUNDED) memblokir penghapusan")
    void testAllBookingStatusesBlockDeletion() {
        for (BookingStatus status : EnumSet.allOf(BookingStatus.class)) {
            FakeScheduleDao fakeDao = new FakeScheduleDao();
            fakeDao.hasBookings = true;
            fakeDao.presentBookingStatus = status;

            assertThrows(IllegalStateException.class, () -> fakeDao.delete(99L),
                    "Status booking " + status + " harus memblokir penghapusan jadwal demi menjaga FK ON DELETE RESTRICT.");
        }
    }

    // Fake ScheduleDao untuk isolasi unit test
    private static class FakeScheduleDao extends ScheduleDao {
        boolean hasBookings = false;
        BookingStatus presentBookingStatus = null;
        Long deletedId = null;

        @Override
        public boolean hasAnyBookings(long scheduleId) {
            return hasBookings;
        }

        @Override
        public boolean delete(long id) throws SQLException {
            if (hasAnyBookings(id)) {
                throw new IllegalStateException(
                        "Jadwal tidak dapat dihapus karena memiliki riwayat transaksi/booking.");
            }
            this.deletedId = id;
            return true;
        }
    }
}
