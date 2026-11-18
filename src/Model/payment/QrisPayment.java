package Model.payment;

import Model.enums.PaymentMethod;
import Model.exception.InsufficientPaymentException;

import java.math.BigDecimal;

public final class QrisPayment extends Payment {
    public QrisPayment(BigDecimal amount) {
        super(amount);
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.QRIS;
    }

    @Override
    public PaymentResult processPayment() throws InsufficientPaymentException {
        return new PaymentResult(getMethod(), getAmount(), BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
