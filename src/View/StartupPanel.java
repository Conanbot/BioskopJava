package View;

import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public final class StartupPanel extends javax.swing.JPanel {

    public StartupPanel() {
        initComponents();
        postInit();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        databaseStatus = new JLabel();
        databaseDetails = new JTextArea();
        connectionButton = new JButton("Periksa koneksi MySQL");
        dialogButton = new JButton("Uji dialog");

        setLayout(new BorderLayout(0, 24));
        setBackground(SwingTheme.BACKGROUND);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        JPanel statusPanel = new JPanel(new BorderLayout(0, 10));
        statusPanel.setBackground(SwingTheme.SURFACE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(140, 133, 119)),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel sectionTitle = new JLabel("Status database");
        sectionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        sectionTitle.setForeground(SwingTheme.TEXT);
        databaseStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        databaseStatus.setForeground(SwingTheme.TEXT);
        databaseDetails.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        databaseDetails.setForeground(SwingTheme.MUTED_TEXT);
        databaseDetails.setBackground(SwingTheme.SURFACE);
        databaseDetails.setLineWrap(true);
        databaseDetails.setWrapStyleWord(true);
        databaseDetails.setEditable(false);
        databaseDetails.setFocusable(false);
        databaseDetails.setRows(2);

        JPanel statusContent = new JPanel(new BorderLayout(0, 8));
        statusContent.setOpaque(false);
        statusContent.add(databaseStatus, BorderLayout.NORTH);
        statusContent.add(databaseDetails, BorderLayout.CENTER);
        statusPanel.add(sectionTitle, BorderLayout.NORTH);
        statusPanel.add(statusContent, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.CENTER);

        SwingTheme.styleButton(connectionButton, true);
        SwingTheme.styleButton(dialogButton, false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        actions.add(connectionButton);
        actions.add(dialogButton);

        JLabel note = new JLabel(
                "<html>Pemeriksaan ini hanya memvalidasi koneksi dan struktur tabel. "
                + "Data transaksi tidak diubah.</html>");
        note.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        note.setForeground(SwingTheme.MUTED_TEXT);
        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        footer.add(actions, BorderLayout.NORTH);
        footer.add(note, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void postInit() {
        // Post-init logic (none needed currently)
    }

    public JLabel getDatabaseStatus() {
        return databaseStatus;
    }

    public JTextArea getDatabaseDetails() {
        return databaseDetails;
    }

    public JButton getConnectionButton() {
        return connectionButton;
    }

    public JButton getDialogButton() {
        return dialogButton;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel databaseStatus;
    private JTextArea databaseDetails;
    private JButton connectionButton;
    private JButton dialogButton;
    // End of variables declaration//GEN-END:variables
}
