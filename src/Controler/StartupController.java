package Controler;

import Model.service.DatabaseHealthService;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class StartupController {
    private final DatabaseHealthService healthService;
    private final JLabel databaseStatus;
    private final JTextArea databaseDetails;
    private final JButton connectionButton;

    public StartupController(
            DatabaseHealthService healthService,
            JLabel databaseStatus,
            JTextArea databaseDetails,
            JButton connectionButton) {
        this.healthService = Objects.requireNonNull(healthService);
        this.databaseStatus = Objects.requireNonNull(databaseStatus);
        this.databaseDetails = Objects.requireNonNull(databaseDetails);
        this.connectionButton = Objects.requireNonNull(connectionButton);

        databaseStatus.setText("Belum diperiksa");
        databaseDetails.setText("Periksa koneksi sebelum menjalankan transaksi.");
        connectionButton.addActionListener(event -> checkDatabaseConnection());
    }

    private void checkDatabaseConnection() {
        connectionButton.setEnabled(false);
        databaseStatus.setText("Memeriksa database");
        databaseDetails.setText("Menguji koneksi dan tabel yang dibutuhkan.");

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
                    databaseDetails.setText("Koneksi dan tujuh tabel inti berhasil diperiksa.");
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
        databaseDetails.setText(message == null || message.isBlank()
                ? "Pemeriksaan gagal. Periksa konfigurasi dan koneksi MySQL."
                : message);
    }
}
