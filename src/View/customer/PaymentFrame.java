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
                BorderFactory.createMatteBorder(0, 0, 3, 0, BaseCinemaFrame.CINEMA_GOLD),
                new EmptyBorder(0, 24, 0, 24)
        ));
        mainContentPanel.setBorder(new EmptyBorder(20, 24, 18, 24));

        summaryCardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(18, 20, 18, 20)
        ));
        summaryTitleLabel.setForeground(SwingTheme.NAVY);
        summaryTitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        summaryTitleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        totalPayLabel.setForeground(SwingTheme.NAVY);
        totalPayLabel.setAlignmentX(LEFT_ALIGNMENT);
        methodTitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        radioPanel.setAlignmentX(LEFT_ALIGNMENT);
        methodCardsPanel.setAlignmentX(LEFT_ALIGNMENT);

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
            javax.swing.SwingUtilities.invokeLater(cashInputField::requestFocusInWindow);
        });

        SwingTheme.styleButton(cancelButton, false);
        cancelButton.setPreferredSize(new Dimension(110, 40));
        cancelButton.addActionListener(e -> dispose());

        SwingTheme.styleButton(payButton, true);
        payButton.setPreferredSize(new Dimension(210, 40));
        payButton.addActionListener(e -> executePayment());

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingTheme.applyFont(this);
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
        qrisInfo.setFont(new Font("Poppins", Font.PLAIN, 12));
        qrisInfo.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        qrisInfo.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(qrisInfo);
        panel.add(Box.createVerticalStrut(12));

        MockQrisWidget qrWidget = new MockQrisWidget();
        qrWidget.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(qrWidget);
        panel.add(Box.createVerticalStrut(10));

        JLabel prompt = new JLabel("Klik 'Konfirmasi Pembayaran' setelah memindai.");
        prompt.setFont(new Font("Poppins", Font.ITALIC, 11));
        prompt.setForeground(BaseCinemaFrame.CINEMA_GOLD);
        prompt.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(prompt);

        return panel;
    }

    private JPanel createCashPanel() {
        JPanel panel = BaseCinemaFrame.createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel cashInfo = new JLabel("Masukkan nominal uang tunai yang diterima:");
        cashInfo.setFont(new Font("Poppins", Font.PLAIN, 12));
        cashInfo.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        cashInfo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(cashInfo);
        panel.add(Box.createVerticalStrut(12));

        cashInputField.setBackground(new Color(241, 245, 249));
        cashInputField.setForeground(BaseCinemaFrame.CINEMA_TEXT);
        cashInputField.setCaretColor(SwingTheme.NAVY);
        cashInputField.setFont(new Font("Poppins", Font.BOLD, 16));
        cashInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        cashInputField.setAlignmentX(LEFT_ALIGNMENT);
        cashInputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel quickPanel = new JPanel();
        quickPanel.setLayout(new BoxLayout(quickPanel, BoxLayout.X_AXIS));
        quickPanel.setOpaque(false);
        quickPanel.setAlignmentX(LEFT_ALIGNMENT);

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
        quickPanel.add(Box.createHorizontalStrut(8));
        quickPanel.add(btn100k);
        quickPanel.add(Box.createHorizontalStrut(8));
        quickPanel.add(btn200k);

        cashInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateChange();
            }
        });
        cashInputField.addActionListener(e -> {
            if (payButton.isEnabled()) {
                payButton.doClick();
            }
        });

        panel.add(cashInputField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(quickPanel);
        panel.add(Box.createVerticalStrut(16));

        JPanel changeRow = new JPanel(new BorderLayout());
        changeRow.setOpaque(false);
        changeRow.setAlignmentX(LEFT_ALIGNMENT);
        changeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel changeTitle = new JLabel("Kembalian:");
        changeTitle.setFont(new Font("Poppins", Font.BOLD, 14));
        changeTitle.setForeground(BaseCinemaFrame.CINEMA_MUTED);

        changeAmountLabel.setFont(new Font("Poppins", Font.BOLD, 16));
        changeAmountLabel.setForeground(SwingTheme.NAVY);

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
        BigDecimal cashReceived = null;
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
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(label + ": ");
        lbl.setFont(new Font("Poppins", Font.PLAIN, 12));
        lbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Poppins", Font.BOLD, 12));
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
        buttonGapFiller = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0));
        payButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pembayaran Tiket Bioskop");
        setBackground(new java.awt.Color(245, 247, 250));
        setMinimumSize(new java.awt.Dimension(580, 520));
        setPreferredSize(new java.awt.Dimension(680, 680));

        headerPanel.setBackground(new java.awt.Color(0, 26, 58));
        headerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 3, 0, new java.awt.Color(245, 166, 35)), javax.swing.BorderFactory.createEmptyBorder(0, 24, 0, 24)));
        headerPanel.setPreferredSize(new java.awt.Dimension(0, 64));
        headerPanel.setLayout(new java.awt.BorderLayout());

        headerTitleLabel.setFont(new java.awt.Font("Poppins", 1, 18)); // NOI18N
        headerTitleLabel.setForeground(new java.awt.Color(245, 166, 35));
        headerTitleLabel.setText("KASIR & PEMBAYARAN TIKET");
        headerPanel.add(headerTitleLabel, java.awt.BorderLayout.CENTER);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        mainContentPanel.setBackground(new java.awt.Color(245, 247, 250));
        mainContentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 18, 24));
        mainContentPanel.setLayout(new java.awt.BorderLayout(0, 16));

        summaryCardPanel.setBackground(new java.awt.Color(255, 255, 255));
        summaryCardPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)), javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        summaryCardPanel.setLayout(new javax.swing.BoxLayout(summaryCardPanel, javax.swing.BoxLayout.Y_AXIS));

        summaryTitleLabel.setFont(new java.awt.Font("Poppins", 1, 16)); // NOI18N
        summaryTitleLabel.setForeground(new java.awt.Color(0, 26, 58));
        summaryTitleLabel.setText("Rincian Transaksi");
        summaryTitleLabel.setAlignmentX(0.0F);
        summaryTitleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        summaryCardPanel.add(summaryTitleLabel);

        totalPayLabel.setFont(new java.awt.Font("Poppins", 1, 18)); // NOI18N
        totalPayLabel.setForeground(new java.awt.Color(0, 26, 58));
        totalPayLabel.setText("TOTAL BAYAR: Rp 0,00");
        totalPayLabel.setAlignmentX(0.0F);
        summaryCardPanel.add(totalPayLabel);

        mainContentPanel.add(summaryCardPanel, java.awt.BorderLayout.NORTH);

        methodSectionPanel.setOpaque(false);
        methodSectionPanel.setLayout(new javax.swing.BoxLayout(methodSectionPanel, javax.swing.BoxLayout.Y_AXIS));

        methodTitleLabel.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        methodTitleLabel.setForeground(new java.awt.Color(0, 26, 58));
        methodTitleLabel.setText("Pilih Metode Pembayaran:");
        methodTitleLabel.setAlignmentX(0.0F);
        methodTitleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        methodSectionPanel.add(methodTitleLabel);

        radioPanel.setAlignmentX(0.0F);
        radioPanel.setOpaque(false);
        radioPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 6));

        paymentButtonGroup.add(qrisRadio);
        qrisRadio.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        qrisRadio.setForeground(new java.awt.Color(15, 23, 42));
        qrisRadio.setSelected(true);
        qrisRadio.setText("QRIS");
        qrisRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 20));
        qrisRadio.setOpaque(false);
        qrisRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qrisRadioActionPerformed(evt);
            }
        });
        radioPanel.add(qrisRadio);

        paymentButtonGroup.add(cashRadio);
        cashRadio.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        cashRadio.setForeground(new java.awt.Color(15, 23, 42));
        cashRadio.setText("Tunai");
        cashRadio.setOpaque(false);
        radioPanel.add(cashRadio);

        methodSectionPanel.add(radioPanel);

        methodCardsPanel.setAlignmentX(0.0F);
        methodCardsPanel.setOpaque(false);
        methodCardsPanel.setLayout(new java.awt.CardLayout());
        methodSectionPanel.add(methodCardsPanel);

        mainContentPanel.add(methodSectionPanel, java.awt.BorderLayout.CENTER);

        bottomBarPanel.setOpaque(false);
        bottomBarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

        cancelButton.setBackground(new java.awt.Color(255, 255, 255));
        cancelButton.setFont(new java.awt.Font("Poppins", 1, 13)); // NOI18N
        cancelButton.setForeground(new java.awt.Color(0, 26, 58));
        cancelButton.setText("Batal");
        cancelButton.setPreferredSize(new java.awt.Dimension(110, 40));
        bottomBarPanel.add(cancelButton);
        bottomBarPanel.add(buttonGapFiller);

        payButton.setBackground(new java.awt.Color(0, 26, 58));
        payButton.setFont(new java.awt.Font("Poppins", 1, 13)); // NOI18N
        payButton.setForeground(new java.awt.Color(255, 255, 255));
        payButton.setText("Konfirmasi Pembayaran");
        payButton.setPreferredSize(new java.awt.Dimension(210, 40));
        bottomBarPanel.add(payButton);

        mainContentPanel.add(bottomBarPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainContentPanel, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void qrisRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qrisRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_qrisRadioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomBarPanel;
    private javax.swing.Box.Filler buttonGapFiller;
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
