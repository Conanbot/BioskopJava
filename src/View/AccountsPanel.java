package View;

import View.theme.SwingTheme;

import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JPanel;

/**
 * Panel induk untuk modul akun: menampung LoginPanel dan RegisterPanel menggunakan CardLayout.
 * Ini yang menggantikan placeholder "accounts" pada MainFrameController.
 */
public final class AccountsPanel extends JPanel {
    public static final String CARD_LOGIN = "login";
    public static final String CARD_REGISTER = "register";

    private final CardLayout cardLayout = new CardLayout();
    private final LoginPanel loginPanel = new LoginPanel();
    private final RegisterPanel registerPanel = new RegisterPanel();

    public AccountsPanel() {
        setLayout(cardLayout);
        setBackground(SwingTheme.BACKGROUND);

        add(loginPanel, CARD_LOGIN);
        add(registerPanel, CARD_REGISTER);

        showLogin();
    }

    public void showLogin() {
        cardLayout.show(this, CARD_LOGIN);
    }

    public void showRegister() {
        cardLayout.show(this, CARD_REGISTER);
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    public RegisterPanel getRegisterPanel() {
        return registerPanel;
    }
}
