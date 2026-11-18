package Controler;

import Model.service.DatabaseHealthService;
import View.PendingModulePanel;
import View.StartupPanel;
import View.MainFrame;

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
        registerPage(
                ACCOUNTS,
                "Akun dan sesi",
                new PendingModulePanel(
                        "Akun dan sesi",
                        "Jonathan",
                        List.of("Login", "Register", "SessionManager", "User, Admin, dan Customer")),
                view.getAccountsButton());
        registerPage(
                CUSTOMER,
                "Layanan customer",
                new PendingModulePanel(
                        "Layanan customer",
                        "Dava",
                        List.of(
                                "Customer Home",
                                "Film Detail",
                                "Seat Selection dan SeatMapView",
                                "Payment UI",
                                "E-Ticket dan Booking History",
                                "Animasi dan interaktivitas")),
                view.getCustomerButton());
        registerPage(
                ADMIN,
                "Operasional admin",
                new PendingModulePanel(
                        "Operasional admin",
                        "Jonathan",
                        List.of("Dashboard Admin", "CRUD Studio", "CRUD Jadwal", "Validasi jadwal bentrok")),
                view.getAdminButton());
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
