package Model.exception;

public class SeatAlreadyBookedException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String seatCode;

    public SeatAlreadyBookedException(String seatCode) {
        super("Kursi " + seatCode + " sudah dipesan untuk jadwal ini.");
        this.seatCode = seatCode;
    }

    public String getSeatCode() {
        return seatCode;
    }
}
