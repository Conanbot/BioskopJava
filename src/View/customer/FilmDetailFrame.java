/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.customer;

import Controler.AlertUtil;
import Model.dao.ScheduleDao;
import Model.entity.Film;
import Model.entity.Schedule;
import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Rincian Informasi Film & Pilihan Jadwal Tayang (Film Detail).
 * Diformat sebagai NetBeans JFrame Form (Matisse GUI Builder).
 * Memiliki tab Source dan Design di NetBeans IDE.
 *
 * @author human
 */
public class FilmDetailFrame extends javax.swing.JFrame {

    private final Film film;
    private final ScheduleDao scheduleDao = new ScheduleDao();

    /**
     * Creates new form FilmDetailFrame
     */
    public FilmDetailFrame(Film film) {
        this.film = film;
        initComponents();
        postInitStyles();
        populateFilmData();
    }

    private void postInitStyles() {
        setLocationRelativeTo(null);
        getContentPane().setBackground(BaseCinemaFrame.CINEMA_BG);

        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, BaseCinemaFrame.CINEMA_GOLD),
                new EmptyBorder(0, 18, 0, 18)
        ));
        mainContentPanel.setBorder(new EmptyBorder(18, 22, 16, 22));

        infoCardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 56, 66), 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        posterPanel.setBorder(BorderFactory.createLineBorder(BaseCinemaFrame.CINEMA_GOLD, 1));
        posterIcon.setFont(SwingTheme.font(Font.BOLD, 13));
        posterIcon.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        posterIcon.setText("POSTER");
        synopsisArea.setOpaque(false);
        synopsisArea.setForeground(BaseCinemaFrame.CINEMA_TEXT);
        synopsisArea.setFont(SwingTheme.font(Font.PLAIN, 13));

        styleBadge(durationBadge, new Color(45, 52, 60), BaseCinemaFrame.CINEMA_TEXT);
        styleBadge(genreBadge, new Color(45, 52, 60), BaseCinemaFrame.CINEMA_TEXT);
        styleBadge(ratingBadge, BaseCinemaFrame.CINEMA_RED, Color.WHITE);

        SwingTheme.styleButton(backButton, false);
        backButton.setPreferredSize(new Dimension(170, 38));
        backButton.addActionListener(e -> dispose());

        schedulesScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        schedulesScrollPane.getViewport().setBackground(BaseCinemaFrame.CINEMA_BG);
        schedulesScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        SwingTheme.applyInterFont(this);
    }

    private void styleBadge(JLabel badge, Color bg, Color fg) {
        badge.setFont(new Font("Inter", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
    }

    private void populateFilmData() {
        if (film == null) {
            filmTitleLabel.setText("Data film tidak ditemukan.");
            return;
        }

        filmTitleLabel.setText(film.getTitle());
        durationBadge.setText(film.getDurationMinutes() + " Menit");
        genreBadge.setText(film.getGenre());
        ratingBadge.setText(film.getAgeRating());
        synopsisArea.setText(film.getSynopsis().isEmpty() ? "Belum ada sinopsis." : film.getSynopsis());

        schedulesListContainer.removeAll();
        try {
            List<Schedule> schedules = scheduleDao.findUpcomingByFilmId(film.getId());
            if (schedules.isEmpty()) {
                JPanel emptyCard = BaseCinemaFrame.createCardPanel();
                JLabel emptyLbl = new JLabel("Belum ada jadwal penayangan aktif untuk film ini saat ini.");
                emptyLbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);
                emptyCard.add(emptyLbl);
                schedulesListContainer.add(emptyCard);
            } else {
                for (Schedule s : schedules) {
                    schedulesListContainer.add(createScheduleCard(s));
                    schedulesListContainer.add(Box.createVerticalStrut(10));
                }
            }
        } catch (SQLException ex) {
            JLabel err = new JLabel("Gagal memuat jadwal: " + ex.getMessage());
            err.setForeground(BaseCinemaFrame.CINEMA_RED);
            schedulesListContainer.add(err);
        }
        SwingTheme.applyInterFont(schedulesListContainer);
        schedulesListContainer.revalidate();
        schedulesListContainer.repaint();
    }

    private JPanel createScheduleCard(Schedule s) {
        JPanel card = BaseCinemaFrame.createCardPanel();
        card.setLayout(new BorderLayout(14, 0));
        card.setPreferredSize(new Dimension(0, 84));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        String dateStr = s.getShowDate().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        String timeStr = s.getShowTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        JLabel dateLbl = new JLabel(dateStr + " • Jam " + timeStr);
        dateLbl.setFont(SwingTheme.font(Font.BOLD, 14));
        dateLbl.setForeground(BaseCinemaFrame.CINEMA_GOLD);
        dateLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel("Studio: " + s.getStudioName() + "  |  Sisa Kursi: " + s.getAvailableSeats() + " / " + s.getTotalSeats());
        subLbl.setFont(SwingTheme.font(Font.PLAIN, 12));
        subLbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        subLbl.setAlignmentX(LEFT_ALIGNMENT);

        left.add(dateLbl);
        left.add(Box.createVerticalStrut(6));
        left.add(subLbl);
        card.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);

        JLabel priceLbl = new JLabel(String.format("Rp %,.2f", s.getTicketPrice()));
        priceLbl.setFont(SwingTheme.font(Font.BOLD, 15));
        priceLbl.setForeground(BaseCinemaFrame.CINEMA_TEXT);

        JButton pickSeatBtn = BaseCinemaFrame.createCinemaButton("Pilih Kursi", true);
        pickSeatBtn.setPreferredSize(new Dimension(125, 36));
        pickSeatBtn.addActionListener(e -> {
            SeatSelectionFrame seatFrame = new SeatSelectionFrame(s);
            seatFrame.setVisible(true);
            dispose();
        });

        right.add(priceLbl);
        right.add(pickSeatBtn);
        card.add(right, BorderLayout.EAST);

        return card;
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
        headerTitle = new javax.swing.JLabel();
        mainContentPanel = new javax.swing.JPanel();
        infoCardPanel = new javax.swing.JPanel();
        posterPanel = new javax.swing.JPanel();
        posterIcon = new javax.swing.JLabel();
        descPanel = new javax.swing.JPanel();
        filmTitleLabel = new javax.swing.JLabel();
        tagsPanel = new javax.swing.JPanel();
        durationBadge = new javax.swing.JLabel();
        genreBadge = new javax.swing.JLabel();
        ratingBadge = new javax.swing.JLabel();
        synopsisArea = new javax.swing.JTextArea();
        schedulesSectionPanel = new javax.swing.JPanel();
        sectionTitleLabel = new javax.swing.JLabel();
        schedulesScrollPane = new javax.swing.JScrollPane();
        schedulesListContainer = new javax.swing.JPanel();
        bottomBarPanel = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Detail Film - Bioskop Nusantara");
        setBackground(new java.awt.Color(18, 19, 22));
        setMinimumSize(new java.awt.Dimension(600, 480));
        setPreferredSize(new java.awt.Dimension(760, 680));

        headerPanel.setBackground(new java.awt.Color(27, 30, 35));
        headerPanel.setPreferredSize(new java.awt.Dimension(0, 56));
        headerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 14));

        headerTitle.setFont(new java.awt.Font("Inter", 1, 18)); // NOI18N
        headerTitle.setForeground(new java.awt.Color(214, 169, 75));
        headerTitle.setText("INFORMASI DETAIL FILM");
        headerPanel.add(headerTitle);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        mainContentPanel.setBackground(new java.awt.Color(18, 19, 22));
        mainContentPanel.setLayout(new java.awt.BorderLayout(0, 16));

        infoCardPanel.setBackground(new java.awt.Color(34, 38, 44));
        infoCardPanel.setLayout(new java.awt.BorderLayout(18, 0));

        posterPanel.setBackground(new java.awt.Color(22, 24, 28));
        posterPanel.setPreferredSize(new java.awt.Dimension(130, 160));
        posterPanel.setLayout(new java.awt.BorderLayout());

        posterIcon.setFont(new java.awt.Font("Inter", 0, 44)); // NOI18N
        posterIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        posterIcon.setText("POSTER");
        posterPanel.add(posterIcon, java.awt.BorderLayout.CENTER);

        infoCardPanel.add(posterPanel, java.awt.BorderLayout.WEST);

        descPanel.setOpaque(false);
        descPanel.setLayout(new javax.swing.BoxLayout(descPanel, javax.swing.BoxLayout.Y_AXIS));

        filmTitleLabel.setFont(new java.awt.Font("Inter", 1, 22)); // NOI18N
        filmTitleLabel.setForeground(new java.awt.Color(214, 169, 75));
        filmTitleLabel.setText("Judul Film");
        descPanel.add(filmTitleLabel);

        tagsPanel.setOpaque(false);
        tagsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));

        durationBadge.setText("120 Menit");
        tagsPanel.add(durationBadge);

        genreBadge.setText("Action");
        tagsPanel.add(genreBadge);

        ratingBadge.setText("13+");
        tagsPanel.add(ratingBadge);

        descPanel.add(tagsPanel);

        synopsisArea.setColumns(20);
        synopsisArea.setLineWrap(true);
        synopsisArea.setRows(3);
        synopsisArea.setWrapStyleWord(true);
        descPanel.add(synopsisArea);

        infoCardPanel.add(descPanel, java.awt.BorderLayout.CENTER);

        mainContentPanel.add(infoCardPanel, java.awt.BorderLayout.NORTH);

        schedulesSectionPanel.setOpaque(false);
        schedulesSectionPanel.setLayout(new java.awt.BorderLayout(0, 8));

        sectionTitleLabel.setFont(new java.awt.Font("Inter", 1, 14)); // NOI18N
        sectionTitleLabel.setForeground(new java.awt.Color(245, 241, 232));
        sectionTitleLabel.setText("Pilih Jadwal Penayangan:");
        schedulesSectionPanel.add(sectionTitleLabel, java.awt.BorderLayout.NORTH);

        schedulesScrollPane.setBorder(null);

        schedulesListContainer.setBackground(new java.awt.Color(18, 19, 22));
        schedulesListContainer.setLayout(new javax.swing.BoxLayout(schedulesListContainer, javax.swing.BoxLayout.Y_AXIS));
        schedulesScrollPane.setViewportView(schedulesListContainer);

        schedulesSectionPanel.add(schedulesScrollPane, java.awt.BorderLayout.CENTER);

        mainContentPanel.add(schedulesSectionPanel, java.awt.BorderLayout.CENTER);

        bottomBarPanel.setOpaque(false);
        bottomBarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 6));

        backButton.setText("Kembali ke Katalog");
        bottomBarPanel.add(backButton);

        mainContentPanel.add(bottomBarPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainContentPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JPanel bottomBarPanel;
    private javax.swing.JPanel descPanel;
    private javax.swing.JLabel durationBadge;
    private javax.swing.JLabel filmTitleLabel;
    private javax.swing.JLabel genreBadge;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel headerTitle;
    private javax.swing.JPanel infoCardPanel;
    private javax.swing.JPanel mainContentPanel;
    private javax.swing.JPanel posterPanel;
    private javax.swing.JLabel posterIcon;
    private javax.swing.JLabel ratingBadge;
    private javax.swing.JPanel schedulesListContainer;
    private javax.swing.JScrollPane schedulesScrollPane;
    private javax.swing.JPanel schedulesSectionPanel;
    private javax.swing.JLabel sectionTitleLabel;
    private javax.swing.JTextArea synopsisArea;
    private javax.swing.JPanel tagsPanel;
    // End of variables declaration//GEN-END:variables
}
