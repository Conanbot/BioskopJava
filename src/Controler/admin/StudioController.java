package Controler.admin;

import Controler.AlertUtil;
import Controler.SessionManager;
import Model.entity.Studio;
import Model.dao.StudioDao;
import View.admin.StudioFormDialog;
import View.admin.StudioManagementPanel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Controller untuk CRUD Studio.
 */
public class StudioController {
    private final StudioDao studioDao;
    private final StudioManagementPanel view;

    public StudioController(StudioDao studioDao, StudioManagementPanel view) {
        this.studioDao = Objects.requireNonNull(studioDao);
        this.view = Objects.requireNonNull(view);

        initListeners();
    }

    private void initListeners() {
        view.getRefreshButton().addActionListener(e -> loadStudios());
        view.getAddButton().addActionListener(e -> handleAddStudio());
        view.getEditButton().addActionListener(e -> handleEditStudio());
        view.getDeleteButton().addActionListener(e -> handleDeleteStudio());
    }

    public void loadStudios() {
        view.setStatus("Memuat daftar studio...");
        SwingWorker<List<Studio>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Studio> doInBackground() throws Exception {
                return studioDao.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Studio> list = get();
                    view.populateTable(list);
                    view.setStatus("Total studio: " + list.size());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.setStatusError("Gagal memuat studio.");
                } catch (ExecutionException ex) {
                    view.setStatusError("Gagal memuat data studio dari database.");
                }
            }
        };
        worker.execute();
    }

    private void handleAddStudio() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat menambah studio.");
            return;
        }

        StudioFormDialog dialog = new StudioFormDialog(SwingUtilities.getWindowAncestor(view), null);
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            return;
        }

        String name = dialog.getStudioName();
        int rows = dialog.getSeatRows();
        int cols = dialog.getSeatCols();

        SwingWorker<Studio, Void> worker = new SwingWorker<>() {
            @Override
            protected Studio doInBackground() throws Exception {
                if (studioDao.nameExistsForOther(name, 0)) {
                    throw new IllegalArgumentException("Nama studio '" + name + "' sudah digunakan.");
                }
                return studioDao.insert(name, rows, cols);
            }

            @Override
            protected void done() {
                try {
                    Studio created = get();
                    view.setStatusSuccess("Studio '" + created.getName() + "' berhasil ditambahkan.");
                    loadStudios();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal menambah studio.";
                    AlertUtil.showError(view, "Gagal Menambah Studio", msg);
                }
            }
        };
        worker.execute();
    }

    private void handleEditStudio() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat mengedit studio.");
            return;
        }

        Studio selected = view.getSelectedStudio();
        if (selected == null) {
            AlertUtil.showWarning(view, "Peringatan", "Pilih studio yang ingin diedit.");
            return;
        }

        StudioFormDialog dialog = new StudioFormDialog(SwingUtilities.getWindowAncestor(view), selected);
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            return;
        }

        String name = dialog.getStudioName();
        int rows = dialog.getSeatRows();
        int cols = dialog.getSeatCols();

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (studioDao.nameExistsForOther(name, selected.getId())) {
                    throw new IllegalArgumentException("Nama studio '" + name + "' sudah digunakan.");
                }
                if ((rows != selected.getSeatRows() || cols != selected.getSeatCols())
                        && studioDao.isUsedBySchedule(selected.getId())) {
                    throw new IllegalStateException(
                            "Dimensi kursi studio (baris/kolom) tidak dapat diubah karena studio sudah digunakan oleh jadwal tayang.");
                }
                return studioDao.update(selected.getId(), name, rows, cols);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        view.setStatusSuccess("Studio '" + name + "' berhasil diperbarui.");
                        loadStudios();
                    } else {
                        view.setStatusError("Studio tidak ditemukan atau tidak berubah.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal memperbarui studio.";
                    AlertUtil.showError(view, "Gagal Edit Studio", msg);
                }
            }
        };
        worker.execute();
    }

    private void handleDeleteStudio() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError(view, "Akses Ditolak", "Hanya Admin yang dapat menghapus studio.");
            return;
        }

        Studio selected = view.getSelectedStudio();
        if (selected == null) {
            AlertUtil.showWarning(view, "Peringatan", "Pilih studio yang ingin dihapus.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Apakah Anda yakin ingin menghapus studio '" + selected.getName() + "'?\n"
                        + "Jika studio masih digunakan pada jadwal tayang, penghapusan akan ditolak.",
                "Konfirmasi Hapus Studio",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (studioDao.isUsedBySchedule(selected.getId())) {
                    throw new IllegalStateException("Studio '" + selected.getName() + "' masih digunakan oleh jadwal tayang aktif.");
                }
                return studioDao.delete(selected.getId());
            }

            @Override
            protected void done() {
                try {
                    boolean deleted = get();
                    if (deleted) {
                        view.setStatusSuccess("Studio '" + selected.getName() + "' berhasil dihapus.");
                        loadStudios();
                    } else {
                        view.setStatusError("Studio tidak ditemukan.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : "Gagal menghapus studio.";
                    AlertUtil.showError(view, "Gagal Hapus Studio", msg);
                }
            }
        };
        worker.execute();
    }
}
