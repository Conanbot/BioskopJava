package Controler.admin;

import Controler.AlertUtil;
import Controler.SessionManager;
import Model.dao.ScheduleDao;
import Model.dao.StudioDao;
import Model.entity.Schedule;
import Model.entity.Studio;
import View.admin.ScheduleFormDialog;
import View.admin.ScheduleManagementPanel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Controller untuk CRUD Jadwal Tayang.
 * Memvalidasi studio, film, waktu, harga, dan konflik jadwal studio.
 */
public class ScheduleController {
    private final ScheduleDao scheduleDao;
    private final StudioDao studioDao;
    private final ScheduleManagementPanel view;

    public ScheduleController(ScheduleDao scheduleDao, StudioDao studioDao, ScheduleManagementPanel view) {
        this.scheduleDao = Objects.requireNonNull(scheduleDao);
        this.studioDao = Objects.requireNonNull(studioDao);
        this.view = Objects.requireNonNull(view);

        initListeners();
    }

    private void initListeners() {
        view.getRefreshButton().addActionListener(e -> loadSchedules());
        view.getAddButton().addActionListener(e -> handleAddSchedule());
        view.getEditButton().addActionListener(e -> handleEditSchedule());
        view.getDeleteButton().addActionListener(e -> handleDeleteSchedule());
    }

    public void loadSchedules() {
        view.setStatus("Memuat jadwal tayang...");
        SwingWorker<List<Schedule>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Schedule> doInBackground() throws Exception {
                return scheduleDao.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Schedule> list = get();
                    view.populateTable(list);
                    view.setStatus("Total jadwal: " + list.size());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.setStatusError("Gagal memuat jadwal.");
                } catch (ExecutionException ex) {
                    view.setStatusError("Gagal memuat data jadwal dari database.");
                }
            }
        };
        worker.execute();
    }

    private void handleAddSchedule() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat menambah jadwal.");
            return;
        }

        view.setStatus("Memuat referensi film & studio...");
        SwingWorker<FormData, Void> initWorker = new SwingWorker<>() {
            @Override
            protected FormData doInBackground() throws Exception {
                List<ScheduleDao.FilmSummary> films = scheduleDao.findAvailableFilms();
                List<Studio> studios = studioDao.findAll();
                return new FormData(films, studios);
            }

            @Override
            protected void done() {
                try {
                    FormData data = get();
                    if (data.films().isEmpty()) {
                        AlertUtil.showWarning(view, "Perhatian", "Belum ada data film aktif (NOW_SHOWING atau COMING_SOON).");
                        return;
                    }
                    if (data.studios().isEmpty()) {
                        AlertUtil.showWarning(view, "Perhatian", "Belum ada studio terdaftar. Tambahkan studio terlebih dahulu.");
                        return;
                    }

                    ScheduleFormDialog dialog = new ScheduleFormDialog(
                            SwingUtilities.getWindowAncestor(view), null, data.films(), data.studios());
                    dialog.setVisible(true);

                    if (!dialog.isConfirmed()) {
                        return;
                    }

                    long filmId = dialog.getSelectedFilmId();
                    Studio studio = dialog.getSelectedStudio();
                    LocalDate date = dialog.getShowDate();
                    LocalTime time = dialog.getShowTime();
                    BigDecimal price = dialog.getTicketPrice();
                    int totalSeats = studio.getTotalCapacity();

                    saveNewSchedule(filmId, studio.getId(), date, time, price, totalSeats);

                } catch (Exception ex) {
                    AlertUtil.showError(view, "Kesalahan", "Gagal menyiapkan form jadwal: " + ex.getMessage());
                }
            }
        };
        initWorker.execute();
    }

    private void saveNewSchedule(long filmId, long studioId, LocalDate date, LocalTime time, BigDecimal price, int totalSeats) {
        SwingWorker<Schedule, Void> worker = new SwingWorker<>() {
            @Override
            protected Schedule doInBackground() throws Exception {
                if (scheduleDao.hasConflict(filmId, studioId, date, time, 0)) {
                    throw new IllegalArgumentException(
                            "Konflik jadwal: Studio sudah memiliki penayangan yang bertabrakan pada rentang waktu penayangan film tersebut.");
                }
                return scheduleDao.insert(filmId, studioId, date, time, price, totalSeats);
            }

            @Override
            protected void done() {
                try {
                    Schedule created = get();
                    view.setStatusSuccess("Jadwal #" + created.getId() + " berhasil disimpan.");
                    loadSchedules();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal menambah jadwal.";
                    AlertUtil.showError(view, "Gagal Menambah Jadwal", msg);
                }
            }
        };
        worker.execute();
    }

    private void handleEditSchedule() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat mengedit jadwal.");
            return;
        }

        long selectedId = view.getSelectedScheduleId();
        if (selectedId < 0) {
            AlertUtil.showWarning(view, "Peringatan", "Pilih jadwal yang ingin diedit.");
            return;
        }

        view.setStatus("Menyiapkan edit jadwal...");
        SwingWorker<EditData, Void> worker = new SwingWorker<>() {
            @Override
            protected EditData doInBackground() throws Exception {
                if (scheduleDao.hasActiveBookings(selectedId)) {
                    throw new IllegalStateException("Jadwal ini memiliki booking aktif. Mengubah jadwal dapat merusak transaksi!");
                }
                Schedule s = scheduleDao.findById(selectedId)
                        .orElseThrow(() -> new IllegalArgumentException("Jadwal tidak ditemukan."));
                List<ScheduleDao.FilmSummary> films = scheduleDao.findAvailableFilms();
                List<Studio> studios = studioDao.findAll();
                return new EditData(s, films, studios);
            }

            @Override
            protected void done() {
                try {
                    EditData data = get();
                    ScheduleFormDialog dialog = new ScheduleFormDialog(
                            SwingUtilities.getWindowAncestor(view), data.schedule(), data.films(), data.studios());
                    dialog.setVisible(true);

                    if (!dialog.isConfirmed()) {
                        return;
                    }

                    long filmId = dialog.getSelectedFilmId();
                    Studio studio = dialog.getSelectedStudio();
                    LocalDate date = dialog.getShowDate();
                    LocalTime time = dialog.getShowTime();
                    BigDecimal price = dialog.getTicketPrice();
                    int totalSeats = studio.getTotalCapacity();

                    updateSchedule(selectedId, filmId, studio.getId(), date, time, price, totalSeats);

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal memproses edit.";
                    AlertUtil.showError(view, "Peringatan", msg);
                }
            }
        };
        worker.execute();
    }

    private void updateSchedule(long id, long filmId, long studioId, LocalDate date, LocalTime time, BigDecimal price, int totalSeats) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (scheduleDao.hasConflict(filmId, studioId, date, time, id)) {
                    throw new IllegalArgumentException(
                            "Konflik jadwal: Studio sudah memiliki penayangan yang bertabrakan pada rentang waktu penayangan film tersebut.");
                }
                return scheduleDao.update(id, filmId, studioId, date, time, price, totalSeats);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        view.setStatusSuccess("Jadwal #" + id + " berhasil diperbarui.");
                        loadSchedules();
                    } else {
                        view.setStatusError("Gagal memperbarui jadwal.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal update jadwal.";
                    AlertUtil.showError(view, "Gagal Edit Jadwal", msg);
                }
            }
        };
        worker.execute();
    }

    private void handleDeleteSchedule() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat menghapus jadwal.");
            return;
        }

        long selectedId = view.getSelectedScheduleId();
        if (selectedId < 0) {
            AlertUtil.showWarning(view, "Peringatan", "Pilih jadwal yang ingin dihapus.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Apakah Anda yakin ingin menghapus jadwal #" + selectedId + "?\n"
                        + "Jika ada transaksi/booking terkait, penghapusan akan ditolak demi keamanan database.",
                "Konfirmasi Hapus Jadwal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (scheduleDao.hasAnyBookings(selectedId)) {
                    throw new IllegalStateException("Jadwal ini memiliki riwayat transaksi/booking dan tidak dapat dihapus.");
                }
                return scheduleDao.delete(selectedId);
            }

            @Override
            protected void done() {
                try {
                    boolean deleted = get();
                    if (deleted) {
                        view.setStatusSuccess("Jadwal #" + selectedId + " berhasil dihapus.");
                        loadSchedules();
                    } else {
                        view.setStatusError("Jadwal tidak ditemukan.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal menghapus jadwal.";
                    AlertUtil.showError(view, "Gagal Hapus Jadwal", msg);
                }
            }
        };
        worker.execute();
    }

    private record FormData(List<ScheduleDao.FilmSummary> films, List<Studio> studios) {}
    private record EditData(Schedule schedule, List<ScheduleDao.FilmSummary> films, List<Studio> studios) {}
}
