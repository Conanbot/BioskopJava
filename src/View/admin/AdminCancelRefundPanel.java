package View.admin;

import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Panel Cancel/Refund Booking untuk Admin.
 * Admin memasukkan kode booking, memilih tindakan CANCEL atau REFUND,
 * lalu menekan tombol untuk memproses melalui BookingService.cancelOrRefundBooking().
 */
public class AdminCancelRefundPanel extends javax.swing.JPanel {



    public AdminCancelRefundPanel() {
        initComponents();
        postInit();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bookingCodeField = new JTextField(20);
        resolutionCombo = new JComboBox<>(new String[]{"CANCEL", "REFUND"});
        processButton = new JButton("Proses");
        clearButton = new JButton("Reset");
        resultArea = new JTextArea(8, 40);
        statusLabel = new JLabel(" ");

        setLayout(new BorderLayout(0, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildFormAndResult(), BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void postInit() {
        setBackground(SwingTheme.BACKGROUND);
        setBorder(new EmptyBorder(28, 32, 28, 32));
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("Batalkan / Refund Booking");
        title.setFont(new Font("Inter", Font.BOLD, 22));
        title.setForeground(SwingTheme.TEXT);

        JLabel subtitle = new JLabel("Proses pembatalan atau refund booking atas nama Admin.");
        subtitle.setFont(new Font("Inter", Font.PLAIN, 13));
        subtitle.setForeground(SwingTheme.MUTED_TEXT);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        return header;
    }

    private JPanel buildFormAndResult() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);

        panel.add(buildFormCard(), BorderLayout.NORTH);
        panel.add(buildResultCard(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildFormCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(SwingTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(140, 133, 119)),
                new EmptyBorder(20, 24, 20, 24)));

        GridBagConstraints lblGbc = new GridBagConstraints();
        lblGbc.anchor = GridBagConstraints.WEST;
        lblGbc.insets = new Insets(0, 0, 4, 12);

        GridBagConstraints fldGbc = new GridBagConstraints();
        fldGbc.fill = GridBagConstraints.HORIZONTAL;
        fldGbc.insets = new Insets(0, 0, 14, 0);
        fldGbc.weightx = 1.0;

        // Styling
        styleField(bookingCodeField);
        bookingCodeField.setToolTipText("Contoh: BKG-20261119-AB12");

        resolutionCombo.setFont(new Font("Inter", Font.PLAIN, 14));
        resolutionCombo.setBackground(SwingTheme.BACKGROUND);
        resolutionCombo.setForeground(SwingTheme.TEXT);
        resolutionCombo.setPreferredSize(new Dimension(180, 42));

        SwingTheme.styleButton(processButton, true);
        SwingTheme.styleButton(clearButton, false);

        JLabel warningLabel = new JLabel(
                "<html><small>Tindakan ini tidak dapat dibatalkan setelah diproses.</small></html>");
        warningLabel.setForeground(new java.awt.Color(220, 160, 60));
        warningLabel.setFont(new Font("Inter", Font.ITALIC, 12));

        // Row 0 - Booking Code
        lblGbc.gridx = 0; lblGbc.gridy = 0;
        card.add(createLabel("Kode Booking"), lblGbc);
        fldGbc.gridx = 1; fldGbc.gridy = 0;
        card.add(bookingCodeField, fldGbc);

        // Row 1 - Resolution
        lblGbc.gridy = 1;
        card.add(createLabel("Tindakan"), lblGbc);
        fldGbc.gridy = 1;
        fldGbc.fill = GridBagConstraints.NONE;
        card.add(resolutionCombo, fldGbc);

        // Row 2 - Warning
        GridBagConstraints warnGbc = new GridBagConstraints();
        warnGbc.gridx = 1; warnGbc.gridy = 2;
        warnGbc.anchor = GridBagConstraints.WEST;
        warnGbc.insets = new Insets(0, 0, 14, 0);
        card.add(warningLabel, warnGbc);

        // Row 3 - Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(processButton);
        buttonPanel.add(clearButton);
        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.gridx = 1; btnGbc.gridy = 3;
        btnGbc.anchor = GridBagConstraints.WEST;
        card.add(buttonPanel, btnGbc);

        // Row 4 - Status
        GridBagConstraints statusGbc = new GridBagConstraints();
        statusGbc.gridx = 1; statusGbc.gridy = 4;
        statusGbc.anchor = GridBagConstraints.WEST;
        statusGbc.insets = new Insets(8, 0, 0, 0);
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        statusLabel.setForeground(SwingTheme.MUTED_TEXT);
        card.add(statusLabel, statusGbc);

        return card;
    }

    private JPanel buildResultCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(SwingTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(140, 133, 119)),
                new EmptyBorder(16, 20, 16, 20)));

        JLabel resultTitle = new JLabel("Hasil Proses");
        resultTitle.setFont(new Font("Inter", Font.BOLD, 14));
        resultTitle.setForeground(SwingTheme.MUTED_TEXT);

        resultArea.setEditable(false);
        resultArea.setFocusable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBackground(SwingTheme.BACKGROUND);
        resultArea.setForeground(SwingTheme.TEXT);
        resultArea.setFont(new Font("Inter", Font.PLAIN, 13));
        resultArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        resultArea.setText("Belum ada proses dijalankan.");

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBorder(BorderFactory.createLineBorder(new java.awt.Color(60, 56, 50)));

        card.add(resultTitle, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.BOLD, 13));
        label.setForeground(SwingTheme.TEXT);
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBackground(SwingTheme.BACKGROUND);
        field.setForeground(SwingTheme.TEXT);
        field.setCaretColor(SwingTheme.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(100, 95, 85)),
                new EmptyBorder(8, 10, 8, 10)));
    }

    // ── Getters / State ───────────────────────────────────────────────────────

    public String getBookingCode() {
        return bookingCodeField.getText().trim();
    }

    public String getSelectedResolution() {
        return (String) resolutionCombo.getSelectedItem();
    }

    public void clearForm() {
        bookingCodeField.setText("");
        resolutionCombo.setSelectedIndex(0);
        resultArea.setText("Belum ada proses dijalankan.");
        statusLabel.setText(" ");
    }

    public void setResultText(String text) {
        resultArea.setText(text == null ? "" : text);
    }

    public void setStatusLoading() {
        statusLabel.setText("Memproses...");
        statusLabel.setForeground(SwingTheme.MUTED_TEXT);
        processButton.setEnabled(false);
    }

    public void setStatusReady() {
        statusLabel.setText(" ");
        processButton.setEnabled(true);
    }

    public void setStatusSuccess(String message) {
        statusLabel.setText(message == null ? "Berhasil." : message);
        statusLabel.setForeground(new java.awt.Color(80, 200, 120));
        processButton.setEnabled(true);
    }

    public void setStatusError(String message) {
        statusLabel.setText(message == null ? "Terjadi kesalahan." : message);
        statusLabel.setForeground(new java.awt.Color(220, 80, 80));
        processButton.setEnabled(true);
    }

    public JButton getProcessButton() { return processButton; }
    public JButton getClearButton() { return clearButton; }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bookingCodeField;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton processButton;
    private javax.swing.JComboBox<String> resolutionCombo;
    private javax.swing.JTextArea resultArea;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
}
