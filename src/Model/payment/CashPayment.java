package Model.payment;

import Model.enums.PaymentMethod;
import Model.exception.InsufficientPaymentException;

import java.math.BigDecimal;
import java.util.Objects;

public final class CashPayment extends Payment {
    private final BigDecimal cashReceived;

    public CashPayment(BigDecimal amount, BigDecimal cashReceived) {
        super(amount);
        this.cashReceived = Objects.requireNonNull(cashReceived, "Nominal uang tunai wajib diisi.");
        if (cashReceived.signum() < 0) {
            throw new IllegalArgumentException("Nominal uang tunai tidak boleh negatif.");
        }
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public PaymentResult processPayment() throws InsufficientPaymentException {
        if (cashReceived.compareTo(getAmount()) < 0) {
            throw new InsufficientPaymentException(getAmount(), cashReceived);
        }
        return new PaymentResult(getMethod(), getAmount(), cashReceived,
                cashReceived.subtract(getAmount()));
    }
}
