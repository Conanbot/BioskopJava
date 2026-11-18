package Model.exception;

public class BookingNotCancellableException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookingNotCancellableException(String message) {
        super(message);
    }
}
