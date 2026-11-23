package Controler.admin;

import Controler.SessionManager;
import Model.dao.FilmDao;
import Model.dao.ScheduleDao;
import Model.dao.StudioDao;
import Model.service.BookingService;
import View.admin.AdminPanel;

import java.util.Objects;

/**
 * Controller utama untuk mengorkestrasi sub-fitur Admin:
 * Dashboard, Film, Studio, Jadwal, Cancel/Refund, dan Logout.
 */
public class AdminMainController {
    private final AdminPanel view;
    private final AdminDashboardController dashboardController;
    private final FilmController filmController;
    private final StudioController studioController;
    private final ScheduleController scheduleController;
    private final AdminCancelRefundController cancelRefundController;
    private final Runnable onLogout;

    public AdminMainController(
            AdminPanel view,
            BookingService bookingService,
            FilmDao filmDao,
            StudioDao studioDao,
            ScheduleDao scheduleDao,
            Runnable onLogout) {
        this.view = Objects.requireNonNull(view);
        this.onLogout = Objects.requireNonNull(onLogout);

        this.dashboardController = new AdminDashboardController(bookingService, view.getDashboardPanel());
        this.filmController = new FilmController(filmDao, view.getFilmPanel());
        this.studioController = new StudioController(studioDao, view.getStudioPanel());
        this.scheduleController = new ScheduleController(scheduleDao, studioDao, view.getSchedulePanel());
        this.cancelRefundController = new AdminCancelRefundController(bookingService, view.getCancelRefundPanel());

        initNavigation();
    }

    private void initNavigation() {
        view.getDashboardButton().addActionListener(e -> {
            view.showDashboard();
            dashboardController.loadDashboardSummary();
        });

        view.getFilmButton().addActionListener(e -> {
            view.showFilm();
            filmController.loadFilms();
        });

        view.getStudioButton().addActionListener(e -> {
            view.showStudio();
            studioController.loadStudios();
        });

        view.getScheduleButton().addActionListener(e -> {
            view.showSchedule();
            scheduleController.loadSchedules();
        });

        view.getCancelRefundButton().addActionListener(e -> {
            view.showCancelRefund();
        });

        view.getLogoutButton().addActionListener(e -> {
            SessionManager.getInstance().clearSession();
            onLogout.run();
        });
    }

    /**
     * Dipanggil saat halaman Admin diaktifkan/dibuka.
     */
    public void onAdminOpened() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            view.setUsernameDisplay(session.getCurrentUser().getUsername());
        }
        view.showDashboard();
        dashboardController.loadDashboardSummary();
    }
}
