package Model.exception;

public class BookingAccessDeniedException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookingAccessDeniedException() {
        super("Akun ini tidak berhak mengubah booking tersebut.");
    }
}
