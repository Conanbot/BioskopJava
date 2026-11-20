package View;

import View.theme.SwingTheme;

import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JPanel;

/**
 * Panel induk untuk modul akun: menampung LoginPanel dan RegisterPanel menggunakan CardLayout.
 * Ini yang menggantikan placeholder "accounts" pada MainFrameController.
 */
public final class AccountsPanel extends javax.swing.JPanel {
    public static final String CARD_LOGIN = "login";
    public static final String CARD_REGISTER = "register";

    public AccountsPanel() {
        initComponents();
        postInit();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        cardLayout = new CardLayout();
        loginPanel = new LoginPanel();
        registerPanel = new RegisterPanel();

        setLayout(cardLayout);
        setBackground(SwingTheme.BACKGROUND);

        add(loginPanel, CARD_LOGIN);
        add(registerPanel, CARD_REGISTER);
    }// </editor-fold>//GEN-END:initComponents

    private void postInit() {
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private CardLayout cardLayout;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    // End of variables declaration//GEN-END:variables
}
