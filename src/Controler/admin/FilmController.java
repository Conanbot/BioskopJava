package Controler.admin;

import Controler.AlertUtil;
import Controler.SessionManager;
import Model.dao.FilmDao;
import Model.entity.Film;
import View.admin.FilmFormDialog;
import View.admin.ManageFilm;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Controller untuk CRUD Pengelolaan Film (Admin).
 */
public class FilmController {
    private final FilmDao filmDao;
    private final ManageFilm view;

    public FilmController(FilmDao filmDao, ManageFilm view) {
        this.filmDao = Objects.requireNonNull(filmDao);
        this.view = Objects.requireNonNull(view);

        initListeners();
    }

    private void initListeners() {
        view.getRefreshButton().addActionListener(e -> {
            view.clearSearch();
            loadFilms();
        });
        view.getSearchButton().addActionListener(e -> handleSearchFilms());
        view.getSearchField().addActionListener(e -> handleSearchFilms());
        view.getAddButton().addActionListener(e -> handleAddFilm());
        view.getEditButton().addActionListener(e -> handleEditFilm());
        view.getDeleteButton().addActionListener(e -> handleDeleteFilm());
    }

    public void loadFilms() {
        view.setStatus("Memuat daftar film...");
        SwingWorker<List<Film>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Film> doInBackground() throws Exception {
                return filmDao.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Film> list = get();
                    view.populateTable(list);
                    view.setStatus("Total film: " + list.size());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.setStatusError("Gagal memuat daftar film.");
                } catch (ExecutionException ex) {
                    view.setStatusError("Gagal memuat data film dari database.");
                }
            }
        };
        worker.execute();
    }

    private void handleSearchFilms() {
        String keyword = view.getSearchKeyword();
        if (keyword.isEmpty()) {
            loadFilms();
            return;
        }

        view.setStatus("Mencari film \"" + keyword + "\"...");
        SwingWorker<List<Film>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Film> doInBackground() throws Exception {
                return filmDao.searchFilms(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<Film> list = get();
                    view.populateTable(list);
                    view.setStatus("Ditemukan " + list.size() + " film untuk \"" + keyword + "\".");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.setStatusError("Pencarian film dibatalkan.");
                } catch (ExecutionException ex) {
                    view.setStatusError("Gagal mencari data film di database.");
                }
            }
        };
        worker.execute();
    }

    private void handleAddFilm() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat menambah film.");
            return;
        }

        FilmFormDialog dialog = new FilmFormDialog(SwingUtilities.getWindowAncestor(view), null);
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            return;
        }

        String title = dialog.getFilmTitle();
        String genre = dialog.getGenre();
        int duration = dialog.getDurationMinutes();
        String ageRating = dialog.getAgeRating();
        String status = dialog.getFilmStatus();
        String synopsis = dialog.getSynopsis();

        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                if (filmDao.titleExistsForOther(title, 0)) {
                    throw new IllegalArgumentException("Judul film '" + title + "' sudah terdaftar.");
                }
                Film newFilm = new Film(0, title, genre, duration, ageRating, synopsis, status);
                return filmDao.insert(newFilm);
            }

            @Override
            protected void done() {
                try {
                    get();
                    view.setStatusSuccess("Film '" + title + "' berhasil ditambahkan.");
                    loadFilms();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal menambah film.";
                    AlertUtil.showError(view, "Gagal Menambah Film", msg);
                }
            }
        };
        worker.execute();
    }

    private void handleEditFilm() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat mengedit film.");
            return;
        }

        Film selected = view.getSelectedFilm();
        if (selected == null) {
            AlertUtil.showWarning(view, "Peringatan", "Pilih film yang ingin diedit.");
            return;
        }

        FilmFormDialog dialog = new FilmFormDialog(SwingUtilities.getWindowAncestor(view), selected);
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            return;
        }

        String title = dialog.getFilmTitle();
        String genre = dialog.getGenre();
        int duration = dialog.getDurationMinutes();
        String ageRating = dialog.getAgeRating();
        String status = dialog.getFilmStatus();
        String synopsis = dialog.getSynopsis();

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (filmDao.titleExistsForOther(title, selected.getId())) {
                    throw new IllegalArgumentException("Judul film '" + title + "' sudah digunakan film lain.");
                }
                Film updatedFilm = new Film(
                        selected.getId(),
                        title,
                        genre,
                        duration,
                        ageRating,
                        synopsis,
                        selected.getPosterPath(),
                        status,
                        selected.getCreatedAt());
                return filmDao.update(updatedFilm);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        view.setStatusSuccess("Film '" + title + "' berhasil diperbarui.");
                        loadFilms();
                    } else {
                        view.setStatusError("Film tidak ditemukan atau tidak berubah.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal memperbarui film.";
                    AlertUtil.showError(view, "Gagal Edit Film", msg);
                }
            }
        };
        worker.execute();
    }

    private void handleDeleteFilm() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat menghapus film.");
            return;
        }

        Film selected = view.getSelectedFilm();
        if (selected == null) {
            AlertUtil.showWarning(view, "Peringatan", "Pilih film yang ingin dihapus.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Apakah Anda yakin ingin menghapus film '" + selected.getTitle() + "'?\n"
                        + "Jika film masih digunakan pada jadwal tayang, penghapusan akan ditolak.",
                "Konfirmasi Hapus Film",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (filmDao.isUsedBySchedule(selected.getId())) {
                    throw new IllegalStateException(
                            "Film '" + selected.getTitle() + "' masih digunakan oleh jadwal tayang aktif.");
                }
                return filmDao.delete(selected.getId());
            }

            @Override
            protected void done() {
                try {
                    boolean deleted = get();
                    if (deleted) {
                        view.setStatusSuccess("Film '" + selected.getTitle() + "' berhasil dihapus.");
                        loadFilms();
                    } else {
                        view.setStatusError("Film tidak ditemukan.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal menghapus film.";
                    AlertUtil.showError(view, "Gagal Hapus Film", msg);
                }
            }
        };
        worker.execute();
    }
}
