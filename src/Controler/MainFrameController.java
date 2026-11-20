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
    public static final String DIAGNOSTICS = "diagnostics";
    public static final String ACCOUNTS = "accounts";
    public static final String CUSTOMER = "customer";
    public static final String ADMIN = "admin";
    public static final String FILMS = "films";

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

        // ── Dava: Modul Layanan Customer ──────────────────────────────────────────
        CustomerPanel customerPanel = new CustomerPanel();
        customerPanel.setOnLogout(() -> {
            SessionManager.getInstance().clearSession();
            applyRoleNavigation(null);
            accountsPanel.showLogin();
            navigationController.showPage(ACCOUNTS);
        });

        registerPage(
                CUSTOMER,
                "Layanan Customer",
                customerPanel,
                view.getCustomerButton());

        new LoginController(authService, accountsPanel.getLoginPanel(), accountsPanel, user -> {
            applyRoleNavigation(user.getRole());
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

        registerPage(
                ACCOUNTS,
                "Akun dan sesi",
                accountsPanel,
                view.getAccountsButton());

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
                    // Saat logout: bersihkan sesi, reset menu ke kondisi belum login, kembali ke Login
                    SessionManager.getInstance().clearSession();
                    applyRoleNavigation(null);
                    accountsPanel.showLogin();
                    navigationController.showPage(ACCOUNTS);
                }
        );

        // Proteksi klik tombol Admin di sidebar: harus login role ADMIN
        view.getAdminButton().addActionListener(e -> {
            if (!SessionManager.getInstance().isAdmin()) {
                AlertUtil.showWarning(view, "Akses Terbatas", "Halaman ini memerlukan login dengan Role ADMIN.");
                navigationController.showPage(ACCOUNTS);
                return;
            }
            adminMainController.onAdminOpened();
        });

        // Proteksi klik tombol Customer di sidebar: harus login role CUSTOMER
        view.getCustomerButton().addActionListener(e -> {
            if (!SessionManager.getInstance().isLoggedIn()) {
                AlertUtil.showWarning(view, "Akses Terbatas", "Silakan login terlebih dahulu untuk mengakses layanan Customer.");
                navigationController.showPage(ACCOUNTS);
            } else {
                customerPanel.updateGreeting();
                customerPanel.loadFilmCards();
            }
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
                startupPanel.getConnectionButton(),
                () -> {
                    // Sesuai Flowchart: Database siap -> Tampilkan Login / Register
                    accountsPanel.showLogin();
                    navigationController.showPage(ACCOUNTS);
                });
        startupPanel.getDialogButton().addActionListener(event -> AlertUtil.showInformation(
                view,
                "Uji dialog",
                "Dialog berhasil dibuka. Tombol OK atau Escape menutup dialog."));

        // Kondisi awal belum login: sesuaikan tombol navigasi
        applyRoleNavigation(null);

        navigationController.showPage(ACCOUNTS);
    }

    /**
     * Mengatur visibilitas tombol navigasi di sidebar berdasarkan role pengguna:
     * - Belum login: Tampilkan Diagnostik dan Login/Register; sembunyikan Customer dan Admin.
     * - ADMIN: Tampilkan Diagnostik, Admin, Film; sembunyikan Customer.
     * - CUSTOMER: Tampilkan Diagnostik, Customer, Film; sembunyikan Admin.
     */
    public void applyRoleNavigation(Role role) {
        if (role == Role.ADMIN) {
            view.getAccountsButton().setVisible(false);
            view.getCustomerButton().setVisible(false);
            view.getAdminButton().setVisible(true);
            view.getFilmsButton().setVisible(true);
            view.getDiagnosticsButton().setVisible(true);
        } else if (role == Role.CUSTOMER) {
            view.getAccountsButton().setVisible(false);
            view.getCustomerButton().setVisible(true);
            view.getAdminButton().setVisible(false);
            view.getFilmsButton().setVisible(true);
            view.getDiagnosticsButton().setVisible(true);
        } else {
            // Belum login
            view.getAccountsButton().setVisible(true);
            view.getCustomerButton().setVisible(false);
            view.getAdminButton().setVisible(false);
            view.getFilmsButton().setVisible(false);
            view.getDiagnosticsButton().setVisible(true);
        }
        view.revalidate();
        view.repaint();
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
