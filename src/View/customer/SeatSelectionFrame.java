/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.customer;

import View.theme.SwingTheme;

import Controler.AlertUtil;
import Controler.SessionManager;
import Model.dao.ScheduleDao;
import Model.entity.Schedule;
import Model.entity.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Pemilihan Kursi Bioskop Interaktif (Seat Selection & SeatMapView).
 * Diformat sebagai NetBeans JFrame Form (Matisse GUI Builder).
 * Memiliki tab Source dan Design di NetBeans IDE.
 *
 * @author human
 */
public class SeatSelectionFrame extends javax.swing.JFrame {

    public static final int MAX_SEATS = 6;

    private final Schedule schedule;
    private final ScheduleDao scheduleDao = new ScheduleDao();
    private final Set<String> bookedSeats = new HashSet<>();
    private final Set<String> selectedSeats = new HashSet<>();

    private JButton[][] seatButtons;
    private int rows = 5;
    private int cols = 8;

    private final JLabel selectedSeatsValueLabel = new JLabel("-");
    private final JLabel ticketCountValueLabel = new JLabel("0 Tiket");

    /**
     * Creates new form SeatSelectionFrame
     */
    public SeatSelectionFrame(Schedule schedule) {
        this.schedule = schedule;
        initComponents();
        postInitStyles();
        loadSeatDataAndBuildGrid();
    }

    private void postInitStyles() {
        setLocationRelativeTo(null);
        getContentPane().setBackground(BaseCinemaFrame.CINEMA_BG);

        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, BaseCinemaFrame.CINEMA_GOLD),
                new EmptyBorder(0, 16, 0, 16)
        ));

        infoBarPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        summaryCardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Styling summary labels
        summaryCardPanel.add(createSummaryRow("Harga Tiket", String.format("Rp %,.2f", schedule != null ? schedule.getTicketPrice() : BigDecimal.ZERO)), 2);
        summaryCardPanel.add(Box.createVerticalStrut(8), 3);
        summaryCardPanel.add(createSummaryRow("Kursi Dipilih", selectedSeatsValueLabel), 4);
        summaryCardPanel.add(Box.createVerticalStrut(8), 5);
        summaryCardPanel.add(createSummaryRow("Jumlah Kursi", ticketCountValueLabel), 6);
        summaryCardPanel.add(Box.createVerticalStrut(12), 7);

        proceedPaymentButton.setEnabled(false);
        proceedPaymentButton.setBackground(BaseCinemaFrame.CINEMA_GOLD);
        proceedPaymentButton.setForeground(BaseCinemaFrame.CINEMA_BG);
        proceedPaymentButton.setFont(new Font("Inter", Font.BOLD, 13));
        proceedPaymentButton.setFocusPainted(false);
        proceedPaymentButton.addActionListener(e -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                AlertUtil.showWarning(this, "Login Diperlukan", "Silakan login terlebih dahulu untuk melanjutkan pembayaran.");
                return;
            }
            List<String> seatsToBook = new ArrayList<>(selectedSeats);
            PaymentFrame paymentFrame = new PaymentFrame(schedule, seatsToBook, currentUser);
            paymentFrame.setVisible(true);
            dispose();
        });

        backButton.setBackground(BaseCinemaFrame.CINEMA_CARD);
        backButton.setForeground(BaseCinemaFrame.CINEMA_TEXT);
        backButton.setFont(SwingTheme.font(Font.BOLD, 13));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> dispose());

        SwingTheme.applyInterFont(this);
    }

    private void loadSeatDataAndBuildGrid() {
        if (schedule == null) {
            infoFilmTitleLabel.setText("Jadwal tidak valid.");
            return;
        }

        infoFilmTitleLabel.setText(schedule.getFilmTitle());
        String timeStr = schedule.getShowDate() + " " + schedule.getShowTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        infoScheduleLabel.setText("• Studio: " + schedule.getStudioName() + " • " + timeStr);

        try {
            scheduleDao.findStudioDimensionsForSchedule(schedule.getId()).ifPresent(dim -> {
                this.rows = dim.seatRows();
                this.cols = dim.seatCols();
            });
            this.bookedSeats.addAll(scheduleDao.findBookedSeats(schedule.getId()));
        } catch (SQLException ex) {
            AlertUtil.showError(this, "Database Error", "Gagal memuat denah kursi: " + ex.getMessage());
        }

        seatsContentPanel.removeAll();
        seatsContentPanel.add(new CinemaScreenWidget());
        seatsContentPanel.add(Box.createVerticalStrut(20));
        seatsContentPanel.add(buildSeatGrid());
        seatsContentPanel.add(Box.createVerticalStrut(20));
        seatsContentPanel.add(createSeatLegend());

        seatsContentPanel.revalidate();
        seatsContentPanel.repaint();
    }

    private JPanel buildSeatGrid() {
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gridWrapper.setOpaque(false);

        JPanel seatGrid = new JPanel(new GridLayout(rows, cols, 8, 8));
        seatGrid.setOpaque(false);

        this.seatButtons = new JButton[rows][cols];

        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);
            for (int c = 0; c < cols; c++) {
                int colNum = c + 1;
                String seatCode = "" + rowChar + colNum;

                JButton btn = new JButton(seatCode);
                btn.setPreferredSize(new Dimension(52, 42));
                btn.setFont(new Font("Inter", Font.BOLD, 12));
                btn.setFocusPainted(false);
                btn.setOpaque(true);

                if (bookedSeats.contains(seatCode)) {
                    btn.setBackground(BaseCinemaFrame.CINEMA_RED);
                    btn.setForeground(Color.WHITE);
                    btn.setBorder(BorderFactory.createLineBorder(new Color(100, 20, 22), 1));
                    btn.setEnabled(false);
                    btn.setToolTipText("Kursi " + seatCode + " sudah dipesan");
                } else {
                    styleAvailableSeat(btn);
                    btn.setToolTipText("Klik untuk memilih kursi " + seatCode);
                    btn.addActionListener(e -> toggleSeatSelection(btn, seatCode));
                }

                seatButtons[r][c] = btn;
                seatGrid.add(btn);
            }
        }

        gridWrapper.add(seatGrid);
        return gridWrapper;
    }

    private void styleAvailableSeat(JButton btn) {
        btn.setBackground(BaseCinemaFrame.CINEMA_CARD);
        btn.setForeground(BaseCinemaFrame.CINEMA_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
    }

    private void styleSelectedSeat(JButton btn) {
        btn.setBackground(BaseCinemaFrame.CINEMA_GOLD);
        btn.setForeground(BaseCinemaFrame.CINEMA_BG);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    private void toggleSeatSelection(JButton btn, String seatCode) {
        if (selectedSeats.contains(seatCode)) {
            selectedSeats.remove(seatCode);
            styleAvailableSeat(btn);
        } else {
            if (selectedSeats.size() >= MAX_SEATS) {
                AlertUtil.showWarning(this, "Batas Maksimal", "Maksimal pemesanan adalah " + MAX_SEATS + " tiket per transaksi.");
                return;
            }
            selectedSeats.add(seatCode);
            styleSelectedSeat(btn);
        }
        updateSummary();
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsValueLabel.setText("-");
            ticketCountValueLabel.setText("0 Tiket");
            totalPriceLabel.setText("Rp 0,00");
            proceedPaymentButton.setEnabled(false);
            return;
        }

        String sortedSeats = selectedSeats.stream().sorted().collect(Collectors.joining(", "));
        int count = selectedSeats.size();
        BigDecimal total = schedule.getTicketPrice().multiply(BigDecimal.valueOf(count));

        selectedSeatsValueLabel.setText(sortedSeats);
        ticketCountValueLabel.setText(count + " Tiket");
        totalPriceLabel.setText(String.format("Rp %,.2f", total));
        proceedPaymentButton.setEnabled(true);
    }

    private JPanel createSummaryRow(String label, Object valueWidget) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        row.add(lbl, BorderLayout.WEST);

        if (valueWidget instanceof JLabel labelWidget) {
            labelWidget.setFont(new Font("Inter", Font.BOLD, 13));
            labelWidget.setForeground(BaseCinemaFrame.CINEMA_TEXT);
            row.add(labelWidget, BorderLayout.EAST);
        } else {
            JLabel val = new JLabel(String.valueOf(valueWidget));
            val.setFont(new Font("Inter", Font.BOLD, 13));
            val.setForeground(BaseCinemaFrame.CINEMA_TEXT);
            row.add(val, BorderLayout.EAST);
        }
        return row;
    }

    private JPanel createSeatLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        legend.setOpaque(false);

        legend.add(createLegendItem(BaseCinemaFrame.CINEMA_CARD, new Color(203, 213, 225), "Tersedia"));
        legend.add(createLegendItem(BaseCinemaFrame.CINEMA_GOLD, Color.WHITE, "Pilihan Anda"));
        legend.add(createLegendItem(BaseCinemaFrame.CINEMA_RED, new Color(100, 20, 22), "Sudah Dipesan"));

        return legend;
    }

    private JPanel createLegendItem(Color bg, Color border, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        item.setOpaque(false);

        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(20, 20));
        box.setBackground(bg);
        box.setBorder(BorderFactory.createLineBorder(border, 1));

        JLabel text = new JLabel(label);
        text.setFont(new Font("Inter", Font.PLAIN, 12));
        text.setForeground(BaseCinemaFrame.CINEMA_MUTED);

        item.add(box);
        item.add(text);
        return item;
    }

    private static class CinemaScreenWidget extends JPanel {
        public CinemaScreenWidget() {
            setOpaque(false);
            setPreferredSize(new Dimension(500, 50));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int screenWidth = Math.min(width - 60, 550);
            int startX = (width - screenWidth) / 2;

            GradientPaint gp = new GradientPaint(
                    startX, 10, new Color(214, 169, 75, 200),
                    startX + screenWidth, 10, new Color(214, 169, 75, 40));
            g2.setPaint(gp);
            g2.fillRoundRect(startX, 10, screenWidth, 8, 8, 8);

            g2.setColor(new Color(214, 169, 75, 180));
            g2.setFont(new Font("Inter", Font.BOLD, 11));
            String text = "--- LAYAR BIOSKOP / SCREEN ---";
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (width - textWidth) / 2, 34);

            g2.dispose();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        headerTitleLabel = new javax.swing.JLabel();
        mainContentPanel = new javax.swing.JPanel();
        seatMapContainer = new javax.swing.JPanel();
        infoBarPanel = new javax.swing.JPanel();
        infoFilmTitleLabel = new javax.swing.JLabel();
        infoScheduleLabel = new javax.swing.JLabel();
        seatsScrollPane = new javax.swing.JScrollPane();
        seatsContentPanel = new javax.swing.JPanel();
        summaryCardPanel = new javax.swing.JPanel();
        summaryTitleLabel = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 16), new java.awt.Dimension(0, 16), new java.awt.Dimension(0, 16));
        totalHeaderLabel = new javax.swing.JLabel();
        totalPriceLabel = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        proceedPaymentButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        backButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pilih Kursi - Bioskop Nusantara");
        setBackground(new java.awt.Color(245, 247, 250));
        setMinimumSize(new java.awt.Dimension(700, 520));
        setPreferredSize(new java.awt.Dimension(950, 720));

        headerPanel.setBackground(new java.awt.Color(0, 26, 58));
        headerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createEmptyBorder(0, 22, 0, 22)));
        headerPanel.setPreferredSize(new java.awt.Dimension(0, 58));
        headerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 16));

        headerTitleLabel.setFont(new java.awt.Font("Inter", 1, 18)); // NOI18N
        headerTitleLabel.setForeground(new java.awt.Color(245, 166, 35));
        headerTitleLabel.setText("DENAH & PEMILIHAN KURSI STUDIO");
        headerPanel.add(headerTitleLabel);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        mainContentPanel.setBackground(new java.awt.Color(245, 247, 250));
        mainContentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 20, 16, 20));
        mainContentPanel.setLayout(new java.awt.BorderLayout());

        seatMapContainer.setOpaque(false);
        seatMapContainer.setLayout(new java.awt.BorderLayout());

        infoBarPanel.setBackground(new java.awt.Color(0, 26, 58));
        infoBarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 14, 10));

        infoFilmTitleLabel.setFont(new java.awt.Font("Inter", 1, 15)); // NOI18N
        infoFilmTitleLabel.setForeground(new java.awt.Color(245, 166, 35));
        infoFilmTitleLabel.setText("Judul Film");
        infoBarPanel.add(infoFilmTitleLabel);

        infoScheduleLabel.setFont(new java.awt.Font("Inter", 0, 13)); // NOI18N
        infoScheduleLabel.setForeground(new java.awt.Color(203, 213, 225));
        infoScheduleLabel.setText("Studio & Jadwal");
        infoBarPanel.add(infoScheduleLabel);

        seatMapContainer.add(infoBarPanel, java.awt.BorderLayout.NORTH);

        seatsScrollPane.setBorder(null);

        seatsContentPanel.setBackground(new java.awt.Color(245, 247, 250));
        seatsContentPanel.setLayout(new javax.swing.BoxLayout(seatsContentPanel, javax.swing.BoxLayout.Y_AXIS));
        seatsScrollPane.setViewportView(seatsContentPanel);

        seatMapContainer.add(seatsScrollPane, java.awt.BorderLayout.CENTER);

        mainContentPanel.add(seatMapContainer, java.awt.BorderLayout.CENTER);

        summaryCardPanel.setBackground(new java.awt.Color(255, 255, 255));
        summaryCardPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)), javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        summaryCardPanel.setPreferredSize(new java.awt.Dimension(280, 0));
        summaryCardPanel.setLayout(new javax.swing.BoxLayout(summaryCardPanel, javax.swing.BoxLayout.Y_AXIS));

        summaryTitleLabel.setFont(new java.awt.Font("Inter", 1, 17)); // NOI18N
        summaryTitleLabel.setForeground(new java.awt.Color(0, 26, 58));
        summaryTitleLabel.setText("Ringkasan Pesanan");
        summaryCardPanel.add(summaryTitleLabel);
        summaryCardPanel.add(filler1);

        totalHeaderLabel.setFont(new java.awt.Font("Inter", 1, 12)); // NOI18N
        totalHeaderLabel.setForeground(new java.awt.Color(100, 116, 139));
        totalHeaderLabel.setText("TOTAL HARGA");
        summaryCardPanel.add(totalHeaderLabel);

        totalPriceLabel.setFont(new java.awt.Font("Inter", 1, 22)); // NOI18N
        totalPriceLabel.setForeground(new java.awt.Color(0, 26, 58));
        totalPriceLabel.setText("Rp 0,00");
        summaryCardPanel.add(totalPriceLabel);
        summaryCardPanel.add(filler2);

        proceedPaymentButton.setBackground(new java.awt.Color(0, 26, 58));
        proceedPaymentButton.setFont(new java.awt.Font("Inter", 1, 13)); // NOI18N
        proceedPaymentButton.setForeground(new java.awt.Color(255, 255, 255));
        proceedPaymentButton.setText("Lanjut ke Pembayaran");
        proceedPaymentButton.setMaximumSize(new java.awt.Dimension(2147483647, 44));
        summaryCardPanel.add(proceedPaymentButton);
        summaryCardPanel.add(filler3);

        backButton.setBackground(new java.awt.Color(255, 255, 255));
        backButton.setFont(new java.awt.Font("Inter", 1, 13)); // NOI18N
        backButton.setForeground(new java.awt.Color(0, 26, 58));
        backButton.setText("Kembali");
        backButton.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        summaryCardPanel.add(backButton);

        mainContentPanel.add(summaryCardPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(mainContentPanel, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel headerTitleLabel;
    private javax.swing.JPanel infoBarPanel;
    private javax.swing.JLabel infoFilmTitleLabel;
    private javax.swing.JLabel infoScheduleLabel;
    private javax.swing.JPanel mainContentPanel;
    private javax.swing.JButton proceedPaymentButton;
    private javax.swing.JPanel seatMapContainer;
    private javax.swing.JPanel seatsContentPanel;
    private javax.swing.JScrollPane seatsScrollPane;
    private javax.swing.JPanel summaryCardPanel;
    private javax.swing.JLabel summaryTitleLabel;
    private javax.swing.JLabel totalHeaderLabel;
    private javax.swing.JLabel totalPriceLabel;
    // End of variables declaration//GEN-END:variables
}
