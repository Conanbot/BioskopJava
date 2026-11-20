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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Antarmuka Pembayaran Bioskop (Payment UI).
 * Menerapkan:
 * - Inheritance (extends BaseCinemaFrame)
 * - Polymorphism (CASH vs QRIS payment processing)
 * - Kondisi & Validasi (pengecekan nominal uang cukup atau tidak)
 * - Exception Handling (InsufficientPaymentException, SeatAlreadyBookedException, SQLException)
 * - CardLayout untuk beralih antar metode bayar
 */
public class PaymentFrame extends BaseCinemaFrame {

    private final Schedule schedule;
    private final List<String> seatCodes;
    private final User user;
    private final BigDecimal totalPrice;

    private final BookingService bookingService = new BookingService();

    // UI state
    private PaymentMethod selectedMethod = PaymentMethod.QRIS;
    private final CardLayout methodCardLayout = new CardLayout();
    private final JPanel methodDetailPanel = new JPanel(methodCardLayout);

    // Cash components
    private final JTextField cashInputField = new JTextField();
    private final JLabel changeAmountLabel = new JLabel("Rp 0,00");

    public PaymentFrame(Schedule schedule, List<String> seatCodes, User user) {
        super("Pembayaran Tiket Bioskop", 680, 680);
        this.schedule = schedule;
        this.seatCodes = seatCodes;
        this.user = user;
        this.totalPrice = schedule.getTicketPrice().multiply(BigDecimal.valueOf(seatCodes.size()));

        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setBackground(CINEMA_BG);
        wrapper.setBorder(new EmptyBorder(20, 26, 20, 26));

        // 1. Ringkasan Pesanan (Order Summary Card)
        JPanel summaryCard = createCardPanel();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Rincian Transaksi");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        title.setForeground(CINEMA_GOLD);
        summaryCard.add(title);
        summaryCard.add(Box.createVerticalStrut(12));

        String seatList = seatCodes.stream().sorted().collect(Collectors.joining(", "));
        summaryCard.add(createSummaryRow("Film", schedule.getFilmTitle()));
        summaryCard.add(createSummaryRow("Studio", schedule.getStudioName()));
        summaryCard.add(createSummaryRow("Jadwal", schedule.getShowDate() + " - " + schedule.getShowTime()));
        summaryCard.add(createSummaryRow("Kursi", seatList + " (" + seatCodes.size() + " Tiket)"));
        summaryCard.add(Box.createVerticalStrut(6));

        JLabel totalLabel = new JLabel(String.format("TOTAL BAYAR: Rp %,.2f", totalPrice));
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        totalLabel.setForeground(CINEMA_GOLD);
        summaryCard.add(totalLabel);

        wrapper.add(summaryCard, BorderLayout.NORTH);

        // 2. Pemilihan Metode Pembayaran
        JPanel paymentMethodSection = new JPanel();
        paymentMethodSection.setLayout(new BoxLayout(paymentMethodSection, BoxLayout.Y_AXIS));
        paymentMethodSection.setOpaque(false);

        JLabel methodTitle = new JLabel("Pilih Metode Pembayaran:");
        methodTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        methodTitle.setForeground(CINEMA_TEXT);
        paymentMethodSection.add(methodTitle);
        paymentMethodSection.add(Box.createVerticalStrut(10));

        // Radio Buttons
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        radioPanel.setOpaque(false);

        JRadioButton qrisRadio = new JRadioButton("QRIS (Instan & Non-Tunai)", true);
        styleRadioButton(qrisRadio);
        JRadioButton cashRadio = new JRadioButton("Tunai (CASH di Kasir)", false);
        styleRadioButton(cashRadio);

        ButtonGroup group = new ButtonGroup();
        group.add(qrisRadio);
        group.add(cashRadio);

        radioPanel.add(qrisRadio);
        radioPanel.add(cashRadio);
        paymentMethodSection.add(radioPanel);
        paymentMethodSection.add(Box.createVerticalStrut(14));

        // 3. Card Panel Detail Metode (QRIS vs CASH)
        methodDetailPanel.setOpaque(false);
        methodDetailPanel.add(createQrisPanel(), "QRIS");
        methodDetailPanel.add(createCashPanel(), "CASH");
        paymentMethodSection.add(methodDetailPanel);

        qrisRadio.addActionListener(e -> {
            selectedMethod = PaymentMethod.QRIS;
            methodCardLayout.show(methodDetailPanel, "QRIS");
        });

        cashRadio.addActionListener(e -> {
            selectedMethod = PaymentMethod.CASH;
            methodCardLayout.show(methodDetailPanel, "CASH");
        });

        wrapper.add(paymentMethodSection, BorderLayout.CENTER);

        // 4. Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        actionPanel.setOpaque(false);

        JButton cancelBtn = createCinemaButton("Batal", false);
        JButton payBtn = createCinemaButton("Konfirmasi Pembayaran ✔", true);

        cancelBtn.addActionListener(e -> dispose());
        payBtn.addActionListener(e -> executePayment());

        actionPanel.add(cancelBtn);
        actionPanel.add(payBtn);
        wrapper.add(actionPanel, BorderLayout.SOUTH);

        mainContentPanel.add(wrapper, BorderLayout.CENTER);
    }

    private JPanel createQrisPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel qrisInfo = new JLabel("Pindai QR Code berikut menggunakan aplikasi e-Wallet atau Mobile Banking Anda:");
        qrisInfo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        qrisInfo.setForeground(CINEMA_MUTED);
        qrisInfo.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(qrisInfo);
        panel.add(Box.createVerticalStrut(12));

        // Visual Mock QR Code Canvas
        MockQrisWidget qrWidget = new MockQrisWidget();
        qrWidget.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(qrWidget);
        panel.add(Box.createVerticalStrut(10));

        JLabel prompt = new JLabel("Klik 'Konfirmasi Pembayaran' setelah memindai.");
        prompt.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        prompt.setForeground(CINEMA_GOLD);
        prompt.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(prompt);

        return panel;
    }

    private JPanel createCashPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel cashInfo = new JLabel("Masukkan nominal uang tunai yang diterima dari pelanggan:");
        cashInfo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cashInfo.setForeground(CINEMA_MUTED);
        panel.add(cashInfo);
        panel.add(Box.createVerticalStrut(12));

        // Input field
        cashInputField.setBackground(new Color(20, 22, 26));
        cashInputField.setForeground(CINEMA_TEXT);
        cashInputField.setCaretColor(CINEMA_GOLD);
        cashInputField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        cashInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CINEMA_GOLD, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        cashInputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Quick buttons (Pas, 100k, 200k, 500k)
        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quickPanel.setOpaque(false);

        JButton btnPas = createCinemaButton("Uang Pas", false);
        btnPas.addActionListener(e -> {
            cashInputField.setText(String.valueOf(totalPrice.intValue()));
            calculateChange();
        });

        JButton btn100k = createCinemaButton("Rp 100.000", false);
        btn100k.addActionListener(e -> {
            cashInputField.setText("100000");
            calculateChange();
        });

        JButton btn200k = createCinemaButton("Rp 200.000", false);
        btn200k.addActionListener(e -> {
            cashInputField.setText("200000");
            calculateChange();
        });

        quickPanel.add(btnPas);
        quickPanel.add(btn100k);
        quickPanel.add(btn200k);

        // Listener untuk kalkulasi kembalian real-time
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

        // Label Kembalian
        JPanel changeRow = new JPanel(new BorderLayout());
        changeRow.setOpaque(false);
        changeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel changeTitle = new JLabel("Kembalian:");
        changeTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        changeTitle.setForeground(CINEMA_MUTED);

        changeAmountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        changeAmountLabel.setForeground(CINEMA_GOLD);

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
                changeAmountLabel.setForeground(CINEMA_MUTED);
                return;
            }
            BigDecimal cash = new BigDecimal(text);
            BigDecimal change = cash.subtract(totalPrice);
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                changeAmountLabel.setText(String.format("Rp %,.2f", change));
                changeAmountLabel.setForeground(new Color(40, 167, 69)); // Hijau
            } else {
                changeAmountLabel.setText(String.format("Kurang: Rp %,.2f", change.abs()));
                changeAmountLabel.setForeground(CINEMA_RED); // Merah
            }
        } catch (Exception ex) {
            changeAmountLabel.setText("Nominal tidak valid");
            changeAmountLabel.setForeground(CINEMA_RED);
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

        // Jalankan proses booking transaksi via BookingService
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

            // Buka E-Ticket
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

    private void styleRadioButton(JRadioButton radio) {
        radio.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        radio.setForeground(CINEMA_TEXT);
        radio.setOpaque(false);
        radio.setFocusPainted(false);
    }

    private JPanel createSummaryRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lbl = new JLabel(label + ": ");
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lbl.setForeground(CINEMA_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        val.setForeground(CINEMA_TEXT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    /**
     * Widget grafis tiruan QRIS dengan pola matriks kotak hitam-putih.
     */
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            g2.setColor(Color.BLACK);
            // Pola sudut QR (Position markers)
            drawQrMarker(g2, 10, 10);
            drawQrMarker(g2, 110, 10);
            drawQrMarker(g2, 10, 110);

            // Kotak-kotak acak representasi QR
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
}
