package View;

import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Panel Login untuk autentikasi pengguna.
 * Komponen GUI responsif dan ramah ukuran layar.
 */
public final class LoginPanel extends javax.swing.JPanel {

    public LoginPanel() {
        initComponents();
        postInit();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Masuk");
        registerButton = new JButton("Daftar Akun Baru");
        errorLabel = new JLabel(" ");

        setLayout(new BorderLayout());
        setBackground(SwingTheme.BACKGROUND);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(SwingTheme.BACKGROUND);

        JPanel formCard = buildFormCard();
        centerWrapper.add(formCard);

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);
        scrollPane.setBackground(SwingTheme.BACKGROUND);
        scrollPane.getViewport().setBackground(SwingTheme.BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void postInit() {
        // Post-init logic
    }

    private JPanel buildFormCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SwingTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(140, 133, 119)),
                new EmptyBorder(34, 40, 34, 40)));
        card.setPreferredSize(new Dimension(400, 420));
        card.setMaximumSize(new Dimension(440, 460));

        JLabel title = new JLabel("Masuk ke Akun");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(SwingTheme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sistem Booking Tiket Bioskop");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(SwingTheme.MUTED_TEXT);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Form fields
        JPanel fieldsPanel = buildFieldsPanel();

        // Error label
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        errorLabel.setForeground(new java.awt.Color(220, 80, 80));
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Buttons
        SwingTheme.styleButton(loginButton, true);
        SwingTheme.styleButton(registerButton, false);
        loginButton.setAlignmentX(CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerButton.setAlignmentX(CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(fieldsPanel);
        card.add(Box.createVerticalStrut(6));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(8));
        card.add(registerButton);

        return card;
    }

    private JPanel buildFieldsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(0, 0, 4, 0);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1.0;
        fieldGbc.insets = new Insets(0, 0, 14, 0);

        // Style text fields
        styleField(usernameField);
        styleField(passwordField);

        labelGbc.gridy = 0;
        panel.add(createFieldLabel("Username"), labelGbc);
        fieldGbc.gridy = 1;
        panel.add(usernameField, fieldGbc);

        labelGbc.gridy = 2;
        panel.add(createFieldLabel("Password"), labelGbc);
        fieldGbc.gridy = 3;
        panel.add(passwordField, fieldGbc);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        label.setForeground(SwingTheme.TEXT);
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        field.setBackground(SwingTheme.BACKGROUND);
        field.setForeground(SwingTheme.TEXT);
        field.setCaretColor(SwingTheme.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(100, 95, 85)),
                new EmptyBorder(8, 10, 8, 10)));
        field.setPreferredSize(new Dimension(280, 40));
    }

    // ── Getters untuk Controller ──────────────────────────────────────────────

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public void clearPassword() {
        passwordField.setText("");
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
    }

    public void showError(String message) {
        errorLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public void setLoginEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        registerButton.setEnabled(enabled);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel errorLabel;
    // End of variables declaration//GEN-END:variables
}
