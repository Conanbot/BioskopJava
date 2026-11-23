/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.customer;

import Controler.AlertUtil;
import Model.entity.BookingReceipt;
import Model.entity.Schedule;
import Model.entity.User;
import Model.enums.PaymentMethod;
import Model.exception.InsufficientPaymentException;
import Model.exception.SeatAlreadyBookedException;
import Model.exception.ValidationException;
import Model.service.BookingService;
import Model.service.CreateBookingRequest;
import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Antarmuka Pembayaran Bioskop (Payment UI).
 * Diformat sebagai NetBeans JFrame Form (Matisse GUI Builder).
 * Memiliki tab Source dan Design di NetBeans IDE.
 *
 * @author human
 */
public class PaymentFrame extends javax.swing.JFrame {

    private final Schedule schedule;
    private final List<String> seatCodes;
    private final User user;
    private final BigDecimal totalPrice;

    private final BookingService bookingService = new BookingService();
    private PaymentMethod selectedMethod = PaymentMethod.QRIS;

    private final CardLayout methodCardLayout = new CardLayout();
    private final JTextField cashInputField = new JTextField();
    private final JLabel changeAmountLabel = new JLabel("Rp 0,00");

    /**
     * Creates new form PaymentFrame
     */
    public PaymentFrame(Schedule schedule, List<String> seatCodes, User user) {
        this.schedule = schedule;
        this.seatCodes = seatCodes;
        this.user = user;
        this.totalPrice = schedule != null && seatCodes != null
                ? schedule.getTicketPrice().multiply(BigDecimal.valueOf(seatCodes.size()))
                : BigDecimal.ZERO;

        initComponents();
        postInitStyles();
        populateSummary();
    }

    private void postInitStyles() {
        setLocationRelativeTo(null);
        getContentPane().setBackground(BaseCinemaFrame.CINEMA_BG);

        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, BaseCinemaFrame.CINEMA_GOLD),
                new EmptyBorder(0, 18, 0, 18)
        ));
        mainContentPanel.setBorder(new EmptyBorder(18, 22, 16, 22));

        summaryCardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 50, 58), 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        qrisRadio.setForeground(BaseCinemaFrame.CINEMA_TEXT);
        cashRadio.setForeground(BaseCinemaFrame.CINEMA_TEXT);

        methodCardsPanel.setLayout(methodCardLayout);
        methodCardsPanel.add(createQrisPanel(), "QRIS");
        methodCardsPanel.add(createCashPanel(), "CASH");

        qrisRadio.addActionListener(e -> {
            selectedMethod = PaymentMethod.QRIS;
            methodCardLayout.show(methodCardsPanel, "QRIS");
        });

        cashRadio.addActionListener(e -> {
            selectedMethod = PaymentMethod.CASH;
            methodCardLayout.show(methodCardsPanel, "CASH");
        });

        SwingTheme.styleButton(cancelButton, false);
        cancelButton.setPreferredSize(new Dimension(110, 40));
        cancelButton.addActionListener(e -> dispose());

        SwingTheme.styleButton(payButton, true);
        payButton.setPreferredSize(new Dimension(210, 40));
        payButton.addActionListener(e -> executePayment());

        SwingTheme.applyInterFont(this);
    }

    private void populateSummary() {
        if (schedule == null || seatCodes == null) {
            return;
        }

        String seatList = seatCodes.stream().sorted().collect(Collectors.joining(", "));
        summaryCardPanel.add(createSummaryRow("Film", schedule.getFilmTitle()), 1);
        summaryCardPanel.add(createSummaryRow("Studio", schedule.getStudioName()), 2);
        summaryCardPanel.add(createSummaryRow("Jadwal", schedule.getShowDate() + " - " + schedule.getShowTime()), 3);
        summaryCardPanel.add(createSummaryRow("Kursi", seatList + " (" + seatCodes.size() + " Tiket)"), 4);
        summaryCardPanel.add(Box.createVerticalStrut(8), 5);

        totalPayLabel.setText(String.format("TOTAL BAYAR: Rp %,.2f", totalPrice));
    }

    private JPanel createQrisPanel() {
        JPanel panel = BaseCinemaFrame.createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel qrisInfo = new JLabel("Pindai QR Code berikut menggunakan aplikasi e-Wallet atau Mobile Banking Anda:");
        qrisInfo.setFont(new Font("Inter", Font.PLAIN, 12));
        qrisInfo.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        qrisInfo.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(qrisInfo);
        panel.add(Box.createVerticalStrut(12));

        MockQrisWidget qrWidget = new MockQrisWidget();
        qrWidget.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(qrWidget);
        panel.add(Box.createVerticalStrut(10));

        JLabel prompt = new JLabel("Klik 'Konfirmasi Pembayaran' setelah memindai.");
        prompt.setFont(new Font("Inter", Font.ITALIC, 11));
        prompt.setForeground(BaseCinemaFrame.CINEMA_GOLD);
        prompt.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(prompt);

        return panel;
    }

    private JPanel createCashPanel() {
        JPanel panel = BaseCinemaFrame.createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel cashInfo = new JLabel("Masukkan nominal uang tunai yang diterima:");
        cashInfo.setFont(new Font("Inter", Font.PLAIN, 12));
        cashInfo.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        panel.add(cashInfo);
        panel.add(Box.createVerticalStrut(12));

        cashInputField.setBackground(new Color(20, 22, 26));
        cashInputField.setForeground(BaseCinemaFrame.CINEMA_TEXT);
        cashInputField.setCaretColor(BaseCinemaFrame.CINEMA_GOLD);
        cashInputField.setFont(new Font("Inter", Font.BOLD, 16));
        cashInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BaseCinemaFrame.CINEMA_GOLD, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        cashInputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quickPanel.setOpaque(false);

        JButton btnPas = BaseCinemaFrame.createCinemaButton("Uang Pas", false);
        btnPas.addActionListener(e -> {
            cashInputField.setText(String.valueOf(totalPrice.intValue()));
            calculateChange();
        });

        JButton btn100k = BaseCinemaFrame.createCinemaButton("Rp 100.000", false);
        btn100k.addActionListener(e -> {
            cashInputField.setText("100000");
            calculateChange();
        });

        JButton btn200k = BaseCinemaFrame.createCinemaButton("Rp 200.000", false);
        btn200k.addActionListener(e -> {
            cashInputField.setText("200000");
            calculateChange();
        });

        quickPanel.add(btnPas);
        quickPanel.add(btn100k);
        quickPanel.add(btn200k);

        cashInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateChange();
            }
        });

        panel.add(cashInputField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(quickPanel);
        panel.add(Box.createVerticalStrut(16));

        JPanel changeRow = new JPanel(new BorderLayout());
        changeRow.setOpaque(false);
        changeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel changeTitle = new JLabel("Kembalian:");
        changeTitle.setFont(new Font("Inter", Font.BOLD, 14));
        changeTitle.setForeground(BaseCinemaFrame.CINEMA_MUTED);

        changeAmountLabel.setFont(new Font("Inter", Font.BOLD, 16));
        changeAmountLabel.setForeground(BaseCinemaFrame.CINEMA_GOLD);

        changeRow.add(changeTitle, BorderLayout.WEST);
        changeRow.add(changeAmountLabel, BorderLayout.EAST);
        panel.add(changeRow);

        return panel;
    }

    private void calculateChange() {
        try {
            String text = cashInputField.getText().trim().replaceAll("[^0-9]", "");
            if (text.isEmpty()) {
                changeAmountLabel.setText("Rp 0,00");
                changeAmountLabel.setForeground(BaseCinemaFrame.CINEMA_MUTED);
                return;
            }
            BigDecimal cash = new BigDecimal(text);
            BigDecimal change = cash.subtract(totalPrice);
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                changeAmountLabel.setText(String.format("Rp %,.2f", change));
                changeAmountLabel.setForeground(new Color(40, 167, 69));
            } else {
                changeAmountLabel.setText(String.format("Kurang: Rp %,.2f", change.abs()));
                changeAmountLabel.setForeground(BaseCinemaFrame.CINEMA_RED);
            }
        } catch (Exception ex) {
            changeAmountLabel.setText("Nominal tidak valid");
            changeAmountLabel.setForeground(BaseCinemaFrame.CINEMA_RED);
        }
    }

    private void executePayment() {
        BigDecimal cashReceived = BigDecimal.ZERO;
        if (selectedMethod == PaymentMethod.CASH) {
            try {
                String text = cashInputField.getText().trim().replaceAll("[^0-9]", "");
                if (text.isEmpty()) {
                    throw new ValidationException("Masukkan nominal uang tunai yang diterima.");
                }
                cashReceived = new BigDecimal(text);
                if (cashReceived.compareTo(totalPrice) < 0) {
                    throw new InsufficientPaymentException(totalPrice, cashReceived);
                }
            } catch (InsufficientPaymentException | ValidationException ex) {
                AlertUtil.showWarning(this, "Pembayaran Kurang", ex.getMessage());
                return;
            } catch (NumberFormatException ex) {
                AlertUtil.showWarning(this, "Input Tidak Valid", "Nominal uang harus berupa angka.");
                return;
            }
        }

        try {
            CreateBookingRequest request = new CreateBookingRequest(
                    user.getId(),
                    schedule.getId(),
                    seatCodes,
                    selectedMethod,
                    cashReceived);

            BookingReceipt receipt = bookingService.createBooking(request);

            AlertUtil.showInformation(
                    this,
                    "Pemesanan Berhasil!",
                    "Tiket Anda berhasil dipesan dengan kode: " + receipt.booking().bookingCode());

            ETicketFrame ticketFrame = new ETicketFrame(receipt);
            ticketFrame.setVisible(true);
            dispose();

        } catch (SeatAlreadyBookedException ex) {
            AlertUtil.showError(
                    this,
                    "Kursi Sudah Terisi",
                    "Kursi " + ex.getSeatCode() + " baru saja dipesan oleh orang lain. Silakan pilih kursi lain.");
        } catch (InsufficientPaymentException | ValidationException ex) {
            AlertUtil.showWarning(this, "Validasi Gagal", ex.getMessage());
        } catch (SQLException ex) {
            AlertUtil.showError(this, "Kesalahan Sistem", "Terjadi kesalahan database: " + ex.getMessage());
        }
    }

    private JPanel createSummaryRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lbl = new JLabel(label + ": ");
        lbl.setFont(new Font("Inter", Font.PLAIN, 12));
        lbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Inter", Font.BOLD, 12));
        val.setForeground(BaseCinemaFrame.CINEMA_TEXT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private static class MockQrisWidget extends JPanel {
        public MockQrisWidget() {
            setPreferredSize(new Dimension(160, 160));
            setMaximumSize(new Dimension(160, 160));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.BLACK);
            drawQrMarker(g2, 10, 10);
            drawQrMarker(g2, 110, 10);
            drawQrMarker(g2, 10, 110);

            for (int r = 0; r < 14; r++) {
                for (int c = 0; c < 14; c++) {
                    if ((r < 5 && c < 5) || (r < 5 && c > 8) || (r > 8 && c < 5)) {
                        continue;
                    }
                    if ((r * 7 + c * 13) % 3 == 0) {
                        g2.fillRect(15 + c * 9, 15 + r * 9, 7, 7);
                    }
                }
            }
            g2.dispose();
        }

        private void drawQrMarker(Graphics2D g2, int x, int y) {
            g2.fillRect(x, y, 40, 40);
            g2.setColor(Color.WHITE);
            g2.fillRect(x + 6, y + 6, 28, 28);
            g2.setColor(Color.BLACK);
            g2.fillRect(x + 12, y + 12, 16, 16);
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

        paymentButtonGroup = new javax.swing.ButtonGroup();
        headerPanel = new javax.swing.JPanel();
        headerTitleLabel = new javax.swing.JLabel();
        mainContentPanel = new javax.swing.JPanel();
        summaryCardPanel = new javax.swing.JPanel();
        summaryTitleLabel = new javax.swing.JLabel();
        totalPayLabel = new javax.swing.JLabel();
        methodSectionPanel = new javax.swing.JPanel();
        methodTitleLabel = new javax.swing.JLabel();
        radioPanel = new javax.swing.JPanel();
        qrisRadio = new javax.swing.JRadioButton();
        cashRadio = new javax.swing.JRadioButton();
        methodCardsPanel = new javax.swing.JPanel();
        bottomBarPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        payButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pembayaran Tiket Bioskop");
        setBackground(new java.awt.Color(18, 19, 22));
        setMinimumSize(new java.awt.Dimension(580, 520));
        setPreferredSize(new java.awt.Dimension(680, 680));

        headerPanel.setBackground(new java.awt.Color(27, 30, 35));
        headerPanel.setPreferredSize(new java.awt.Dimension(0, 56));
        headerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 14));

        headerTitleLabel.setFont(new java.awt.Font("Inter", 1, 18)); // NOI18N
        headerTitleLabel.setForeground(new java.awt.Color(214, 169, 75));
        headerTitleLabel.setText("KASIR & PEMBAYARAN TIKET");
        headerPanel.add(headerTitleLabel);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        mainContentPanel.setBackground(new java.awt.Color(18, 19, 22));
        mainContentPanel.setLayout(new java.awt.BorderLayout(0, 16));

        summaryCardPanel.setBackground(new java.awt.Color(34, 38, 44));
        summaryCardPanel.setLayout(new javax.swing.BoxLayout(summaryCardPanel, javax.swing.BoxLayout.Y_AXIS));

        summaryTitleLabel.setFont(new java.awt.Font("Inter", 1, 16)); // NOI18N
        summaryTitleLabel.setForeground(new java.awt.Color(214, 169, 75));
        summaryTitleLabel.setText("Rincian Transaksi");
        summaryCardPanel.add(summaryTitleLabel);

        totalPayLabel.setFont(new java.awt.Font("Inter", 1, 18)); // NOI18N
        totalPayLabel.setForeground(new java.awt.Color(214, 169, 75));
        totalPayLabel.setText("TOTAL BAYAR: Rp 0,00");
        summaryCardPanel.add(totalPayLabel);

        mainContentPanel.add(summaryCardPanel, java.awt.BorderLayout.NORTH);

        methodSectionPanel.setOpaque(false);
        methodSectionPanel.setLayout(new javax.swing.BoxLayout(methodSectionPanel, javax.swing.BoxLayout.Y_AXIS));

        methodTitleLabel.setFont(new java.awt.Font("Inter", 1, 14)); // NOI18N
        methodTitleLabel.setForeground(new java.awt.Color(245, 241, 232));
        methodTitleLabel.setText("Pilih Metode Pembayaran:");
        methodSectionPanel.add(methodTitleLabel);

        radioPanel.setOpaque(false);
        radioPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 8));

        paymentButtonGroup.add(qrisRadio);
        qrisRadio.setSelected(true);
        qrisRadio.setText("QRIS (Instan & Non-Tunai)");
        radioPanel.add(qrisRadio);

        paymentButtonGroup.add(cashRadio);
        cashRadio.setText("Tunai (CASH di Kasir)");
        radioPanel.add(cashRadio);

        methodSectionPanel.add(radioPanel);

        methodCardsPanel.setOpaque(false);
        methodCardsPanel.setLayout(new java.awt.CardLayout());
        methodSectionPanel.add(methodCardsPanel);

        mainContentPanel.add(methodSectionPanel, java.awt.BorderLayout.CENTER);

        bottomBarPanel.setOpaque(false);
        bottomBarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 14, 10));

        cancelButton.setText("Batal");
        bottomBarPanel.add(cancelButton);

        payButton.setText("Konfirmasi Pembayaran");
        bottomBarPanel.add(payButton);

        mainContentPanel.add(bottomBarPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainContentPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomBarPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton cashRadio;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel headerTitleLabel;
    private javax.swing.JPanel mainContentPanel;
    private javax.swing.JPanel methodCardsPanel;
    private javax.swing.JPanel methodSectionPanel;
    private javax.swing.JLabel methodTitleLabel;
    private javax.swing.JButton payButton;
    private javax.swing.ButtonGroup paymentButtonGroup;
    private javax.swing.JRadioButton qrisRadio;
    private javax.swing.JPanel radioPanel;
    private javax.swing.JPanel summaryCardPanel;
    private javax.swing.JLabel summaryTitleLabel;
    private javax.swing.JLabel totalPayLabel;
    // End of variables declaration//GEN-END:variables
}
