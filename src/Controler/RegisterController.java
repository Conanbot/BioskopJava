package Controler;

import Model.entity.User;
import Model.exception.ValidationException;
import Model.service.AuthService;
import View.AccountsPanel;
import View.RegisterPanel;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/**
 * Controller untuk menangani registrasi akun Customer baru.
 */
public class RegisterController {
    private final AuthService authService;
    private final RegisterPanel registerPanel;
    private final AccountsPanel accountsPanel;

    public RegisterController(
            AuthService authService,
            RegisterPanel registerPanel,
            AccountsPanel accountsPanel) {
        this.authService = Objects.requireNonNull(authService);
        this.registerPanel = Objects.requireNonNull(registerPanel);
        this.accountsPanel = Objects.requireNonNull(accountsPanel);

        initListeners();
    }

    private void initListeners() {
        registerPanel.getRegisterButton().addActionListener(e -> handleRegister());
        registerPanel.getBackToLoginButton().addActionListener(e -> {
            registerPanel.clearMessages();
            accountsPanel.showLogin();
        });
    }

    private void handleRegister() {
        String username = registerPanel.getUsername();
        char[] passwordChars = registerPanel.getPassword();
        char[] confirmChars = registerPanel.getConfirmPassword();
        String password = new String(passwordChars);
        String confirmPassword = new String(confirmChars);

        registerPanel.clearMessages();
        registerPanel.setFormEnabled(false);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.register(username, password, confirmPassword);
            }

            @Override
            protected void done() {
                registerPanel.clearSensitiveFields();
                registerPanel.setFormEnabled(true);
                try {
                    User newUser = get();
                    registerPanel.clearAll();
                    registerPanel.showSuccess("Registrasi berhasil untuk @" + newUser.getUsername() + "! Silakan kembali ke login.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    registerPanel.showError("Proses registrasi dibatalkan.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof ValidationException) {
                        registerPanel.showError(cause.getMessage());
                    } else if (cause instanceof SQLException) {
                        if (cause.getMessage() != null && cause.getMessage().contains("uq_users_username")) {
                            registerPanel.showError("Username sudah terdaftar. Gunakan username lain.");
                        } else {
                            registerPanel.showError("Gagal menyimpan ke database. Cek koneksi MySQL.");
                        }
                    } else {
                        registerPanel.showError("Terjadi kesalahan saat pendaftaran akun.");
                    }
                }
            }
        };

        worker.execute();
    }
}
