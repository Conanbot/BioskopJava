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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Panel Register untuk pendaftaran akun Customer baru.
 * Registrasi publik hanya membuat akun CUSTOMER — tidak ada pilihan role Admin.
 */
public final class RegisterPanel extends JPanel {
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmPasswordField = new JPasswordField(20);
    private final JButton registerButton = new JButton("Daftar");
    private final JButton backToLoginButton = new JButton("Kembali ke Login");
    private final JLabel errorLabel = new JLabel(" ");
    private final JLabel successLabel = new JLabel(" ");

    public RegisterPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingTheme.BACKGROUND);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(SwingTheme.BACKGROUND);

        JPanel formCard = buildFormCard();
        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel buildFormCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SwingTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(140, 133, 119)),
                new EmptyBorder(36, 44, 36, 44)));
        card.setPreferredSize(new Dimension(400, 460));

        JLabel title = new JLabel("Buat Akun Baru");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(SwingTheme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Akun Customer — Sistem Booking Bioskop");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(SwingTheme.MUTED_TEXT);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        JPanel fieldsPanel = buildFieldsPanel();

        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        errorLabel.setForeground(new java.awt.Color(220, 80, 80));
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        successLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        successLabel.setForeground(new java.awt.Color(80, 200, 120));
        successLabel.setAlignmentX(CENTER_ALIGNMENT);

        SwingTheme.styleButton(registerButton, true);
        SwingTheme.styleButton(backToLoginButton, false);
        registerButton.setAlignmentX(CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        backToLoginButton.setAlignmentX(CENTER_ALIGNMENT);
        backToLoginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        card.add(fieldsPanel);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(successLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(10));
        card.add(backToLoginButton);

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

        styleField(usernameField);
        styleField(passwordField);
        styleField(confirmPasswordField);

        labelGbc.gridy = 0;
        panel.add(createFieldLabel("Username"), labelGbc);
        fieldGbc.gridy = 1;
        panel.add(usernameField, fieldGbc);

        labelGbc.gridy = 2;
        panel.add(createFieldLabel("Password (min. 6 karakter)"), labelGbc);
        fieldGbc.gridy = 3;
        panel.add(passwordField, fieldGbc);

        labelGbc.gridy = 4;
        panel.add(createFieldLabel("Konfirmasi Password"), labelGbc);
        fieldGbc.gridy = 5;
        panel.add(confirmPasswordField, fieldGbc);

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
        field.setPreferredSize(new Dimension(280, 42));
    }

    // ── Getters untuk Controller ──────────────────────────────────────────────

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public char[] getConfirmPassword() {
        return confirmPasswordField.getPassword();
    }

    public void clearSensitiveFields() {
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    public void clearAll() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        errorLabel.setText(" ");
        successLabel.setText(" ");
    }

    public void showError(String message) {
        successLabel.setText(" ");
        errorLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    public void showSuccess(String message) {
        errorLabel.setText(" ");
        successLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    public void clearMessages() {
        errorLabel.setText(" ");
        successLabel.setText(" ");
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getBackToLoginButton() {
        return backToLoginButton;
    }

    public void setFormEnabled(boolean enabled) {
        registerButton.setEnabled(enabled);
        backToLoginButton.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        confirmPasswordField.setEnabled(enabled);
    }
}
