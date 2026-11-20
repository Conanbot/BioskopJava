package View;

import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public final class PendingModulePanel extends javax.swing.JPanel {

    /**
     * Default constructor for NetBeans GUI Builder Design tab.
     */
    public PendingModulePanel() {
        this("Module", "Owner", List.of("Part"));
    }

    public PendingModulePanel(String moduleTitle, String owner, List<String> pendingParts) {
        Objects.requireNonNull(moduleTitle, "Judul modul wajib diisi.");
        Objects.requireNonNull(owner, "Pemilik modul wajib diisi.");
        Objects.requireNonNull(pendingParts, "Daftar bagian modul wajib diisi.");
        if (moduleTitle.isBlank() || owner.isBlank() || pendingParts.isEmpty()) {
            throw new IllegalArgumentException("Informasi placeholder modul wajib lengkap.");
        }

        this.moduleTitleText = moduleTitle;
        this.ownerText = owner;
        this.pendingPartsData = pendingParts;

        initComponents();
        postInit(moduleTitle, owner, pendingParts);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        headingPanel = new JPanel(new BorderLayout(0, 8));
        titleLabel = new JLabel();
        statusLabel = new JLabel();
        detailsArea = new JTextArea();
        noticeLabel = new JLabel();

        setLayout(new BorderLayout(0, 16));
        setBackground(SwingTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(140, 133, 119)),
                new EmptyBorder(24, 26, 24, 26)));

        headingPanel.setOpaque(false);
        headingPanel.add(titleLabel, BorderLayout.NORTH);
        headingPanel.add(statusLabel, BorderLayout.CENTER);
        add(headingPanel, BorderLayout.NORTH);

        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setEditable(false);
        detailsArea.setFocusable(false);
        add(detailsArea, BorderLayout.CENTER);

        add(noticeLabel, BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void postInit(String moduleTitle, String owner, List<String> pendingParts) {
        titleLabel.setText(moduleTitle);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(SwingTheme.TEXT);

        statusLabel.setText("Menunggu source dari " + owner);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        statusLabel.setForeground(SwingTheme.GOLD);

        detailsArea.setText(String.join("\n", pendingParts));
        detailsArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        detailsArea.setForeground(SwingTheme.MUTED_TEXT);
        detailsArea.setBackground(SwingTheme.SURFACE);
        detailsArea.setRows(Math.max(4, pendingParts.size()));

        noticeLabel.setText(
                "<html>Belum ada implementasi yang dijalankan pada halaman ini. "
                + "Fungsinya akan tersedia setelah source pemilik modul diintegrasikan.</html>");
        noticeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        noticeLabel.setForeground(SwingTheme.MUTED_TEXT);

        // TODO: Replace this placeholder with its owner's real Swing view and controller.
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel headingPanel;
    private JLabel titleLabel;
    private JLabel statusLabel;
    private JTextArea detailsArea;
    private JLabel noticeLabel;
    // End of variables declaration//GEN-END:variables

    // ── Internal state (not GUI components) ──────────────────────────────────
    private final String moduleTitleText;
    private final String ownerText;
    private final List<String> pendingPartsData;
}
