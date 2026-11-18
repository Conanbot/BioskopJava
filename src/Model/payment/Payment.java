package Model.payment;

import Model.enums.PaymentMethod;
import Model.exception.InsufficientPaymentException;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class Payment {
    private final BigDecimal amount;

    protected Payment(BigDecimal amount) {
        this.amount = Objects.requireNonNull(amount, "Total pembayaran wajib diisi.");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Total pembayaran harus lebih dari nol.");
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public abstract PaymentMethod getMethod();

    public abstract PaymentResult processPayment() throws InsufficientPaymentException;
}
