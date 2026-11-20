package View.customer;

import Controler.AlertUtil;
import Controler.SessionManager;
import Model.entity.BookingCancellationResult;
import Model.entity.BookingHistoryItem;
import Model.entity.BookingReceipt;
import Model.entity.User;
import Model.enums.BookingResolution;
import Model.enums.BookingStatus;
import Model.enums.Role;
import Model.service.BookingService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Riwayat Transaksi & Pembatalan Pemesanan (Booking History & Refund).
 * Menerapkan:
 * - Inheritance (extends BaseCinemaFrame)
 * - Polymorphism & Interface Usage (BookingResolution, BookingStatus)
 * - Java Stream API (filtering status)
 * - Exception Handling (BookingNotFoundException, BookingNotCancellableException, SQLException)
 */
public class BookingHistoryFrame extends BaseCinemaFrame {

    private final User user;
    private final BookingService bookingService = new BookingService();
    private final JPanel historyListPanel = new JPanel();

    public BookingHistoryFrame(User user) {
        super("Riwayat Booking Tiket - " + (user != null ? user.getUsername() : "Pelanggan"), 820, 650);
        this.user = user;
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setBackground(CINEMA_BG);
        wrapper.setBorder(new EmptyBorder(18, 24, 18, 24));

        // Header info pengguna
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel userTitle = new JLabel("Riwayat Tiket: " + (user != null ? user.getUsername() : "Guest"));
        userTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        userTitle.setForeground(CINEMA_GOLD);

        JButton refreshBtn = createCinemaButton("🔄 Refresh", false);
        refreshBtn.addActionListener(e -> loadHistoryData());

        topBar.add(userTitle, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        wrapper.add(topBar, BorderLayout.NORTH);

        // Center List
        historyListPanel.setLayout(new BoxLayout(historyListPanel, BoxLayout.Y_AXIS));
        historyListPanel.setBackground(CINEMA_BG);

        JScrollPane scrollPane = new JScrollPane(historyListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CINEMA_BG);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton closeBtn = createCinemaButton("Tutup", false);
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);
        wrapper.add(bottomBar, BorderLayout.SOUTH);

        mainContentPanel.add(wrapper, BorderLayout.CENTER);

        loadHistoryData();
    }

    private void loadHistoryData() {
        historyListPanel.removeAll();

        if (user == null) {
            JLabel err = new JLabel("Anda belum login. Silakan login terlebih dahulu.", SwingConstants.CENTER);
            err.setForeground(CINEMA_MUTED);
            historyListPanel.add(err);
            historyListPanel.revalidate();
            historyListPanel.repaint();
            return;
        }

        try {
            List<BookingHistoryItem> items = bookingService.getBookingHistory(
                    user.getId(), user.getId(), user.getRole());

            if (items.isEmpty()) {
                JPanel emptyCard = createCardPanel();
                emptyCard.add(new JLabel("Belum ada transaksi pemesanan tiket yang tercatat."));
                historyListPanel.add(emptyCard);
            } else {
                for (BookingHistoryItem item : items) {
                    historyListPanel.add(createHistoryCard(item));
                    historyListPanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (Exception ex) {
            JLabel err = new JLabel("Gagal memuat riwayat: " + ex.getMessage());
            err.setForeground(CINEMA_RED);
            historyListPanel.add(err);
        }

        historyListPanel.revalidate();
        historyListPanel.repaint();
    }

    private JPanel createHistoryCard(BookingHistoryItem item) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(14, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        codeRow.setOpaque(false);

        JLabel codeLbl = new JLabel(item.booking().bookingCode());
        codeLbl.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        codeLbl.setForeground(CINEMA_TEXT);

        JLabel statusBadge = createStatusBadge(item.booking().status());
        codeRow.add(codeLbl);
        codeRow.add(statusBadge);

        String dateStr = item.showDateTime() != null
                ? item.showDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm"))
                : "-";
        JLabel detailLbl = new JLabel("🎬 " + item.filmTitle() + " | Studio: " + item.studioName() + " | " + dateStr);
        detailLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        detailLbl.setForeground(CINEMA_MUTED);

        JLabel priceLbl = new JLabel(item.booking().ticketCount() + " Tiket | Total: " + String.format("Rp %,.2f", item.totalPrice()));
        priceLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        priceLbl.setForeground(CINEMA_GOLD);

        left.add(codeRow);
        left.add(Box.createVerticalStrut(4));
        left.add(detailLbl);
        left.add(Box.createVerticalStrut(4));
        left.add(priceLbl);

        card.add(left, BorderLayout.CENTER);

        // Tombol Aksi di Kanan
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 14));
        right.setOpaque(false);

        JButton viewTicketBtn = createCinemaButton("Lihat Tiket", false);
        viewTicketBtn.setPreferredSize(new Dimension(110, 34));
        viewTicketBtn.addActionListener(e -> openTicketDetail(item.booking().bookingCode()));
        right.add(viewTicketBtn);

        // Hanya booking ACTIVE yang bisa dibatalkan/refund
        if (item.booking().status() == BookingStatus.ACTIVE) {
            JButton cancelBtn = createCinemaButton("Batalkan / Refund", false);
            cancelBtn.setBackground(new Color(60, 25, 25));
            cancelBtn.setForeground(new Color(255, 140, 140));
            cancelBtn.setPreferredSize(new Dimension(140, 34));
            cancelBtn.addActionListener(e -> handleCancelBooking(item.booking().bookingCode()));
            right.add(cancelBtn);
        }

        card.add(right, BorderLayout.EAST);
        return card;
    }

    private void openTicketDetail(String bookingCode) {
        try {
            Optional<BookingReceipt> receiptOpt = bookingService.getBookingDetails(
                    bookingCode, user.getId(), user.getRole());
            if (receiptOpt.isPresent()) {
                ETicketFrame ticketFrame = new ETicketFrame(receiptOpt.get());
                ticketFrame.setVisible(true);
            } else {
                AlertUtil.showWarning(this, "Tidak Ditemukan", "Rincian tiket tidak ditemukan.");
            }
        } catch (Exception ex) {
            AlertUtil.showError(this, "Kesalahan", "Gagal membuka tiket: " + ex.getMessage());
        }
    }

    private void handleCancelBooking(String bookingCode) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin membatalkan booking " + bookingCode + "?\nDana tiket akan diproses refund 100%.",
                "Konfirmasi Pembatalan Tiket",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                BookingCancellationResult result = bookingService.cancelOrRefundBooking(
                        bookingCode, user.getId(), user.getRole(), BookingResolution.REFUND);

                AlertUtil.showInformation(
                        this,
                        "Pembatalan Berhasil",
                        "Booking " + result.bookingCode() + " berhasil dibatalkan.\nStatus: " + result.status()
                        + "\nNominal Refund: " + String.format("Rp %,.2f", result.refundedAmount()));

                loadHistoryData();
            } catch (Exception ex) {
                AlertUtil.showError(this, "Gagal Membatalkan", ex.getMessage());
            }
        }
    }

    private JLabel createStatusBadge(BookingStatus status) {
        JLabel badge = new JLabel(" " + status.name() + " ");
        badge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        badge.setOpaque(true);
        if (status == BookingStatus.ACTIVE) {
            badge.setBackground(new Color(40, 167, 69));
            badge.setForeground(Color.WHITE);
        } else if (status == BookingStatus.REFUNDED) {
            badge.setBackground(new Color(214, 169, 75));
            badge.setForeground(Color.BLACK);
        } else {
            badge.setBackground(new Color(100, 105, 115));
            badge.setForeground(Color.WHITE);
        }
        return badge;
    }
}
