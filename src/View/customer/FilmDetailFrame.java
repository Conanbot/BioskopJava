package View.customer;

import Controler.AlertUtil;
import Model.dao.ScheduleDao;
import Model.entity.Film;
import Model.entity.Schedule;

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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Rincian Informasi Film & Pilihan Jadwal Tayang (Film Detail).
 * Menerapkan:
 * - Inheritance (extends BaseCinemaFrame)
 * - Encapsulation (membaca data Film)
 * - Perulangan & Percabangan (menampilkan list jadwal tayang)
 * - JDBC query (ScheduleDao.findUpcomingByFilmId)
 */
public class FilmDetailFrame extends BaseCinemaFrame {

    private final Film film;
    private final ScheduleDao scheduleDao = new ScheduleDao();

    public FilmDetailFrame(Film film) {
        super("Detail Film - " + (film != null ? film.getTitle() : ""), 760, 680);
        this.film = film;
        buildUI();
    }

    private void buildUI() {
        if (film == null) {
            JLabel err = new JLabel("Data film tidak ditemukan.", SwingConstants.CENTER);
            err.setForeground(CINEMA_TEXT);
            mainContentPanel.add(err, BorderLayout.CENTER);
            return;
        }

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(CINEMA_BG);
        root.setBorder(new EmptyBorder(20, 26, 20, 26));

        // 1. Header Banner & Info Film
        JPanel infoCard = createCardPanel();
        infoCard.setLayout(new BorderLayout(20, 0));

        // Poster Placeholder / Icon Box
        JPanel posterBox = new JPanel(new BorderLayout());
        posterBox.setPreferredSize(new Dimension(130, 180));
        posterBox.setBackground(new Color(22, 24, 28));
        posterBox.setBorder(BorderFactory.createLineBorder(CINEMA_GOLD, 1));

        JLabel iconPoster = new JLabel("🎬", SwingConstants.CENTER);
        iconPoster.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
        posterBox.add(iconPoster, BorderLayout.CENTER);

        JLabel statusBadge = new JLabel(film.getStatus(), SwingConstants.CENTER);
        statusBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        statusBadge.setOpaque(true);
        statusBadge.setBackground(CINEMA_GOLD);
        statusBadge.setForeground(CINEMA_BG);
        posterBox.add(statusBadge, BorderLayout.SOUTH);

        infoCard.add(posterBox, BorderLayout.WEST);

        // Deskripsi Film
        JPanel descPanel = new JPanel();
        descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
        descPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(film.getTitle());
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titleLbl.setForeground(CINEMA_GOLD);
        descPanel.add(titleLbl);
        descPanel.add(Box.createVerticalStrut(8));

        // Tag Info: Genre, Durasi, Rating Usia
        JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tagsPanel.setOpaque(false);

        tagsPanel.add(createBadge("⏱ " + film.getDurationMinutes() + " Menit", new Color(45, 52, 60), CINEMA_TEXT));
        tagsPanel.add(createBadge("🎭 " + film.getGenre(), new Color(45, 52, 60), CINEMA_TEXT));
        tagsPanel.add(createBadge("🔞 " + film.getAgeRating(), CINEMA_RED, Color.WHITE));
        descPanel.add(tagsPanel);
        descPanel.add(Box.createVerticalStrut(12));

        // Sinopsis
        JLabel synTitle = new JLabel("Sinopsis:");
        synTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        synTitle.setForeground(CINEMA_MUTED);
        descPanel.add(synTitle);
        descPanel.add(Box.createVerticalStrut(4));

        JTextArea synopsisArea = new JTextArea(film.getSynopsis().isEmpty() ? "Belum ada sinopsis." : film.getSynopsis());
        synopsisArea.setWrapStyleWord(true);
        synopsisArea.setLineWrap(true);
        synopsisArea.setEditable(false);
        synopsisArea.setOpaque(false);
        synopsisArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        synopsisArea.setForeground(CINEMA_TEXT);
        descPanel.add(synopsisArea);

        infoCard.add(descPanel, BorderLayout.CENTER);
        root.add(infoCard, BorderLayout.NORTH);

        // 2. Daftar Jadwal Tayang (Schedules Section)
        JPanel schedulesSection = new JPanel(new BorderLayout(0, 10));
        schedulesSection.setOpaque(false);

        JLabel schedTitle = new JLabel("Pilih Jadwal Penayangan Tersedia:");
        schedTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        schedTitle.setForeground(CINEMA_TEXT);
        schedulesSection.add(schedTitle, BorderLayout.NORTH);

        JPanel schedListPanel = new JPanel();
        schedListPanel.setLayout(new BoxLayout(schedListPanel, BoxLayout.Y_AXIS));
        schedListPanel.setBackground(CINEMA_BG);

        try {
            List<Schedule> schedules = scheduleDao.findUpcomingByFilmId(film.getId());
            if (schedules.isEmpty()) {
                JPanel emptyCard = createCardPanel();
                emptyCard.add(new JLabel("Belum ada jadwal penayangan aktif untuk film ini saat ini."));
                schedListPanel.add(emptyCard);
            } else {
                for (Schedule s : schedules) {
                    schedListPanel.add(createScheduleCard(s));
                    schedListPanel.add(Box.createVerticalStrut(8));
                }
            }
        } catch (SQLException ex) {
            JLabel err = new JLabel("Gagal memuat jadwal: " + ex.getMessage());
            err.setForeground(CINEMA_RED);
            schedListPanel.add(err);
        }

        JScrollPane scrollPane = new JScrollPane(schedListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CINEMA_BG);
        schedulesSection.add(scrollPane, BorderLayout.CENTER);

        root.add(schedulesSection, BorderLayout.CENTER);

        // Bottom Bar: Kembali
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomBar.setOpaque(false);
        JButton backBtn = createCinemaButton("⬅ Kembali ke Katalog", false);
        backBtn.addActionListener(e -> dispose());
        bottomBar.add(backBtn);
        root.add(bottomBar, BorderLayout.SOUTH);

        mainContentPanel.add(root, BorderLayout.CENTER);
    }

    private JPanel createScheduleCard(Schedule s) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(14, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        String dateStr = s.getShowDate().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        String timeStr = s.getShowTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        JLabel dateLbl = new JLabel(dateStr + " • Jam " + timeStr);
        dateLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        dateLbl.setForeground(CINEMA_GOLD);

        JLabel subLbl = new JLabel("Studio: " + s.getStudioName() + " | Sisa Kursi: " + s.getAvailableSeats() + " / " + s.getTotalSeats());
        subLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subLbl.setForeground(CINEMA_MUTED);

        left.add(dateLbl);
        left.add(Box.createVerticalStrut(4));
        left.add(subLbl);
        card.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);

        JLabel priceLbl = new JLabel(String.format("Rp %,.2f", s.getTicketPrice()));
        priceLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        priceLbl.setForeground(CINEMA_TEXT);

        JButton pickSeatBtn = createCinemaButton("Pilih Kursi ➔", true);
        pickSeatBtn.setPreferredSize(new Dimension(130, 36));
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

    private JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }
}
