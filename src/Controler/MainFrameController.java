package Controler;

import Controler.admin.AdminMainController;
import Model.dao.ScheduleDao;
import Model.dao.StudioDao;
import Model.dao.UserDao;
import Model.entity.User;
import Model.enums.Role;
import Model.service.AuthService;
import Model.service.BookingService;
import Model.service.DatabaseHealthService;
import View.AccountsPanel;
import View.PendingModulePanel;
import View.StartupPanel;
import View.MainFrame;
import View.admin.AdminPanel;
import View.customer.CustomerPanel;

import java.awt.CardLayout;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class MainFrameController {
    private static final String DIAGNOSTICS = "diagnostics";
    private static final String ACCOUNTS = "accounts";
    private static final String CUSTOMER = "customer";
    private static final String ADMIN = "admin";
    private static final String FILMS = "films";

    private final MainFrame view;
    private final NavigationController navigationController;

    public MainFrameController(MainFrame view) {
        this.view = Objects.requireNonNull(view, "Tampilan utama wajib diisi.");
        if (!(view.getCardsPanel().getLayout() instanceof CardLayout cardLayout)) {
            throw new IllegalStateException("Panel halaman harus menggunakan CardLayout.");
        }
        this.navigationController = new NavigationController(
                cardLayout,
                view.getCardsPanel(),
                view.getPageTitle());
    }

    public void initialize() {
        requireEventDispatchThread();

        StartupPanel startupPanel = new StartupPanel();
        registerPage(DIAGNOSTICS, "Pemeriksaan sistem", startupPanel, view.getDiagnosticsButton());

        // ── Jonathan: Modul Akun & Autentikasi ────────────────────────────────────
        AccountsPanel accountsPanel = new AccountsPanel();
        AuthService authService = new AuthService(new UserDao());

        new LoginController(authService, accountsPanel.getLoginPanel(), accountsPanel, user -> {
            // Setelah login berhasil, arahkan sesuai role
            if (user.getRole() == Role.ADMIN) {
                navigationController.showPage(ADMIN);
            } else {
                navigationController.showPage(CUSTOMER);
            }
        });

        new RegisterController(authService, accountsPanel.getRegisterPanel(), accountsPanel);

        registerPage(
                ACCOUNTS,
                "Akun dan sesi",
                accountsPanel,
                view.getAccountsButton());

        // ── Dava: Modul Layanan Customer ──────────────────────────────────────────
        CustomerPanel customerPanel = new CustomerPanel();
        registerPage(
                CUSTOMER,
                "Layanan Customer",
                customerPanel,
                view.getCustomerButton());

        // ── Jonathan: Modul Operasional Admin ─────────────────────────────────────
        AdminPanel adminPanel = new AdminPanel();
        BookingService bookingService = new BookingService();
        StudioDao studioDao = new StudioDao();
        ScheduleDao scheduleDao = new ScheduleDao();

        AdminMainController adminMainController = new AdminMainController(
                adminPanel,
                bookingService,
                studioDao,
                scheduleDao,
                () -> {
                    // Saat admin logout, kembali ke Login
                    accountsPanel.showLogin();
                    navigationController.showPage(ACCOUNTS);
                }
        );

        // Saat tombol Admin di sidebar diklik, periksa akses role
        view.getAdminButton().addActionListener(e -> {
            if (!SessionManager.getInstance().isAdmin()) {
                AlertUtil.showWarning(view, "Akses Terbatas", "Halaman ini memerlukan login dengan Role ADMIN.");
                navigationController.showPage(ACCOUNTS);
                return;
            }
            adminMainController.onAdminOpened();
        });

        registerPage(
                ADMIN,
                "Operasional admin",
                adminPanel,
                view.getAdminButton());

        // ── Tian: Modul Pengelolaan Film (Dilarang diubah) ────────────────────────
        registerPage(
                FILMS,
                "Pengelolaan film",
                new PendingModulePanel(
                        "Pengelolaan film",
                        "Tian",
                        List.of("CRUD Film", "FilmController", "ManageFilm", "Aset poster", "Desain visual global")),
                view.getFilmsButton());

        new StartupController(
                new DatabaseHealthService(),
                startupPanel.getDatabaseStatus(),
                startupPanel.getDatabaseDetails(),
                startupPanel.getConnectionButton());
        startupPanel.getDialogButton().addActionListener(event -> AlertUtil.showInformation(
                view,
                "Uji dialog",
                "Dialog berhasil dibuka. Tombol OK atau Escape menutup dialog."));

        navigationController.showPage(DIAGNOSTICS);
    }

    private void registerPage(
            String id,
            String title,
            JPanel panel,
            javax.swing.JButton navigationButton) {
        navigationController.registerPage(id, title, panel, navigationButton);
    }

    private static void requireEventDispatchThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Controller GUI harus dijalankan pada Event Dispatch Thread.");
        }
    }
}
