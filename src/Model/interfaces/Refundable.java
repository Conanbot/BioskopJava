package Model.interfaces;

import java.math.BigDecimal;

public interface Refundable {
    BigDecimal calculateRefund(BigDecimal refundPercent);
}
