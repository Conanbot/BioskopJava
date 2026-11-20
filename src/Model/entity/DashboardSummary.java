package Model.entity;

import java.math.BigDecimal;
import java.util.Objects;

public record DashboardSummary(
        long totalFilms,
        long totalSchedulesToday,
        long totalBookings,
        BigDecimal totalRevenue) {

    public DashboardSummary {
        if (totalFilms < 0) {
            throw new IllegalArgumentException("Jumlah film tidak boleh negatif.");
        }
        if (totalSchedulesToday < 0) {
            throw new IllegalArgumentException("Jumlah jadwal hari ini tidak boleh negatif.");
        }
        if (totalBookings < 0) {
            throw new IllegalArgumentException("Jumlah booking tidak boleh negatif.");
        }
        Objects.requireNonNull(totalRevenue, "Total pendapatan wajib diisi.");
    }

    public DashboardSummary(long totalBookings, BigDecimal totalRevenue) {
        this(0L, 0L, totalBookings, totalRevenue);
    }
}
