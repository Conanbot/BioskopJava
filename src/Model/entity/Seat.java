package Model.entity;

import java.util.Objects;
import java.util.regex.Pattern;

public record Seat(long scheduleId, String seatCode) {
    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z]{1,5}[1-9][0-9]{0,2}");

    public Seat {
        if (scheduleId <= 0) {
            throw new IllegalArgumentException("ID jadwal harus positif.");
        }
        Objects.requireNonNull(seatCode, "Kode kursi wajib diisi.");
        if (!isValidCode(seatCode)) {
            throw new IllegalArgumentException("Format kode kursi tidak valid.");
        }
    }

    public static boolean isValidCode(String seatCode) {
        return seatCode != null && CODE_PATTERN.matcher(seatCode).matches();
    }
}
