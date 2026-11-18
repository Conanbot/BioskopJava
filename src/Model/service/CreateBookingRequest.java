package Model.service;

import Model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record CreateBookingRequest(
        long userId,
        long scheduleId,
        List<String> seatCodes,
        PaymentMethod paymentMethod,
        BigDecimal cashReceived) {

    public CreateBookingRequest {
        seatCodes = seatCodes == null
                ? null
                : Collections.unmodifiableList(new ArrayList<>(seatCodes));
    }
}
