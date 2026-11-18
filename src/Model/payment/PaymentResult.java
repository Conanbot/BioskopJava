package Model.payment;

import Model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.Objects;

public record PaymentResult(
        PaymentMethod method,
        BigDecimal amountPaid,
        BigDecimal cashReceived,
        BigDecimal changeAmount) {

    public PaymentResult {
        Objects.requireNonNull(method, "Metode pembayaran wajib diisi.");
        Objects.requireNonNull(amountPaid, "Total pembayaran wajib diisi.");
        Objects.requireNonNull(changeAmount, "Nominal kembalian wajib diisi.");
        if (amountPaid.signum() <= 0 || changeAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Nominal pembayaran harus positif dan kembalian tidak boleh negatif.");
        }
        if (method == PaymentMethod.CASH) {
            Objects.requireNonNull(cashReceived, "Nominal uang tunai wajib diisi.");
            if (cashReceived.compareTo(amountPaid) < 0
                    || cashReceived.subtract(amountPaid).compareTo(changeAmount) != 0) {
                throw new IllegalArgumentException("Rincian pembayaran tunai tidak konsisten.");
            }
        } else if (cashReceived == null || cashReceived.signum() != 0
                || changeAmount.signum() != 0) {
            throw new IllegalArgumentException("QRIS tidak menerima uang tunai atau kembalian.");
        }
    }

    public boolean isSimulated() {
        return true;
    }
}
