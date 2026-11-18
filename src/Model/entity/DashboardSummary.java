package Model.entity;

import java.math.BigDecimal;
import java.util.Objects;

public record DashboardSummary(long totalBookings, BigDecimal totalRevenue) {
    public DashboardSummary {
        if (totalBookings < 0) {
            throw new IllegalArgumentException("Jumlah booking tidak boleh negatif.");
        }
        Objects.requireNonNull(totalRevenue, "Total pendapatan wajib diisi.");
    }
}
