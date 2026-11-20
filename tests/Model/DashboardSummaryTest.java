package Model;

import Model.entity.DashboardSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Model Domain DashboardSummary")
class DashboardSummaryTest {

    @Test
    @DisplayName("DashboardSummary memuat 4 statistik utama secara konsisten")
    void testDashboardSummaryFourMetrics() {
        DashboardSummary summary = new DashboardSummary(12L, 5L, 340L, new BigDecimal("15300000.00"));

        assertEquals(12L, summary.totalFilms());
        assertEquals(5L, summary.totalSchedulesToday());
        assertEquals(340L, summary.totalBookings());
        assertEquals(new BigDecimal("15300000.00"), summary.totalRevenue());
    }

    @Test
    @DisplayName("DashboardSummary konstruktor lama kompatibel (backward-compatible)")
    void testLegacyConstructorCompatibility() {
        DashboardSummary summary = new DashboardSummary(150L, new BigDecimal("6750000.00"));

        assertEquals(0L, summary.totalFilms());
        assertEquals(0L, summary.totalSchedulesToday());
        assertEquals(150L, summary.totalBookings());
        assertEquals(new BigDecimal("6750000.00"), summary.totalRevenue());
    }

    @Test
    @DisplayName("DashboardSummary menolak nilai metrik negatif")
    void testNegativeMetricsValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                new DashboardSummary(-1L, 0L, 0L, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () ->
                new DashboardSummary(0L, -1L, 0L, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () ->
                new DashboardSummary(0L, 0L, -1L, BigDecimal.ZERO));
        assertThrows(NullPointerException.class, () ->
                new DashboardSummary(0L, 0L, 0L, null));
    }
}
