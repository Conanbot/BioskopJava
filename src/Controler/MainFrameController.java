package Controler;

import Controler.admin.AdminMainController;
import Model.dao.ScheduleDao;
import Model.dao.StudioDao;
import Model.dao.UserDao;
import Model.enums.Role;
import Model.service.AuthService;
import Model.service.BookingService;
import Model.service.DatabaseHealthService;
import View.AccountsPanel;
import View.MainFrame;
import View.admin.AdminPanel;
import View.customer.CustomerPanel;

import java.awt.CardLayout;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public final class MainFrameController {
    public static final String ACCOUNTS = "accounts";
    public static final String CUSTOMER = "customer";
    public static final String ADMIN = "admin";

    private final MainFrame view;
    private final NavigationController navigationController;
    private AdminMainController adminMainController;

    public MainFrameController(MainFrame view) {
        this.view = Objects.requireNonNull(view, "Tampilan utama wajib diisi.");
        if (!(view.getCardsPanel().getLayout() instanceof CardLayout cardLayout)) {
            throw new IllegalStateException("Panel halaman harus menggunakan CardLayout.");
        }
        this.navigationController = new NavigationController(
                cardLayout,
                view.getCardsPanel());
    }

    public void initialize() {
        requireEventDispatchThread();

        // ── Jonathan: Modul Akun & Autentikasi ────────────────────────────────────
        AccountsPanel accountsPanel = new AccountsPanel();
        AuthService authService = new AuthService(new UserDao());

        // ── Dava: Modul Layanan Customer ──────────────────────────────────────────
        CustomerPanel customerPanel = new CustomerPanel();
        customerPanel.setOnLogout(() -> {
            SessionManager.getInstance().clearSession();
            accountsPanel.showLogin();
            navigationController.showPage(ACCOUNTS);
        });

        registerPage(CUSTOMER, customerPanel);

        new LoginController(authService, accountsPanel.getLoginPanel(), accountsPanel, user -> {
            if (user.getRole() == Role.ADMIN) {
                if (adminMainController != null) {
                    adminMainController.onAdminOpened();
                }
                navigationController.showPage(ADMIN);
            } else {
                customerPanel.updateGreeting();
                customerPanel.loadFilmCards();
                navigationController.showPage(CUSTOMER);
            }
        });

        new RegisterController(authService, accountsPanel.getRegisterPanel(), accountsPanel);

        registerPage(ACCOUNTS, accountsPanel);

        // ── Jonathan: Modul Operasional Admin ─────────────────────────────────────
        AdminPanel adminPanel = new AdminPanel();
        BookingService bookingService = new BookingService();
        StudioDao studioDao = new StudioDao();
        ScheduleDao scheduleDao = new ScheduleDao();

        this.adminMainController = new AdminMainController(
                adminPanel,
                bookingService,
                studioDao,
                scheduleDao,
                () -> {
                    SessionManager.getInstance().clearSession();
                    accountsPanel.showLogin();
                    navigationController.showPage(ACCOUNTS);
                }
        );

        registerPage(ADMIN, adminPanel);

        // Cek koneksi database di background dan tampilkan hasilnya di terminal
        checkDatabaseConnectionInTerminal(new DatabaseHealthService());

        navigationController.showPage(ACCOUNTS);
    }

    private void checkDatabaseConnectionInTerminal(DatabaseHealthService healthService) {
        SwingWorker<Void, Void> checkTask = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                healthService.verifySchema();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    System.out.println("[DB] Koneksi database berhasil.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    System.err.println("[DB] Koneksi database gagal: pemeriksaan dihentikan.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    String message = cause == null ? exception.getMessage() : cause.getMessage();
                    System.err.println("[DB] Koneksi database gagal: "
                            + (message == null || message.isBlank() ? "Terjadi kesalahan koneksi." : message));
                }
            }
        };
        checkTask.execute();
    }

    private void registerPage(String id, JPanel panel) {
        navigationController.registerPage(id, panel);
    }

    private static void requireEventDispatchThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Controller GUI harus dijalankan pada Event Dispatch Thread.");
        }
    }
}
