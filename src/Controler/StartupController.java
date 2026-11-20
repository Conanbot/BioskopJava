package Controler;

import Model.service.DatabaseHealthService;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

public class StartupController {
    private final DatabaseHealthService healthService;
    private final JLabel databaseStatus;
    private final JTextArea databaseDetails;
    private final JButton connectionButton;
    private final Runnable onDatabaseReady;

    public StartupController(
            DatabaseHealthService healthService,
            JLabel databaseStatus,
            JTextArea databaseDetails,
            JButton connectionButton) {
        this(healthService, databaseStatus, databaseDetails, connectionButton, null);
    }

    public StartupController(
            DatabaseHealthService healthService,
            JLabel databaseStatus,
            JTextArea databaseDetails,
            JButton connectionButton,
            Runnable onDatabaseReady) {
        this.healthService = Objects.requireNonNull(healthService);
        this.databaseStatus = Objects.requireNonNull(databaseStatus);
        this.databaseDetails = Objects.requireNonNull(databaseDetails);
        this.connectionButton = Objects.requireNonNull(connectionButton);
        this.onDatabaseReady = onDatabaseReady;

        databaseStatus.setText("Belum diperiksa");
        databaseDetails.setText("Periksa koneksi sebelum menjalankan transaksi.");
        connectionButton.addActionListener(event -> checkDatabaseConnection());

        // Jalankan periksa koneksi otomatis pada saat startup sesuai Flowchart 1
        SwingUtilities.invokeLater(this::checkDatabaseConnection);
    }

    public void checkDatabaseConnection() {
        connectionButton.setEnabled(false);
        databaseStatus.setText("Memeriksa database...");
        databaseDetails.setText("Menguji koneksi MySQL, tabel wajib, dan indeks unik kursi...");

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
                    databaseStatus.setText("Database siap");
                    databaseDetails.setText("Koneksi dan tujuh tabel inti berhasil diperiksa. Mengalihkan ke Login / Register...");
                    if (onDatabaseReady != null) {
                        Timer timer = new Timer(600, evt -> onDatabaseReady.run());
                        timer.setRepeats(false);
                        timer.start();
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showFailure("Pemeriksaan koneksi dihentikan. Silakan coba lagi.");
                } catch (ExecutionException exception) {
                    Throwable failure = exception.getCause();
                    showFailure(failure == null ? exception.getMessage() : failure.getMessage());
                } finally {
                    connectionButton.setEnabled(true);
                }
            }
        };
        checkTask.execute();
    }

    private void showFailure(String message) {
        databaseStatus.setText("Database belum siap");
        databaseDetails.setText((message == null || message.isBlank()
                ? "Pemeriksaan koneksi gagal."
                : message)
                + "\n\nPastikan MySQL aktif (Laragon/XAMPP). Perbaiki config lalu klik tombol di bawah untuk cek ulang.");
    }
}
