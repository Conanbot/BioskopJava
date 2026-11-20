package Controler.admin;

import Controler.SessionManager;
import Model.entity.DashboardSummary;
import Model.enums.Role;
import Model.exception.BookingAccessDeniedException;
import Model.exception.ValidationException;
import Model.service.BookingService;
import View.admin.AdminDashboardPanel;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/**
 * Controller untuk Admin Dashboard.
 * Mengambil ringkasan booking & pendapatan melalui BookingService.getDashboardSummary(actorUserId, actorRole).
 */
public class AdminDashboardController {
    private final BookingService bookingService;
    private final AdminDashboardPanel view;

    public AdminDashboardController(BookingService bookingService, AdminDashboardPanel view) {
        this.bookingService = Objects.requireNonNull(bookingService);
        this.view = Objects.requireNonNull(view);

        initListeners();
    }

    private void initListeners() {
        view.getRefreshButton().addActionListener(e -> loadDashboardSummary());
    }

    public void loadDashboardSummary() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.isAdmin()) {
            view.setStatusError("Akses ditolak: Hanya Admin yang dapat memuat dashboard.");
            return;
        }

        long actorUserId = session.getCurrentUserId();
        Role actorRole = session.getCurrentRole();

        view.setStatusLoading();

        SwingWorker<DashboardSummary, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardSummary doInBackground() throws Exception {
                return bookingService.getDashboardSummary(actorUserId, actorRole);
            }

            @Override
            protected void done() {
                try {
                    DashboardSummary summary = get();
                    view.setTotalFilms(summary.totalFilms());
                    view.setTotalSchedulesToday(summary.totalSchedulesToday());
                    view.setTotalBookings(summary.totalBookings());
                    view.setTotalRevenue(summary.totalRevenue());
                    view.setStatusReady();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.setStatusError("Pemuatan dashboard dibatalkan.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof BookingAccessDeniedException) {
                        view.setStatusError("Akses ditolak oleh BookingService.");
                    } else if (cause instanceof ValidationException) {
                        view.setStatusError(cause.getMessage());
                    } else if (cause instanceof SQLException) {
                        view.setStatusError("Gagal mengambil data dari database MySQL.");
                    } else {
                        view.setStatusError("Terjadi kesalahan memuat ringkasan dashboard.");
                    }
                }
            }
        };

        worker.execute();
    }
}
