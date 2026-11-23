package Controler.admin;

import Controler.AlertUtil;
import Controler.SessionManager;
import Model.entity.BookingCancellationResult;
import Model.entity.BookingHistoryItem;
import Model.enums.BookingResolution;
import Model.enums.Role;
import Model.exception.BookingAccessDeniedException;
import Model.exception.BookingNotCancellableException;
import Model.exception.BookingNotFoundException;
import Model.exception.ValidationException;
import Model.service.BookingService;
import View.admin.AdminCancelRefundPanel;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Controller untuk mengintegrasikan fitur pembatalan dan refund dari sisi Admin.
 * Memanggil BookingService.cancelOrRefundBooking() menggunakan actorUserId dan Role.ADMIN dari SessionManager.
 */
public class AdminCancelRefundController {
    private final BookingService bookingService;
    private final AdminCancelRefundPanel view;

    public AdminCancelRefundController(BookingService bookingService, AdminCancelRefundPanel view) {
        this.bookingService = Objects.requireNonNull(bookingService);
        this.view = Objects.requireNonNull(view);

        initListeners();
    }

    private void initListeners() {
        view.getProcessButton().addActionListener(e -> handleProcess());
        view.getClearButton().addActionListener(e -> view.clearForm());
        view.getRefreshButton().addActionListener(e -> loadTransactions());
    }

    public void loadTransactions() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.isAdmin()) {
            return;
        }
        view.setStatusInfo("Memuat daftar transaksi...");
        long actorUserId = session.getCurrentUserId();
        Role actorRole = session.getCurrentRole();

        SwingWorker<List<BookingHistoryItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BookingHistoryItem> doInBackground() throws Exception {
                return bookingService.getAllBookingHistory(actorUserId, actorRole);
            }

            @Override
            protected void done() {
                try {
                    List<BookingHistoryItem> items = get();
                    view.populateTransactions(items);
                    view.setStatusInfo("Total transaksi: " + items.size());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.setStatusError("Gagal memuat daftar transaksi.");
                } catch (ExecutionException ex) {
                    view.setStatusError("Gagal memuat data transaksi dari database.");
                }
            }
        };
        worker.execute();
    }

    private void handleProcess() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat memproses pembatalan/refund pada modul ini.");
            return;
        }

        String bookingCode = view.getBookingCode();
        if (bookingCode.isEmpty()) {
            AlertUtil.showWarning(view, "Peringatan", "Masukkan kode booking terlebih dahulu.");
            return;
        }

        String resStr = view.getSelectedResolution();
        BookingResolution resolution = "REFUND".equalsIgnoreCase(resStr)
                ? BookingResolution.REFUND : BookingResolution.CANCEL;

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Apakah Anda yakin ingin memproses " + resolution.name()
                        + " untuk booking code: " + bookingCode + "?\n"
                        + "Jika REFUND dipilih, saldo akan dikembalikan 100% dan kursi akan dipulihkan.\n"
                        + "Jika CANCEL dipilih, booking dibatalkan tanpa pengembalian dana.",
                "Konfirmasi " + resolution.name(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        long actorUserId = session.getCurrentUserId();
        Role actorRole = session.getCurrentRole();

        view.setStatusLoading();

        SwingWorker<BookingCancellationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected BookingCancellationResult doInBackground() throws Exception {
                return bookingService.cancelOrRefundBooking(bookingCode, actorUserId, actorRole, resolution);
            }

            @Override
            protected void done() {
                try {
                    BookingCancellationResult result = get();
                    String output = String.format(
                            "STATUS PROSES: SUKSES\n"
                                    + "──────────────────────────────\n"
                                    + "Kode Booking  : %s\n"
                                    + "Status Akhir  : %s\n"
                                    + "Nominal Refund: Rp %,.0f\n"
                                    + "──────────────────────────────\n"
                                    + "Kursi telah dilepaskan dan ketersediaan kursi pada jadwal telah dipulihkan.",
                            result.bookingCode(),
                            result.status(),
                            result.refundedAmount()
                    );
                    view.setResultText(output);
                    view.setStatusSuccess("Booking " + bookingCode + " berhasil di-" + resolution.name() + ".");
                    loadTransactions();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.setStatusError("Proses dihentikan.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg;
                    if (cause instanceof BookingNotFoundException) {
                        msg = "Kode booking tidak ditemukan: " + bookingCode;
                    } else if (cause instanceof BookingNotCancellableException) {
                        msg = "Tidak dapat diproses: " + cause.getMessage();
                    } else if (cause instanceof BookingAccessDeniedException) {
                        msg = "Akses ditolak: Anda tidak berwenang membatalkan booking ini.";
                    } else if (cause instanceof ValidationException) {
                        msg = "Validasi gagal: " + cause.getMessage();
                    } else if (cause instanceof SQLException) {
                        msg = "Kesalahan database saat memperbarui booking/kursi.";
                    } else {
                        msg = "Terjadi kesalahan internal: " + (cause != null ? cause.getMessage() : "Unknown");
                    }
                    view.setResultText("STATUS PROSES: GAGAL\n──────────────────────────────\n" + msg);
                    view.setStatusError(msg);
                }
            }
        };

        worker.execute();
    }
}
