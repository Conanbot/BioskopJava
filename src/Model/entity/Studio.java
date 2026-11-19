package Model.entity;

import java.util.Objects;

/**
 * Entitas studio yang sesuai dengan tabel studios di database.
 * seat_rows dan seat_cols menentukan dimensi kursi pada studio.
 */
public final class Studio {
    private final long id;
    private final String name;
    private final int seatRows;
    private final int seatCols;

    public Studio(long id, String name, int seatRows, int seatCols) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama studio wajib diisi.");
        }
        if (seatRows <= 0) {
            throw new IllegalArgumentException("Jumlah baris kursi harus lebih dari nol.");
        }
        if (seatCols <= 0) {
            throw new IllegalArgumentException("Jumlah kolom kursi harus lebih dari nol.");
        }
        this.id = id;
        this.name = name;
        this.seatRows = seatRows;
        this.seatCols = seatCols;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSeatRows() {
        return seatRows;
    }

    public int getSeatCols() {
        return seatCols;
    }

    public int getTotalCapacity() {
        return seatRows * seatCols;
    }

    @Override
    public String toString() {
        return name;
    }
}
