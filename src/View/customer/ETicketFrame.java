package View.customer;

import Controler.AlertUtil;
import Model.entity.BookingReceipt;
import Model.entity.Seat;
import Model.service.TicketExportService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.time.format.DateTimeFormatter;
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
 * JFrame E-Ticket Resmi Bioskop.
 * Menerapkan:
 * - Inheritance (extends BaseCinemaFrame)
 * - Java Stream API (manipulasi koleksi kursi)
 * - Java File I/O via TicketExportService (cetak/simpan file tiket fisik)
 * - Custom Graphics / UI bertema bioskop (garis perforasi tiket)
 */
public class ETicketFrame extends BaseCinemaFrame {

    private final BookingReceipt receipt;
    private final TicketExportService exportService = new TicketExportService();

    public ETicketFrame(BookingReceipt receipt) {
        super("E-Ticket Bioskop - " + (receipt != null ? receipt.booking().bookingCode() : ""), 580, 720);
        this.receipt = receipt;
        buildUI();
    }

    private void buildUI() {
        if (receipt == null) {
            JLabel errorLabel = new JLabel("Data tiket tidak ditemukan.", SwingConstants.CENTER);
            errorLabel.setForeground(CINEMA_TEXT);
            mainContentPanel.add(errorLabel, BorderLayout.CENTER);
            return;
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CINEMA_BG);
        wrapper.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Panel Tiket Fisik Bergaya Bioskop
        TicketCardPanel ticketCard = new TicketCardPanel();
        ticketCard.setLayout(new BoxLayout(ticketCard, BoxLayout.Y_AXIS));
        ticketCard.setBackground(CINEMA_SURFACE);
        ticketCard.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. Header Tiket
        JLabel headerCinema = new JLabel("🎬 BIOSKOP NUSANTARA");
        headerCinema.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        headerCinema.setForeground(CINEMA_GOLD);
        headerCinema.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subHeader = new JLabel("OFFICIAL CINEMA PASS / E-TICKET");
        subHeader.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subHeader.setForeground(CINEMA_MUTED);
        subHeader.setAlignmentX(CENTER_ALIGNMENT);

        ticketCard.add(headerCinema);
        ticketCard.add(Box.createVerticalStrut(4));
        ticketCard.add(subHeader);
        ticketCard.add(Box.createVerticalStrut(18));

        // 2. Kode Booking & Status Badge
        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        codeRow.setOpaque(false);
        JLabel codeLabel = new JLabel("KODE: " + receipt.booking().bookingCode());
        codeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        codeLabel.setForeground(CINEMA_TEXT);

        JLabel statusBadge = new JLabel(" " + receipt.booking().status().name() + " ");
        statusBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        statusBadge.setOpaque(true);
        statusBadge.setBackground(new Color(40, 167, 69));
        statusBadge.setForeground(Color.WHITE);

        codeRow.add(codeLabel);
        codeRow.add(statusBadge);
        ticketCard.add(codeRow);
        ticketCard.add(Box.createVerticalStrut(18));

        // 3. Garis Perforasi (Dotted separator)
        ticketCard.add(new PerforationLinePanel());
        ticketCard.add(Box.createVerticalStrut(18));

        // 4. Informasi Film & Jadwal
        ticketCard.add(createTicketInfoRow("Film", receipt.filmTitle(), true));
        ticketCard.add(createTicketInfoRow("Studio", receipt.studioName(), false));

        String jadwalStr = receipt.showDateTime() != null
                ? receipt.showDateTime().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy - HH:mm"))
                : "-";
        ticketCard.add(createTicketInfoRow("Jadwal Tayang", jadwalStr, false));

        // Menggunakan Stream API untuk formatting kursi
        String seatString = receipt.seats().stream()
                .map(Seat::seatCode)
                .sorted()
                .collect(Collectors.joining(", "));
        ticketCard.add(createTicketInfoRow("Kursi", seatString + " (" + receipt.booking().ticketCount() + " Kursi)", true));

        ticketCard.add(Box.createVerticalStrut(10));
        ticketCard.add(new PerforationLinePanel());
        ticketCard.add(Box.createVerticalStrut(14));

        // 5. Informasi Pembayaran
        ticketCard.add(createTicketInfoRow("Metode Bayar", receipt.payment().method().name(), false));
        ticketCard.add(createTicketInfoRow("Total Pembayaran", String.format("Rp %,.2f", receipt.booking().totalPrice()), true));
        if (receipt.payment().cashReceived().signum() > 0) {
            ticketCard.add(createTicketInfoRow("Tunai Diterima", String.format("Rp %,.2f", receipt.payment().cashReceived()), false));
            ticketCard.add(createTicketInfoRow("Kembalian", String.format("Rp %,.2f", receipt.payment().changeAmount()), false));
        }

        ticketCard.add(Box.createVerticalStrut(18));

        // 6. Barcode Simulasi
        JLabel barcodeVisual = new JLabel("||||| ||| ||||||| ||| |||| |||||||| ||||||", SwingConstants.CENTER);
        barcodeVisual.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        barcodeVisual.setForeground(CINEMA_MUTED);
        barcodeVisual.setAlignmentX(CENTER_ALIGNMENT);
        ticketCard.add(barcodeVisual);

        JLabel barcodeCode = new JLabel(receipt.booking().bookingCode(), SwingConstants.CENTER);
        barcodeCode.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        barcodeCode.setForeground(CINEMA_MUTED);
        barcodeCode.setAlignmentX(CENTER_ALIGNMENT);
        ticketCard.add(barcodeCode);

        JScrollPane scrollPane = new JScrollPane(ticketCard);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CINEMA_BG);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons di Bawah
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        actionPanel.setBackground(CINEMA_BG);

        JButton exportBtn = createCinemaButton("📥 Simpan / Cetak Tiket (.txt)", true);
        JButton closeBtn = createCinemaButton("Tutup", false);

        exportBtn.addActionListener(e -> {
            try {
                File file = exportService.exportToFile(receipt, null);
                AlertUtil.showInformation(
                        this,
                        "Tiket Berhasil Disimpan",
                        "File E-Ticket telah berhasil diekspor ke:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                AlertUtil.showError(this, "Gagal Ekspor", "Gagal menyimpan tiket: " + ex.getMessage());
            }
        });

        closeBtn.addActionListener(e -> dispose());

        actionPanel.add(exportBtn);
        actionPanel.add(closeBtn);
        wrapper.add(actionPanel, BorderLayout.SOUTH);

        mainContentPanel.add(wrapper, BorderLayout.CENTER);
    }

    private JPanel createTicketInfoRow(String label, String value, boolean highlight) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        lbl.setForeground(CINEMA_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font(Font.SANS_SERIF, highlight ? Font.BOLD : Font.PLAIN, 14));
        val.setForeground(highlight ? CINEMA_GOLD : CINEMA_TEXT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    /**
     * Panel khusus kartu tiket dengan border halus.
     */
    private static class TicketCardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(55, 60, 68));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
        }
    }

    /**
     * Panel garis perforasi putus-putus.
     */
    private static class PerforationLinePanel extends JPanel {
        public PerforationLinePanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(75, 80, 90));
            for (int x = 0; x < getWidth(); x += 8) {
                g2.fillRect(x, getHeight() / 2, 4, 2);
            }
            g2.dispose();
        }
    }
}
