package Controler;

import Model.entity.User;
import Model.enums.Role;
import Model.exception.ValidationException;
import Model.service.AuthService;
import View.AccountsPanel;
import View.LoginPanel;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/**
 * Controller untuk menangani alur Login.
 * Berinteraksi dengan AuthService, mengupdate SessionManager,
 * dan memicu navigasi berdasarkan role pengguna.
 */
public class LoginController {
    private final AuthService authService;
    private final LoginPanel loginPanel;
    private final AccountsPanel accountsPanel;
    private final NavigationCallback navigationCallback;

    @FunctionalInterface
    public interface NavigationCallback {
        void onLoginSuccess(User user);
    }

    public LoginController(
            AuthService authService,
            LoginPanel loginPanel,
            AccountsPanel accountsPanel,
            NavigationCallback navigationCallback) {
        this.authService = Objects.requireNonNull(authService);
        this.loginPanel = Objects.requireNonNull(loginPanel);
        this.accountsPanel = Objects.requireNonNull(accountsPanel);
        this.navigationCallback = Objects.requireNonNull(navigationCallback);

        initListeners();
    }

    private void initListeners() {
        loginPanel.getLoginButton().addActionListener(e -> handleLogin());
        loginPanel.getRegisterButton().addActionListener(e -> {
            loginPanel.clearError();
            accountsPanel.showRegister();
        });
    }

    private void handleLogin() {
        String username = loginPanel.getUsername();
        char[] passwordChars = loginPanel.getPassword();
        String password = new String(passwordChars);

        loginPanel.clearError();
        loginPanel.setLoginEnabled(false);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                loginPanel.clearPassword();
                loginPanel.setLoginEnabled(true);
                try {
                    User user = get();
                    // Simpan identitas ke session terpercaya
                    SessionManager.getInstance().createSession(user);
                    loginPanel.clearFields();
                    // Callback navigasi ke MainFrameController
                    navigationCallback.onLoginSuccess(user);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    loginPanel.showError("Proses login dibatalkan.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof ValidationException || cause instanceof AuthService.AuthenticationException) {
                        loginPanel.showError(cause.getMessage());
                    } else if (cause instanceof SQLException) {
                        loginPanel.showError("Koneksi database gagal. Periksa konfigurasi MySQL.");
                    } else {
                        loginPanel.showError("Terjadi kesalahan pada sistem saat login.");
                    }
                }
            }
        };

        worker.execute();
    }
}
