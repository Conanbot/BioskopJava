package Model.service;

import Model.entity.BookingReceipt;
import Model.entity.Seat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service untuk memformat dan mengekspor E-Ticket ke file teks (.txt).
 * Menggunakan konsep PBO:
 * - String formatting & StringBuilder
 * - Java Stream API (filtering, mapping, collecting)
 * - Java File I/O (NIO Files, Path, Exception Handling)
 */
public class TicketExportService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm");

    /**
     * Memformat objek BookingReceipt menjadi layout tiket bioskop yang estetik.
     */
    public String generateTicketText(BookingReceipt receipt) {
        Objects.requireNonNull(receipt, "BookingReceipt tidak boleh null.");

        // Menggunakan Stream API untuk mengurutkan dan menggabungkan kode kursi
        String seatList = receipt.seats().stream()
                .map(Seat::seatCode)
                .sorted()
                .collect(Collectors.joining(", "));

        String formattedDate = receipt.showDateTime() != null
                ? receipt.showDateTime().format(FORMATTER)
                : "-";

        String barcode = "||| | |||| || |||||| | ||| || |||| |";

        return String.format("""
                =======================================================
                            🎬 BIOSKOP NUSANTARA INDONESIA
                               E-TICKET RESMI BIOSKOP
                =======================================================
                KODE BOOKING   : %s
                STATUS         : %s
                WAKTU CETAK    : %s
                -------------------------------------------------------
                FILM           : %s
                STUDIO         : %s
                JADWAL TAYANG  : %s
                KURSI DIPESAN  : %s (%d Kursi)
                -------------------------------------------------------
                METODE BAYAR   : %s
                TOTAL HARGA    : Rp %,.2f
                BAYAR TUNAI    : Rp %,.2f
                KEMBALIAN      : Rp %,.2f
                -------------------------------------------------------
                BARCODE TIKET  :
                %s
                %s
                =======================================================
                Syarat & Ketentuan:
                1. Tunjukkan E-Ticket ini di pintu masuk studio.
                2. Tiket yang sudah dipesan tidak dapat dibatalkan 
                   setelah jadwal tayang dimulai.
                3. Dilarang membawa makanan & minuman dari luar bioskop.
                =======================================================
                """,
                receipt.booking().bookingCode(),
                receipt.booking().status(),
                java.time.LocalDateTime.now().format(FORMATTER),
                receipt.filmTitle(),
                receipt.studioName(),
                formattedDate,
                seatList,
                receipt.booking().ticketCount(),
                receipt.payment().method(),
                receipt.booking().totalPrice(),
                receipt.payment().cashReceived(),
                receipt.payment().changeAmount(),
                barcode,
                receipt.booking().bookingCode()
        );
    }

    /**
     * Menyimpan e-ticket ke file teks di direktori komputer pengguna.
     * Menggunakan File, Path, dan Files.writeString.
     */
    public File exportToFile(BookingReceipt receipt, Path directory) throws IOException {
        String ticketContent = generateTicketText(receipt);
        String fileName = "Tiket_" + receipt.booking().bookingCode() + ".txt";

        Path targetDir = directory != null ? directory : Path.of(System.getProperty("user.home"), "Downloads");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetFile = targetDir.resolve(fileName);
        Files.writeString(
                targetFile,
                ticketContent,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        return targetFile.toFile();
    }
}
