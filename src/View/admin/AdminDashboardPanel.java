package View.admin;

import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Panel Dashboard Admin.
 * Menampilkan ringkasan total booking dan revenue yang diambil dari BookingService.getDashboardSummary().
 * Data tidak pernah statis — selalu berasal dari service.
 */
public final class AdminDashboardPanel extends JPanel {
    private final JLabel totalBookingsValue = new JLabel("—");
    private final JLabel totalRevenueValue = new JLabel("—");
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton refreshButton = new JButton("Refresh Data");

    public AdminDashboardPanel() {
        setLayout(new BorderLayout(0, 24));
        setBackground(SwingTheme.BACKGROUND);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildStatsPanel(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("Dashboard Admin");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setForeground(SwingTheme.TEXT);

        JLabel subtitle = new JLabel("Ringkasan data transaksi dari sistem booking.");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(SwingTheme.MUTED_TEXT);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        return header;
    }

    private JPanel buildStatsPanel() {
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        stats.setOpaque(false);

        stats.add(buildStatCard("Total Booking", totalBookingsValue));
        stats.add(buildStatCard("Total Pendapatan (Rp)", totalRevenueValue));

        return stats;
    }

    private JPanel buildStatCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SwingTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(140, 133, 119)),
                new EmptyBorder(20, 24, 20, 24)));
        card.setPreferredSize(new Dimension(220, 110));

        JLabel cardLabel = new JLabel(label);
        cardLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        cardLabel.setForeground(SwingTheme.MUTED_TEXT);

        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        valueLabel.setForeground(SwingTheme.GOLD);

        card.add(cardLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);

        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);

        SwingTheme.styleButton(refreshButton, true);
        refreshButton.setAlignmentX(LEFT_ALIGNMENT);
        refreshButton.setMaximumSize(new Dimension(180, 44));

        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        statusLabel.setForeground(SwingTheme.MUTED_TEXT);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);

        footer.add(refreshButton);
        footer.add(Box.createVerticalStrut(10));
        footer.add(statusLabel);

        return footer;
    }

    // ── Update methods (harus dipanggil dari EDT) ─────────────────────────────

    public void setTotalBookings(long count) {
        totalBookingsValue.setText(String.valueOf(count));
    }

    public void setTotalRevenue(BigDecimal revenue) {
        if (revenue == null) {
            totalRevenueValue.setText("—");
        } else {
            totalRevenueValue.setText(String.format("%,.0f", revenue));
        }
    }

    public void setStatusLoading() {
        statusLabel.setText("Memuat data...");
        statusLabel.setForeground(SwingTheme.MUTED_TEXT);
        totalBookingsValue.setText("—");
        totalRevenueValue.setText("—");
    }

    public void setStatusReady() {
        statusLabel.setText("Data diperbarui.");
        statusLabel.setForeground(new java.awt.Color(80, 200, 120));
    }

    public void setStatusError(String message) {
        statusLabel.setText(message == null ? "Gagal memuat data." : message);
        statusLabel.setForeground(new java.awt.Color(220, 80, 80));
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }
}
