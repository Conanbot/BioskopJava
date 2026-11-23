/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.customer;

import Controler.AlertUtil;
import Model.entity.BookingReceipt;
import Model.entity.Seat;
import Model.service.TicketExportService;
import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * JFrame E-Ticket Resmi Bioskop.
 * Diformat sebagai NetBeans JFrame Form (Matisse GUI Builder).
 * Memiliki tab Source dan Design di NetBeans IDE.
 *
 * @author human
 */
public class ETicketFrame extends javax.swing.JFrame {

    private final BookingReceipt receipt;
    private final TicketExportService exportService = new TicketExportService();

    /**
     * Creates new form ETicketFrame
     */
    public ETicketFrame(BookingReceipt receipt) {
        this.receipt = receipt;
        initComponents();
        postInitStyles();
        populateTicket();
    }

    private void postInitStyles() {
        setLocationRelativeTo(null);
        getContentPane().setBackground(BaseCinemaFrame.CINEMA_BG);

        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, BaseCinemaFrame.CINEMA_GOLD),
                new EmptyBorder(0, 18, 0, 18)
        ));

        ticketContainerPanel.setBorder(new EmptyBorder(24, 28, 24, 28));
        ticketScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ticketScrollPane.getViewport().setBackground(Color.WHITE);

        SwingTheme.styleButton(exportButton, true);
        exportButton.setPreferredSize(new Dimension(220, 40));
        exportButton.addActionListener(e -> {
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

        SwingTheme.styleButton(closeButton, false);
        closeButton.setPreferredSize(new Dimension(110, 40));
        closeButton.addActionListener(e -> dispose());

        SwingTheme.applyInterFont(this);
    }

    private void populateTicket() {
        if (receipt == null) {
            JLabel errorLabel = new JLabel("Data tiket tidak ditemukan.", SwingConstants.CENTER);
            errorLabel.setForeground(BaseCinemaFrame.CINEMA_TEXT);
            ticketContainerPanel.add(errorLabel);
            return;
        }

        JLabel headerCinema = new JLabel("TIX ID  •  BIOSKOP NUSANTARA");
        headerCinema.setFont(SwingTheme.font(Font.BOLD, 18));
        headerCinema.setForeground(SwingTheme.NAVY);
        headerCinema.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subHeader = new JLabel("OFFICIAL CINEMA PASS / E-TICKET");
        subHeader.setFont(new Font("Inter", Font.PLAIN, 12));
        subHeader.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        subHeader.setAlignmentX(CENTER_ALIGNMENT);

        ticketContainerPanel.add(headerCinema);
        ticketContainerPanel.add(Box.createVerticalStrut(4));
        ticketContainerPanel.add(subHeader);
        ticketContainerPanel.add(Box.createVerticalStrut(18));

        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        codeRow.setOpaque(false);
        JLabel codeLabel = new JLabel("KODE: " + receipt.booking().bookingCode());
        codeLabel.setFont(new Font("Inter", Font.BOLD, 20));
        codeLabel.setForeground(BaseCinemaFrame.CINEMA_TEXT);

        JLabel statusBadge = new JLabel(" " + receipt.booking().status().name() + " ");
        statusBadge.setFont(new Font("Inter", Font.BOLD, 12));
        statusBadge.setOpaque(true);
        statusBadge.setBackground(new Color(40, 167, 69));
        statusBadge.setForeground(Color.WHITE);

        codeRow.add(codeLabel);
        codeRow.add(statusBadge);
        ticketContainerPanel.add(codeRow);
        ticketContainerPanel.add(Box.createVerticalStrut(18));

        ticketContainerPanel.add(createPerforationLine());
        ticketContainerPanel.add(Box.createVerticalStrut(18));

        ticketContainerPanel.add(createTicketInfoRow("Film", receipt.filmTitle(), true));
        ticketContainerPanel.add(createTicketInfoRow("Studio", receipt.studioName(), false));

        String jadwalStr = receipt.showDateTime() != null
                ? receipt.showDateTime().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy - HH:mm"))
                : "-";
        ticketContainerPanel.add(createTicketInfoRow("Jadwal Tayang", jadwalStr, false));

        String seatString = receipt.seats().stream()
                .map(Seat::seatCode)
                .sorted()
                .collect(Collectors.joining(", "));
        ticketContainerPanel.add(createTicketInfoRow("Kursi", seatString + " (" + receipt.booking().ticketCount() + " Kursi)", true));

        ticketContainerPanel.add(Box.createVerticalStrut(10));
        ticketContainerPanel.add(createPerforationLine());
        ticketContainerPanel.add(Box.createVerticalStrut(14));

        ticketContainerPanel.add(createTicketInfoRow("Metode Bayar", receipt.payment().method().name(), false));
        ticketContainerPanel.add(createTicketInfoRow("Total Pembayaran", String.format("Rp %,.2f", receipt.booking().totalPrice()), true));
        if (receipt.payment().cashReceived().signum() > 0) {
            ticketContainerPanel.add(createTicketInfoRow("Tunai Diterima", String.format("Rp %,.2f", receipt.payment().cashReceived()), false));
            ticketContainerPanel.add(createTicketInfoRow("Kembalian", String.format("Rp %,.2f", receipt.payment().changeAmount()), false));
        }

        ticketContainerPanel.add(Box.createVerticalStrut(18));

        JLabel barcodeVisual = new JLabel("||||| ||| ||||||| ||| |||| |||||||| ||||||", SwingConstants.CENTER);
        barcodeVisual.setFont(new Font("Inter", Font.BOLD, 22));
        barcodeVisual.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        barcodeVisual.setAlignmentX(CENTER_ALIGNMENT);
        ticketContainerPanel.add(barcodeVisual);

        JLabel barcodeCode = new JLabel(receipt.booking().bookingCode(), SwingConstants.CENTER);
        barcodeCode.setFont(new Font("Inter", Font.PLAIN, 12));
        barcodeCode.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        barcodeCode.setAlignmentX(CENTER_ALIGNMENT);
        ticketContainerPanel.add(barcodeCode);

        SwingTheme.applyInterFont(ticketContainerPanel);
    }

    private JPanel createTicketInfoRow(String label, String value, boolean highlight) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Inter", highlight ? Font.BOLD : Font.PLAIN, 14));
        val.setForeground(highlight ? BaseCinemaFrame.CINEMA_GOLD : BaseCinemaFrame.CINEMA_TEXT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel createPerforationLine() {
        return new JPanel() {
            {
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
        };
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
        ticketScrollPane = new javax.swing.JScrollPane();
        ticketContainerPanel = new javax.swing.JPanel();
        bottomBarPanel = new javax.swing.JPanel();
        exportButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("E-Ticket Bioskop Nusantara");
        setBackground(new java.awt.Color(245, 247, 250));
        setMinimumSize(new java.awt.Dimension(500, 550));
        setPreferredSize(new java.awt.Dimension(580, 720));

        headerPanel.setBackground(new java.awt.Color(0, 26, 58));
        headerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createEmptyBorder(0, 22, 0, 22)));
        headerPanel.setPreferredSize(new java.awt.Dimension(0, 58));
        headerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 16));

        headerTitleLabel.setFont(new java.awt.Font("Inter", 1, 18)); // NOI18N
        headerTitleLabel.setForeground(new java.awt.Color(245, 166, 35));
        headerTitleLabel.setText("E-TICKET RESMI BIOSKOP");
        headerPanel.add(headerTitleLabel);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        mainContentPanel.setBackground(new java.awt.Color(245, 247, 250));
        mainContentPanel.setLayout(new java.awt.BorderLayout());

        ticketScrollPane.setBorder(null);
        ticketScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        ticketContainerPanel.setBackground(new java.awt.Color(255, 255, 255));
        ticketContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 28, 24, 28));
        ticketContainerPanel.setLayout(new javax.swing.BoxLayout(ticketContainerPanel, javax.swing.BoxLayout.Y_AXIS));
        ticketScrollPane.setViewportView(ticketContainerPanel);

        mainContentPanel.add(ticketScrollPane, java.awt.BorderLayout.CENTER);

        bottomBarPanel.setBackground(new java.awt.Color(245, 247, 250));
        bottomBarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 14, 12));

        exportButton.setBackground(new java.awt.Color(0, 26, 58));
        exportButton.setFont(new java.awt.Font("Inter", 1, 13)); // NOI18N
        exportButton.setForeground(new java.awt.Color(255, 255, 255));
        exportButton.setText("Simpan / Cetak Tiket (.txt)");
        bottomBarPanel.add(exportButton);

        closeButton.setBackground(new java.awt.Color(255, 255, 255));
        closeButton.setFont(new java.awt.Font("Inter", 1, 13)); // NOI18N
        closeButton.setForeground(new java.awt.Color(0, 26, 58));
        closeButton.setText("Tutup");
        bottomBarPanel.add(closeButton);

        mainContentPanel.add(bottomBarPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainContentPanel, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_exportButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomBarPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel headerTitleLabel;
    private javax.swing.JPanel mainContentPanel;
    private javax.swing.JPanel ticketContainerPanel;
    private javax.swing.JScrollPane ticketScrollPane;
    // End of variables declaration//GEN-END:variables
}
