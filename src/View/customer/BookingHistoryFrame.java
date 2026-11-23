/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.customer;

import Controler.AlertUtil;
import Model.entity.BookingCancellationResult;
import Model.entity.BookingHistoryItem;
import Model.entity.BookingReceipt;
import Model.entity.User;
import Model.enums.BookingResolution;
import Model.enums.BookingStatus;
import Model.service.BookingService;
import View.theme.SwingTheme;

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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Riwayat Transaksi & Pembatalan Pemesanan (Booking History & Refund).
 * Diformat sebagai NetBeans JFrame Form (Matisse GUI Builder).
 * Memiliki tab Source dan Design di NetBeans IDE.
 *
 * @author human
 */
public class BookingHistoryFrame extends javax.swing.JFrame {

    private final User user;
    private final BookingService bookingService = new BookingService();

    /**
     * Creates new form BookingHistoryFrame
     */
    public BookingHistoryFrame(User user) {
        this.user = user;
        initComponents();
        postInitStyles();
        loadHistoryData();
    }

    private void postInitStyles() {
        setLocationRelativeTo(null);
        getContentPane().setBackground(BaseCinemaFrame.CINEMA_BG);

        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, BaseCinemaFrame.CINEMA_GOLD),
                new EmptyBorder(0, 22, 0, 22)
        ));

        SwingTheme.styleButton(refreshButton, false);
        refreshButton.setPreferredSize(new Dimension(105, 38));
        refreshButton.addActionListener(e -> loadHistoryData());

        SwingTheme.styleButton(closeButton, false);
        closeButton.setPreferredSize(new Dimension(105, 38));
        closeButton.addActionListener(e -> dispose());

        historyScrollPane.getViewport().setBackground(BaseCinemaFrame.CINEMA_BG);
        historyScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        SwingTheme.applyInterFont(this);
    }

    private void loadHistoryData() {
        historyListContainer.removeAll();

        if (user == null) {
            JLabel err = new JLabel("Anda belum login. Silakan login terlebih dahulu.", SwingConstants.CENTER);
            err.setFont(SwingTheme.font(Font.PLAIN, 14));
            err.setForeground(BaseCinemaFrame.CINEMA_MUTED);
            historyListContainer.add(err);
            historyListContainer.revalidate();
            historyListContainer.repaint();
            return;
        }

        try {
            List<BookingHistoryItem> items = bookingService.getBookingHistory(
                    user.getId(), user.getId(), user.getRole());

            if (items.isEmpty()) {
                JPanel emptyCard = BaseCinemaFrame.createCardPanel();
                emptyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
                JLabel emptyLbl = new JLabel("Belum ada transaksi pemesanan tiket yang tercatat.");
                emptyLbl.setFont(SwingTheme.font(Font.PLAIN, 13));
                emptyLbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);
                emptyCard.add(emptyLbl);
                historyListContainer.add(emptyCard);
            } else {
                for (BookingHistoryItem item : items) {
                    historyListContainer.add(createHistoryCard(item));
                    historyListContainer.add(Box.createVerticalStrut(12));
                }
            }
        } catch (Exception ex) {
            JLabel err = new JLabel("Gagal memuat riwayat: " + ex.getMessage());
            err.setFont(SwingTheme.font(Font.PLAIN, 13));
            err.setForeground(BaseCinemaFrame.CINEMA_RED);
            historyListContainer.add(err);
        }

        SwingTheme.applyInterFont(historyListContainer);
        historyListContainer.revalidate();
        historyListContainer.repaint();
    }

    private JPanel createHistoryCard(BookingHistoryItem item) {
        JPanel card = BaseCinemaFrame.createCardPanel();
        card.setLayout(new BorderLayout(16, 0));
        card.setPreferredSize(new Dimension(0, 108));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        codeRow.setOpaque(false);
        codeRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel codeLbl = new JLabel(item.booking().bookingCode());
        codeLbl.setFont(SwingTheme.font(Font.BOLD, 15));
        codeLbl.setForeground(BaseCinemaFrame.CINEMA_TEXT);

        JLabel statusBadge = createStatusBadge(item.booking().status());
        codeRow.add(codeLbl);
        codeRow.add(statusBadge);

        String dateStr = item.showDateTime() != null
                ? item.showDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm"))
                : "-";
        JLabel detailLbl = new JLabel(item.filmTitle() + "  •  Studio: " + item.studioName() + "  •  " + dateStr);
        detailLbl.setFont(SwingTheme.font(Font.PLAIN, 13));
        detailLbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        detailLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel priceLbl = new JLabel(item.booking().ticketCount() + " Tiket  |  Total: " + String.format("Rp %,.2f", item.totalPrice()));
        priceLbl.setFont(SwingTheme.font(Font.BOLD, 13));
        priceLbl.setForeground(BaseCinemaFrame.CINEMA_GOLD);
        priceLbl.setAlignmentX(LEFT_ALIGNMENT);

        left.add(codeRow);
        left.add(Box.createVerticalStrut(6));
        left.add(detailLbl);
        left.add(Box.createVerticalStrut(6));
        left.add(priceLbl);

        card.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 18));
        right.setOpaque(false);

        JButton viewTicketBtn = BaseCinemaFrame.createCinemaButton("Lihat Tiket", false);
        viewTicketBtn.setPreferredSize(new Dimension(115, 38));
        viewTicketBtn.addActionListener(e -> openTicketDetail(item.booking().bookingCode()));
        right.add(viewTicketBtn);

        if (item.booking().status() == BookingStatus.ACTIVE) {
            JButton cancelBtn = BaseCinemaFrame.createCinemaButton("Batalkan / Refund", false);
            cancelBtn.setBackground(new Color(65, 28, 28));
            cancelBtn.setForeground(new Color(255, 155, 155));
            cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(145, 65, 65), 1),
                    new EmptyBorder(8, 14, 8, 14)
            ));
            cancelBtn.setPreferredSize(new Dimension(155, 38));
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
        badge.setFont(SwingTheme.font(Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));
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
        headerActionPanel = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        mainContentPanel = new javax.swing.JPanel();
        historyScrollPane = new javax.swing.JScrollPane();
        historyListContainer = new javax.swing.JPanel();
        bottomBarPanel = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Riwayat Booking Tiket - Bioskop Nusantara");
        setBackground(new java.awt.Color(18, 19, 22));
        setMinimumSize(new java.awt.Dimension(780, 500));
        setPreferredSize(new java.awt.Dimension(920, 620));

        headerPanel.setBackground(new java.awt.Color(27, 30, 35));
        headerPanel.setPreferredSize(new java.awt.Dimension(0, 64));
        headerPanel.setLayout(new java.awt.BorderLayout());

        headerTitleLabel.setFont(new java.awt.Font("Inter", 1, 18)); // NOI18N
        headerTitleLabel.setForeground(new java.awt.Color(214, 169, 75));
        headerTitleLabel.setText("RIWAYAT PEMESANAN TIKET");
        headerPanel.add(headerTitleLabel, java.awt.BorderLayout.WEST);

        headerActionPanel.setOpaque(false);
        headerActionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 12));

        refreshButton.setFont(new java.awt.Font("Inter", 1, 13)); // NOI18N
        refreshButton.setText("Refresh");
        headerActionPanel.add(refreshButton);

        headerPanel.add(headerActionPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        mainContentPanel.setBackground(new java.awt.Color(18, 19, 22));
        mainContentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 22, 16, 22));
        mainContentPanel.setLayout(new java.awt.BorderLayout(0, 14));

        historyScrollPane.setBorder(null);
        historyScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        historyListContainer.setBackground(new java.awt.Color(18, 19, 22));
        historyListContainer.setLayout(new javax.swing.BoxLayout(historyListContainer, javax.swing.BoxLayout.Y_AXIS));
        historyScrollPane.setViewportView(historyListContainer);

        mainContentPanel.add(historyScrollPane, java.awt.BorderLayout.CENTER);

        bottomBarPanel.setOpaque(false);
        bottomBarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 4));

        closeButton.setFont(new java.awt.Font("Inter", 1, 13)); // NOI18N
        closeButton.setText("Tutup");
        bottomBarPanel.add(closeButton);

        mainContentPanel.add(bottomBarPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainContentPanel, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomBarPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel headerActionPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel headerTitleLabel;
    private javax.swing.JPanel historyListContainer;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JPanel mainContentPanel;
    private javax.swing.JButton refreshButton;
    // End of variables declaration//GEN-END:variables
}
