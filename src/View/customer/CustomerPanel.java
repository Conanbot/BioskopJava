package View.customer;

import Controler.AlertUtil;
import Controler.SessionManager;
import Model.dao.FilmDao;
import Model.entity.Film;
import Model.entity.User;
import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Panel Layanan Customer yang terintegrasi di dalam MainFrame.
 * Menggantikan placeholder "Menunggu source dari Dava".
 * Menyediakan integrasi langsung ke JFrame Portal Customer.
 */
public class CustomerPanel extends JPanel {

    private final FilmDao filmDao = new FilmDao();
    private final JPanel filmsPreviewGrid = new JPanel();

    public CustomerPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(SwingTheme.BACKGROUND);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        filmDao.ensureDemoDataAvailable();
        buildUI();
    }

    private void buildUI() {
        // 1. Header Banner & Launch Button
        JPanel headerCard = BaseCinemaFrame.createCardPanel();
        headerCard.setLayout(new BorderLayout(16, 0));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("Layanan Customer - Bioskop Nusantara");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(BaseCinemaFrame.CINEMA_GOLD);

        JLabel subtitle = new JLabel("Modul Lengkap: Customer Home • Film Detail • Seat Map • Payment • E-Ticket • Riwayat");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitle.setForeground(BaseCinemaFrame.CINEMA_MUTED);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        headerCard.add(textPanel, BorderLayout.CENTER);

        // Tombol Buka JFrame Penuh
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actions.setOpaque(false);

        JButton launchFrameBtn = BaseCinemaFrame.createCinemaButton("Buka Portal Customer", true);
        launchFrameBtn.setPreferredSize(new Dimension(270, 42));
        launchFrameBtn.addActionListener(e -> {
            CustomerHomeFrame homeFrame = new CustomerHomeFrame();
            homeFrame.setVisible(true);
        });

        JButton historyBtn = BaseCinemaFrame.createCinemaButton("Riwayat Tiket", false);
        historyBtn.addActionListener(e -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                AlertUtil.showWarning(this, "Login Diperlukan", "Silakan login di tab Login/Register untuk melihat riwayat tiket Anda.");
                return;
            }
            new BookingHistoryFrame(currentUser).setVisible(true);
        });

        actions.add(launchFrameBtn);
        actions.add(historyBtn);
        headerCard.add(actions, BorderLayout.EAST);

        add(headerCard, BorderLayout.NORTH);

        // 2. Film Now Showing Preview
        JPanel previewSection = new JPanel(new BorderLayout(0, 8));
        previewSection.setOpaque(false);

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        JLabel secTitle = new JLabel("Film Sedang Tayang Hari Ini (Klik untuk Pesan):");
        secTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        secTitle.setForeground(BaseCinemaFrame.CINEMA_TEXT);
        sectionHeader.add(secTitle, BorderLayout.WEST);

        previewSection.add(sectionHeader, BorderLayout.NORTH);

        filmsPreviewGrid.setLayout(new GridLayout(0, 2, 14, 14));
        filmsPreviewGrid.setBackground(SwingTheme.BACKGROUND);

        loadFilmCards();

        JScrollPane scrollPane = new JScrollPane(filmsPreviewGrid);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(SwingTheme.BACKGROUND);
        previewSection.add(scrollPane, BorderLayout.CENTER);

        add(previewSection, BorderLayout.CENTER);
    }

    public void loadFilmCards() {
        filmsPreviewGrid.removeAll();
        try {
            List<Film> films = filmDao.findByStatus("NOW_SHOWING");
            if (films.isEmpty()) {
                films = filmDao.findAll();
            }

            for (Film f : films) {
                filmsPreviewGrid.add(createMiniCard(f));
            }
        } catch (SQLException ex) {
            JLabel err = new JLabel("Gagal memuat film: " + ex.getMessage());
            err.setForeground(BaseCinemaFrame.CINEMA_RED);
            filmsPreviewGrid.add(err);
        }
        filmsPreviewGrid.revalidate();
        filmsPreviewGrid.repaint();
    }

    private JPanel createMiniCard(Film film) {
        JPanel card = BaseCinemaFrame.createCardPanel();
        card.setLayout(new BorderLayout(12, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel(film.getTitle());
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        title.setForeground(BaseCinemaFrame.CINEMA_GOLD);

        JLabel info = new JLabel("🎭 " + film.getGenre() + " • ⏱ " + film.getDurationMinutes() + " mnt • 🔞 " + film.getAgeRating());
        info.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        info.setForeground(BaseCinemaFrame.CINEMA_MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(info);
        card.add(left, BorderLayout.CENTER);

        JButton bookBtn = BaseCinemaFrame.createCinemaButton("Detail & Pesan ➔", true);
        bookBtn.setPreferredSize(new Dimension(140, 36));
        bookBtn.addActionListener(e -> {
            FilmDetailFrame detailFrame = new FilmDetailFrame(film);
            detailFrame.setVisible(true);
        });

        card.add(bookBtn, BorderLayout.EAST);
        return card;
    }
}
