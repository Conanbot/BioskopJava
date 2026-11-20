package View.customer;

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
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Pemilihan Kursi Bioskop Interaktif (Seat Selection & SeatMapView).
 * Menerapkan materi PBO:
 * - Array 2 Dimensi (JButton[][] seatButtons untuk baris x kolom)
 * - Perulangan bersarang (Nested for-loop)
 * - Kondisi & Percabangan (penentuan status kursi: booked, available, selected)
 * - Java Stream API (pengurutan kode kursi terpilih & agregasi)
 * - Exception Handling (SQLException, validasi jumlah kursi 1-6)
 * - Animasi & interaktivitas (perubahan warna real-time, efek layar melengkung)
 */
public class SeatSelectionFrame extends BaseCinemaFrame {

    public static final int MAX_SEATS = 6;

    private final Schedule schedule;
    private final ScheduleDao scheduleDao = new ScheduleDao();
    private final Set<String> bookedSeats = new HashSet<>();
    private final Set<String> selectedSeats = new HashSet<>();

    // 2D Array untuk representasi matriks kursi studio
    private JButton[][] seatButtons;
    private int rows = 5;
    private int cols = 8;

    // Label kalkulasi harga real-time
    private final JLabel selectedSeatsLabel = new JLabel("-");
    private final JLabel ticketCountLabel = new JLabel("0 Tiket");
    private final JLabel totalPriceLabel = new JLabel("Rp 0,00");
    private final JButton proceedPaymentButton = createCinemaButton("Lanjut ke Pembayaran ➔", true);

    public SeatSelectionFrame(Schedule schedule) {
        super("Pilih Kursi - " + (schedule != null ? schedule.getFilmTitle() : "Bioskop"), 950, 720);
        this.schedule = schedule;
        loadDataAndBuildUI();
    }

    private void loadDataAndBuildUI() {
        if (schedule == null) {
            JLabel err = new JLabel("Jadwal tidak valid.", SwingConstants.CENTER);
            err.setForeground(CINEMA_TEXT);
            mainContentPanel.add(err, BorderLayout.CENTER);
            return;
        }

        // 1. Ambil data dimensi studio dan kursi yang sudah dibooking dari database via JDBC
        try {
            scheduleDao.findStudioDimensionsForSchedule(schedule.getId()).ifPresent(dim -> {
                this.rows = dim.seatRows();
                this.cols = dim.seatCols();
            });
            this.bookedSeats.addAll(scheduleDao.findBookedSeats(schedule.getId()));
        } catch (SQLException ex) {
            AlertUtil.showError(this, "Database Error", "Gagal mengambil denah kursi: " + ex.getMessage());
        }

        // 2. Setup layout utama: Center (Denah Kursi) & East (Summary Panel)
        JPanel mainLayout = new JPanel(new BorderLayout(20, 0));
        mainLayout.setBackground(CINEMA_BG);
        mainLayout.setBorder(new EmptyBorder(16, 24, 16, 24));

        mainLayout.add(createSeatMapArea(), BorderLayout.CENTER);
        mainLayout.add(createSummaryPanel(), BorderLayout.EAST);

        mainContentPanel.add(mainLayout, BorderLayout.CENTER);
    }

    /**
     * Membuat area denah kursi (Layar Bioskop + Matriks Kursi 2D + Legenda Warna).
     */
    private JPanel createSeatMapArea() {
        JPanel area = new JPanel(new BorderLayout(0, 16));
        area.setOpaque(false);

        // Header info jadwal singkat
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        infoBar.setBackground(CINEMA_SURFACE);
        infoBar.setBorder(BorderFactory.createLineBorder(new Color(45, 50, 58), 1));

        JLabel titleLbl = new JLabel(schedule.getFilmTitle());
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titleLbl.setForeground(CINEMA_GOLD);

        String timeStr = schedule.getShowDate() + " " + schedule.getShowTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel scheduleLbl = new JLabel("• Studio: " + schedule.getStudioName() + " • " + timeStr);
        scheduleLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        scheduleLbl.setForeground(CINEMA_TEXT);

        infoBar.add(titleLbl);
        infoBar.add(scheduleLbl);
        area.add(infoBar, BorderLayout.NORTH);

        // Center Panel: Layar bioskop melengkung + Matriks kursi
        JPanel centerGridPanel = new JPanel();
        centerGridPanel.setLayout(new BoxLayout(centerGridPanel, BoxLayout.Y_AXIS));
        centerGridPanel.setBackground(CINEMA_BG);

        // Layar Bioskop Khas Bioskop
        centerGridPanel.add(new CinemaScreenWidget());
        centerGridPanel.add(Box.createVerticalStrut(20));

        // Matriks Kursi menggunakan Array 2D
        JPanel seatsContainer = buildSeatGrid();
        centerGridPanel.add(seatsContainer);
        centerGridPanel.add(Box.createVerticalStrut(24));

        // Legenda Warna Kursi
        centerGridPanel.add(createSeatLegend());

        JScrollPane scrollPane = new JScrollPane(centerGridPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CINEMA_BG);
        area.add(scrollPane, BorderLayout.CENTER);

        return area;
    }

    /**
     * Membangun Grid Kursi interaktif dengan Array 2D dan Perulangan Bersarang (Nested Loops).
     */
    private JPanel buildSeatGrid() {
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gridWrapper.setOpaque(false);

        // rows baris, cols kolom
        JPanel seatGrid = new JPanel(new GridLayout(rows, cols, 8, 8));
        seatGrid.setOpaque(false);

        // Inisialisasi 2D Array
        this.seatButtons = new JButton[rows][cols];

        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);
            for (int c = 0; c < cols; c++) {
                int colNum = c + 1;
                String seatCode = "" + rowChar + colNum;

                JButton btn = new JButton(seatCode);
                btn.setPreferredSize(new Dimension(52, 42));
                btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                btn.setFocusPainted(false);
                btn.setOpaque(true);

                // Percabangan status kursi (Kondisi)
                if (bookedSeats.contains(seatCode)) {
                    // Kursi sudah terisi (Booked)
                    btn.setBackground(CINEMA_RED);
                    btn.setForeground(Color.WHITE);
                    btn.setBorder(BorderFactory.createLineBorder(new Color(100, 20, 22), 1));
                    btn.setEnabled(false);
                    btn.setToolTipText("Kursi " + seatCode + " sudah dipesan");
                } else {
                    // Kursi tersedia (Available)
                    styleAvailableSeat(btn);
                    btn.setToolTipText("Klik untuk memilih kursi " + seatCode);

                    // Event listener untuk memilih/batal memilih kursi
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
        btn.setBackground(CINEMA_CARD);
        btn.setForeground(CINEMA_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 68, 78), 1));
    }

    private void styleSelectedSeat(JButton btn) {
        btn.setBackground(CINEMA_GOLD);
        btn.setForeground(CINEMA_BG);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    /**
     * Mengubah status pemilihan kursi dengan validasi maksimal 6 kursi.
     */
    private void toggleSeatSelection(JButton btn, String seatCode) {
        if (selectedSeats.contains(seatCode)) {
            // Batalkan pilihan
            selectedSeats.remove(seatCode);
            styleAvailableSeat(btn);
        } else {
            // Pilih kursi baru
            if (selectedSeats.size() >= MAX_SEATS) {
                AlertUtil.showWarning(this, "Batas Maksimal", "Maksimal pemesanan adalah " + MAX_SEATS + " tiket per transaksi.");
                return;
            }
            selectedSeats.add(seatCode);
            styleSelectedSeat(btn);
        }

        updateSummary();
    }

    /**
     * Memperbarui label ringkasan, Stream API untuk mengurutkan kode kursi, dan hitung harga.
     */
    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("-");
            ticketCountLabel.setText("0 Tiket");
            totalPriceLabel.setText("Rp 0,00");
            proceedPaymentButton.setEnabled(false);
            return;
        }

        // Menggunakan Java Stream API
        String sortedSeats = selectedSeats.stream()
                .sorted()
                .collect(Collectors.joining(", "));

        int count = selectedSeats.size();
        BigDecimal total = schedule.getTicketPrice().multiply(BigDecimal.valueOf(count));

        selectedSeatsLabel.setText(sortedSeats);
        ticketCountLabel.setText(count + " Tiket");
        totalPriceLabel.setText(String.format("Rp %,.2f", total));
        proceedPaymentButton.setEnabled(true);
    }

    /**
     * Panel ringkasan pesanan di sebelah kanan.
     */
    private JPanel createSummaryPanel() {
        JPanel panel = createCardPanel();
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Ringkasan Pesanan");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        title.setForeground(CINEMA_GOLD);
        title.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(18));

        panel.add(createSummaryRow("Harga Tiket", String.format("Rp %,.2f", schedule.getTicketPrice())));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createSummaryRow("Kursi Dipilih", selectedSeatsLabel));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createSummaryRow("Jumlah Kursi", ticketCountLabel));
        panel.add(Box.createVerticalStrut(14));

        JPanel separator = new JPanel();
        separator.setBackground(new Color(60, 68, 78));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(14));

        JLabel totalTitle = new JLabel("TOTAL HARGA");
        totalTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        totalTitle.setForeground(CINEMA_MUTED);
        panel.add(totalTitle);
        panel.add(Box.createVerticalStrut(4));

        totalPriceLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        totalPriceLabel.setForeground(CINEMA_GOLD);
        panel.add(totalPriceLabel);

        panel.add(Box.createVerticalGlue());

        // Tombol lanjut ke pembayaran
        proceedPaymentButton.setEnabled(false);
        proceedPaymentButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        proceedPaymentButton.addActionListener(e -> {
            // Periksa apakah user login
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                AlertUtil.showWarning(this, "Login Diperlukan", "Silakan login terlebih dahulu untuk melanjutkan pembayaran.");
                return;
            }

            // Buka PaymentFrame
            List<String> seatsToBook = new ArrayList<>(selectedSeats);
            PaymentFrame paymentFrame = new PaymentFrame(schedule, seatsToBook, currentUser);
            paymentFrame.setVisible(true);
            dispose();
        });

        JButton cancelBtn = createCinemaButton("Kembali", false);
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cancelBtn.addActionListener(e -> dispose());

        panel.add(proceedPaymentButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(cancelBtn);

        return panel;
    }

    private JPanel createSummaryRow(String label, Object valueWidget) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        lbl.setForeground(CINEMA_MUTED);
        row.add(lbl, BorderLayout.WEST);

        if (valueWidget instanceof JLabel labelWidget) {
            labelWidget.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            labelWidget.setForeground(CINEMA_TEXT);
            row.add(labelWidget, BorderLayout.EAST);
        } else {
            JLabel val = new JLabel(String.valueOf(valueWidget));
            val.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            val.setForeground(CINEMA_TEXT);
            row.add(val, BorderLayout.EAST);
        }

        return row;
    }

    private JPanel createSeatLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        legend.setOpaque(false);

        legend.add(createLegendItem(CINEMA_CARD, new Color(60, 68, 78), "Tersedia"));
        legend.add(createLegendItem(CINEMA_GOLD, Color.WHITE, "Pilihan Anda"));
        legend.add(createLegendItem(CINEMA_RED, new Color(100, 20, 22), "Sudah Dipesan"));

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
        text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        text.setForeground(CINEMA_MUTED);

        item.add(box);
        item.add(text);
        return item;
    }

    /**
     * Komponen kustom: Animasi/Garis Lengkung Layar Bioskop dengan Efek Cahaya Emas.
     */
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

            // Efek gradient cahaya bioskop
            GradientPaint gp = new GradientPaint(
                    startX, 10, new Color(214, 169, 75, 200),
                    startX + screenWidth, 10, new Color(214, 169, 75, 40));
            g2.setPaint(gp);
            g2.fillRoundRect(startX, 10, screenWidth, 8, 8, 8);

            // Teks layar
            g2.setColor(new Color(214, 169, 75, 180));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            String text = "--- LAYAR BIOSKOP / SCREEN ---";
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (width - textWidth) / 2, 34);

            g2.dispose();
        }
    }
}
