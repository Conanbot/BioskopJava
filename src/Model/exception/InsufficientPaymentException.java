package Model.exception;

import java.math.BigDecimal;

public class InsufficientPaymentException extends Exception {
    private static final long serialVersionUID = 1L;
    private final BigDecimal amountDue;
    private final BigDecimal amountReceived;

    public InsufficientPaymentException(BigDecimal amountDue, BigDecimal amountReceived) {
        super("Uang yang diterima kurang dari total pembayaran.");
        this.amountDue = amountDue;
        this.amountReceived = amountReceived;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }
}
