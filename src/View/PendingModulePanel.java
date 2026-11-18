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

public final class PendingModulePanel extends JPanel {
    public PendingModulePanel(String moduleTitle, String owner, List<String> pendingParts) {
        Objects.requireNonNull(moduleTitle, "Judul modul wajib diisi.");
        Objects.requireNonNull(owner, "Pemilik modul wajib diisi.");
        Objects.requireNonNull(pendingParts, "Daftar bagian modul wajib diisi.");
        if (moduleTitle.isBlank() || owner.isBlank() || pendingParts.isEmpty()) {
            throw new IllegalArgumentException("Informasi placeholder modul wajib lengkap.");
        }

        setLayout(new BorderLayout(0, 16));
        setBackground(SwingTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(140, 133, 119)),
                new EmptyBorder(24, 26, 24, 26)));

        JLabel title = new JLabel(moduleTitle);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setForeground(SwingTheme.TEXT);
        JLabel status = new JLabel("Menunggu source dari " + owner);
        status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        status.setForeground(SwingTheme.GOLD);

        JTextArea details = new JTextArea(String.join("\n", pendingParts));
        details.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        details.setForeground(SwingTheme.MUTED_TEXT);
        details.setBackground(SwingTheme.SURFACE);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setEditable(false);
        details.setFocusable(false);
        details.setRows(Math.max(4, pendingParts.size()));

        JLabel notice = new JLabel(
                "<html>Belum ada implementasi yang dijalankan pada halaman ini. "
                + "Fungsinya akan tersedia setelah source pemilik modul diintegrasikan.</html>");
        notice.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        notice.setForeground(SwingTheme.MUTED_TEXT);

        JPanel heading = new JPanel(new BorderLayout(0, 8));
        heading.setOpaque(false);
        heading.add(title, BorderLayout.NORTH);
        heading.add(status, BorderLayout.CENTER);
        add(heading, BorderLayout.NORTH);
        add(details, BorderLayout.CENTER);
        add(notice, BorderLayout.SOUTH);

        // TODO: Replace this placeholder with its owner's real Swing view and controller.
    }
}
