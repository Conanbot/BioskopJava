package Model.payment;

import Model.enums.PaymentMethod;
import Model.exception.ValidationException;

import java.math.BigDecimal;

public final class PaymentFactory {
    private PaymentFactory() {
    }

    public static Payment create(PaymentMethod method, BigDecimal amount, BigDecimal cashReceived)
            throws ValidationException {
        if (method == null) {
            throw new ValidationException("Metode pembayaran wajib dipilih.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ValidationException("Total pembayaran harus lebih dari nol.");
        }
        if (method == PaymentMethod.QRIS && cashReceived != null) {
            throw new ValidationException("Nominal uang tunai tidak berlaku untuk pembayaran QRIS.");
        }
        if (method == PaymentMethod.CASH && cashReceived == null) {
            throw new ValidationException("Nominal uang tunai wajib diisi.");
        }
        try {
            return switch (method) {
                case CASH -> new CashPayment(amount, cashReceived);
                case QRIS -> new QrisPayment(amount);
            };
        } catch (NullPointerException | IllegalArgumentException exception) {
            throw new ValidationException(exception.getMessage());
        }
    }
}
