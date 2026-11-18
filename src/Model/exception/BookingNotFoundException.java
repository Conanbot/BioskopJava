package Model.exception;

public class BookingNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookingNotFoundException(String bookingCode) {
        super("Booking dengan kode " + bookingCode + " tidak ditemukan.");
    }
}
