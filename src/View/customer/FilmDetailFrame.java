/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.customer;

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
                BorderFactory.createMatteBorder(0, 0, 3, 0, BaseCinemaFrame.CINEMA_GOLD),
                new EmptyBorder(0, 24, 0, 24)
        ));
        mainContentPanel.setBorder(new EmptyBorder(20, 24, 18, 24));

        infoCardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(18, 20, 18, 20)
        ));

        posterPanel.setBackground(new Color(11, 37, 69));
        posterPanel.setPreferredSize(new Dimension(126, 178));
        posterPanel.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        posterIcon.setFont(SwingTheme.font(Font.BOLD, 13));
        posterIcon.setForeground(new Color(148, 163, 184));
        posterIcon.setText("POSTER");

        filmTitleLabel.setForeground(SwingTheme.NAVY);
        filmTitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        filmTitleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));

        tagsPanel.setAlignmentX(LEFT_ALIGNMENT);
        tagsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tagsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tagsPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        tagsPanel.removeAll();
        tagsPanel.add(durationBadge);
        tagsPanel.add(Box.createHorizontalStrut(8));
        tagsPanel.add(genreBadge);
        tagsPanel.add(Box.createHorizontalStrut(8));
        tagsPanel.add(ratingBadge);

        synopsisArea.setOpaque(false);
        synopsisArea.setEditable(false);
        synopsisArea.setFocusable(false);
        synopsisArea.setForeground(new Color(51, 65, 85));
        synopsisArea.setFont(SwingTheme.font(Font.PLAIN, 13));
        synopsisArea.setAlignmentX(LEFT_ALIGNMENT);

        styleBadge(durationBadge, new Color(241, 245, 249), SwingTheme.NAVY);
        styleBadge(genreBadge, new Color(241, 245, 249), SwingTheme.NAVY);
        styleBadge(ratingBadge, BaseCinemaFrame.CINEMA_RED, Color.WHITE);

        SwingTheme.styleButton(backButton, false);
        backButton.setPreferredSize(new Dimension(175, 38));
        backButton.addActionListener(e -> dispose());

        schedulesScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        schedulesScrollPane.getViewport().setBackground(BaseCinemaFrame.CINEMA_BG);
        schedulesScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingTheme.applyFont(this);
    }

    private void styleBadge(JLabel badge, Color bg, Color fg) {
        badge.setFont(new Font("Poppins", Font.BOLD, 11));
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
                emptyCard.setLayout(new java.awt.GridBagLayout());
                emptyCard.setPreferredSize(new Dimension(0, 84));
                emptyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
                emptyCard.setAlignmentX(LEFT_ALIGNMENT);
                JLabel emptyLbl = new JLabel("Belum ada jadwal penayangan aktif untuk film ini saat ini.");
                emptyLbl.setFont(SwingTheme.font(Font.PLAIN, 13));
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
        SwingTheme.applyFont(schedulesListContainer);
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
        dateLbl.setForeground(SwingTheme.NAVY);
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
        setBackground(new java.awt.Color(245, 247, 250));
        setMinimumSize(new java.awt.Dimension(600, 480));
        setPreferredSize(new java.awt.Dimension(760, 680));

        headerPanel.setBackground(new java.awt.Color(0, 26, 58));
        headerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createEmptyBorder(0, 24, 0, 24)));
        headerPanel.setPreferredSize(new java.awt.Dimension(0, 64));
        headerPanel.setLayout(new java.awt.BorderLayout());

        headerTitle.setFont(new java.awt.Font("Poppins", 1, 18)); // NOI18N
        headerTitle.setForeground(new java.awt.Color(245, 166, 35));
        headerTitle.setText("INFORMASI DETAIL FILM");
        headerPanel.add(headerTitle, java.awt.BorderLayout.CENTER);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        mainContentPanel.setBackground(new java.awt.Color(245, 247, 250));
        mainContentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 18, 24));
        mainContentPanel.setLayout(new java.awt.BorderLayout(0, 16));

        infoCardPanel.setBackground(new java.awt.Color(255, 255, 255));
        infoCardPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)), javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        infoCardPanel.setLayout(new java.awt.BorderLayout(20, 0));

        posterPanel.setBackground(new java.awt.Color(11, 37, 69));
        posterPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225)));
        posterPanel.setPreferredSize(new java.awt.Dimension(126, 178));
        posterPanel.setLayout(new java.awt.BorderLayout());

        posterIcon.setFont(new java.awt.Font("Poppins", 1, 13)); // NOI18N
        posterIcon.setForeground(new java.awt.Color(148, 163, 184));
        posterIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        posterIcon.setText("POSTER");
        posterPanel.add(posterIcon, java.awt.BorderLayout.CENTER);

        infoCardPanel.add(posterPanel, java.awt.BorderLayout.WEST);

        descPanel.setOpaque(false);
        descPanel.setLayout(new javax.swing.BoxLayout(descPanel, javax.swing.BoxLayout.Y_AXIS));

        filmTitleLabel.setFont(new java.awt.Font("Poppins", 1, 22)); // NOI18N
        filmTitleLabel.setForeground(new java.awt.Color(0, 26, 58));
        filmTitleLabel.setText("Judul Film");
        filmTitleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        descPanel.add(filmTitleLabel);

        tagsPanel.setAlignmentX(0.0F);
        tagsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));
        tagsPanel.setOpaque(false);
        tagsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        durationBadge.setText("120 Menit");
        tagsPanel.add(durationBadge);

        genreBadge.setText("Action");
        tagsPanel.add(genreBadge);

        ratingBadge.setText("13+");
        tagsPanel.add(ratingBadge);

        descPanel.add(tagsPanel);

        synopsisArea.setEditable(false);
        synopsisArea.setColumns(20);
        synopsisArea.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        synopsisArea.setForeground(new java.awt.Color(51, 65, 85));
        synopsisArea.setLineWrap(true);
        synopsisArea.setRows(3);
        synopsisArea.setWrapStyleWord(true);
        synopsisArea.setAlignmentX(0.0F);
        synopsisArea.setOpaque(false);
        descPanel.add(synopsisArea);

        infoCardPanel.add(descPanel, java.awt.BorderLayout.CENTER);

        mainContentPanel.add(infoCardPanel, java.awt.BorderLayout.NORTH);

        schedulesSectionPanel.setOpaque(false);
        schedulesSectionPanel.setLayout(new java.awt.BorderLayout(0, 10));

        sectionTitleLabel.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        sectionTitleLabel.setForeground(new java.awt.Color(0, 26, 58));
        sectionTitleLabel.setText("Pilih Jadwal Penayangan:");
        schedulesSectionPanel.add(sectionTitleLabel, java.awt.BorderLayout.NORTH);

        schedulesScrollPane.setBorder(null);
        schedulesScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        schedulesListContainer.setBackground(new java.awt.Color(245, 247, 250));
        schedulesListContainer.setLayout(new javax.swing.BoxLayout(schedulesListContainer, javax.swing.BoxLayout.Y_AXIS));
        schedulesScrollPane.setViewportView(schedulesListContainer);

        schedulesSectionPanel.add(schedulesScrollPane, java.awt.BorderLayout.CENTER);

        mainContentPanel.add(schedulesSectionPanel, java.awt.BorderLayout.CENTER);

        bottomBarPanel.setOpaque(false);
        bottomBarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        backButton.setBackground(new java.awt.Color(255, 255, 255));
        backButton.setFont(new java.awt.Font("Poppins", 1, 13)); // NOI18N
        backButton.setForeground(new java.awt.Color(0, 26, 58));
        backButton.setText("Kembali ke Katalog");
        backButton.setPreferredSize(new java.awt.Dimension(175, 38));
        bottomBarPanel.add(backButton);

        mainContentPanel.add(bottomBarPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainContentPanel, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
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
    private javax.swing.JLabel posterIcon;
    private javax.swing.JPanel posterPanel;
    private javax.swing.JLabel ratingBadge;
    private javax.swing.JPanel schedulesListContainer;
    private javax.swing.JScrollPane schedulesScrollPane;
    private javax.swing.JPanel schedulesSectionPanel;
    private javax.swing.JLabel sectionTitleLabel;
    private javax.swing.JTextArea synopsisArea;
    private javax.swing.JPanel tagsPanel;
    // End of variables declaration//GEN-END:variables
}
