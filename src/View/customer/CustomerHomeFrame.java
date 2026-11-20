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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Beranda Layanan Pelanggan Bioskop (Customer Home Portal).
 * Menerapkan seluruh materi PBO:
 * - Inheritance (extends BaseCinemaFrame)
 * - Polymorphism (Displayable interface pada Film)
 * - Java Stream API (pencarian, penyaringan genre, dan status film)
 * - JDBC Data Access (FilmDao)
 * - Array & List Collections
 * - Exception Handling (SQLException)
 * - Animasi & Interaktivitas UI Bioskop (Golden dark mode, hover, hero banner)
 */
public class CustomerHomeFrame extends BaseCinemaFrame {

    private final FilmDao filmDao = new FilmDao();
    private List<Film> allFilms = new ArrayList<>();

    // Komponen Filter & Pencarian
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> genreFilterCombo = new JComboBox<>(new String[]{
            "Semua Genre", "Action", "Sci-Fi", "Comedy", "Horror", "Drama"
    });
    private String selectedStatusTab = "ALL"; // ALL, NOW_SHOWING, COMING_SOON

    private final JPanel moviesGridPanel = new JPanel();
    private final JLabel resultCountLabel = new JLabel("Menampilkan film...");

    public CustomerHomeFrame() {
        super("Portal Pemesanan Tiket Bioskop - Customer Home", 1000, 750);
        // Pastikan demo data tersedia jika tabel kosong
        filmDao.ensureDemoDataAvailable();
        buildUI();
        loadFilmsFromDatabase();
    }

    @Override
    protected void setupHeaderRightContent(JPanel rightPanel) {
        JButton historyBtn = createCinemaButton("🎟 Riwayat Booking", false);
        historyBtn.addActionListener(e -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                AlertUtil.showWarning(this, "Login Diperlukan", "Silakan login di menu utama untuk melihat riwayat tiket Anda.");
                return;
            }
            new BookingHistoryFrame(currentUser).setVisible(true);
        });
        rightPanel.add(historyBtn);
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setBackground(CINEMA_BG);
        wrapper.setBorder(new EmptyBorder(14, 24, 20, 24));

        // 1. Hero Promo Banner Bioskop
        wrapper.add(createHeroBanner(), BorderLayout.NORTH);

        // 2. Center: Filter Bar + Movie Grid
        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);

        centerContainer.add(createFilterBar(), BorderLayout.NORTH);

        // Movie Grid (3 kolom)
        moviesGridPanel.setLayout(new GridLayout(0, 3, 16, 16));
        moviesGridPanel.setBackground(CINEMA_BG);

        JScrollPane scrollPane = new JScrollPane(moviesGridPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CINEMA_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        // Result count status di bawah
        resultCountLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        resultCountLabel.setForeground(CINEMA_MUTED);
        centerContainer.add(resultCountLabel, BorderLayout.SOUTH);

        wrapper.add(centerContainer, BorderLayout.CENTER);
        mainContentPanel.add(wrapper, BorderLayout.CENTER);
    }

    /**
     * Hero Banner interaktif dengan visual promo bioskop.
     */
    private JPanel createHeroBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(24, 27, 32));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 55, 65), 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel promoTag = new JLabel("⭐ PREMIERE ENTERTAINMENT");
        promoTag.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        promoTag.setForeground(CINEMA_GOLD);

        JLabel promoTitle = new JLabel("Nikmati Film Pilihan Terbaik dengan Kualitas Studio Premium");
        promoTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        promoTitle.setForeground(CINEMA_TEXT);

        JLabel promoDesc = new JLabel("Pilih kursi favorit Anda sekarang secara langsung & bayar mudah via QRIS / Tunai.");
        promoDesc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        promoDesc.setForeground(CINEMA_MUTED);

        textPanel.add(promoTag);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(promoTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(promoDesc);

        banner.add(textPanel, BorderLayout.CENTER);
        return banner;
    }

    /**
     * Filter & Search Bar menggunakan Java Stream API.
     */
    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setOpaque(false);

        // Bagian Kiri: Tab Status Tayang (Semua, Now Showing, Coming Soon)
        JPanel tabButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabButtons.setOpaque(false);

        JButton tabAll = createCinemaButton("Semua Film", true);
        JButton tabNow = createCinemaButton("Sedang Tayang", false);
        JButton tabSoon = createCinemaButton("Akan Datang", false);

        tabAll.addActionListener(e -> {
            selectedStatusTab = "ALL";
            applyTabStyle(tabAll, tabNow, tabSoon);
            filterAndDisplayFilms();
        });
        tabNow.addActionListener(e -> {
            selectedStatusTab = "NOW_SHOWING";
            applyTabStyle(tabNow, tabAll, tabSoon);
            filterAndDisplayFilms();
        });
        tabSoon.addActionListener(e -> {
            selectedStatusTab = "COMING_SOON";
            applyTabStyle(tabSoon, tabAll, tabNow);
            filterAndDisplayFilms();
        });

        tabButtons.add(tabAll);
        tabButtons.add(tabNow);
        tabButtons.add(tabSoon);
        bar.add(tabButtons, BorderLayout.WEST);

        // Bagian Kanan: Search Field & Genre Combo
        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchControls.setOpaque(false);

        // Search Field
        searchField.setBackground(new Color(28, 31, 37));
        searchField.setForeground(CINEMA_TEXT);
        searchField.setCaretColor(CINEMA_GOLD);
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 68, 78), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Cari judul film...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterAndDisplayFilms();
            }
        });

        // Genre combo
        genreFilterCombo.setBackground(new Color(28, 31, 37));
        genreFilterCombo.setForeground(CINEMA_TEXT);
        genreFilterCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        genreFilterCombo.addActionListener(e -> filterAndDisplayFilms());

        searchControls.add(new JLabel("🔍"));
        searchControls.add(searchField);
        searchControls.add(genreFilterCombo);
        bar.add(searchControls, BorderLayout.EAST);

        return bar;
    }

    private void applyTabStyle(JButton active, JButton... others) {
        active.setBackground(CINEMA_GOLD);
        active.setForeground(CINEMA_BG);
        for (JButton b : others) {
            b.setBackground(CINEMA_CARD);
            b.setForeground(CINEMA_TEXT);
        }
    }

    private void loadFilmsFromDatabase() {
        try {
            this.allFilms = filmDao.findAll();
            filterAndDisplayFilms();
        } catch (SQLException ex) {
            AlertUtil.showError(this, "Gagal Memuat Film", "Terjadi error database: " + ex.getMessage());
        }
    }

    /**
     * Memfilter daftar film menggunakan Java Stream API:
     * - filter status film
     * - filter keyword pencarian
     * - filter genre terpilih
     */
    private void filterAndDisplayFilms() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedGenre = (String) genreFilterCombo.getSelectedItem();

        // Menggunakan Stream API secara ekstensif (Materi Gambar 2)
        List<Film> filtered = allFilms.stream()
                .filter(film -> {
                    // Filter Status
                    if ("ALL".equals(selectedStatusTab)) {
                        return true;
                    }
                    return selectedStatusTab.equalsIgnoreCase(film.getStatus());
                })
                .filter(film -> {
                    // Filter Keyword
                    if (keyword.isEmpty()) {
                        return true;
                    }
                    return film.getTitle().toLowerCase().contains(keyword)
                            || film.getGenre().toLowerCase().contains(keyword);
                })
                .filter(film -> {
                    // Filter Genre
                    if (selectedGenre == null || "Semua Genre".equalsIgnoreCase(selectedGenre)) {
                        return true;
                    }
                    return film.getGenre().toLowerCase().contains(selectedGenre.toLowerCase());
                })
                .collect(Collectors.toList());

        displayFilmCards(filtered);
    }

    /**
     * Menampilkan Card Film ke dalam GridLayout.
     */
    private void displayFilmCards(List<Film> films) {
        moviesGridPanel.removeAll();

        if (films.isEmpty()) {
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            emptyPanel.setOpaque(false);
            JLabel noData = new JLabel("Tidak ada film yang cocok dengan kriteria pencarian.");
            noData.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            noData.setForeground(CINEMA_MUTED);
            emptyPanel.add(noData);
            moviesGridPanel.add(emptyPanel);
        } else {
            for (Film film : films) {
                moviesGridPanel.add(createMovieCard(film));
            }
        }

        resultCountLabel.setText("Menampilkan " + films.size() + " film.");
        moviesGridPanel.revalidate();
        moviesGridPanel.repaint();
    }

    /**
     * Membuat kartu visual film individual.
     */
    private JPanel createMovieCard(Film film) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        // Poster Box
        JPanel posterArea = new JPanel(new BorderLayout());
        posterArea.setPreferredSize(new Dimension(0, 140));
        posterArea.setBackground(new Color(20, 22, 26));
        posterArea.setBorder(BorderFactory.createLineBorder(new Color(50, 56, 66), 1));

        JLabel icon = new JLabel("🎬", SwingConstants.CENTER);
        icon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
        posterArea.add(icon, BorderLayout.CENTER);

        // Status badge di sudut kanan atas poster
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        badgePanel.setOpaque(false);
        JLabel ageBadge = new JLabel(" " + film.getAgeRating() + " ");
        ageBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        ageBadge.setOpaque(true);
        ageBadge.setBackground(CINEMA_RED);
        ageBadge.setForeground(Color.WHITE);
        badgePanel.add(ageBadge);
        posterArea.add(badgePanel, BorderLayout.NORTH);

        card.add(posterArea, BorderLayout.NORTH);

        // Detail Teks Film
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel titleLbl = new JLabel(film.getTitle());
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titleLbl.setForeground(CINEMA_GOLD);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel genreLbl = new JLabel("🎭 " + film.getGenre());
        genreLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        genreLbl.setForeground(CINEMA_MUTED);
        genreLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel durLbl = new JLabel("⏱ " + film.getDurationMinutes() + " Menit");
        durLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        durLbl.setForeground(CINEMA_MUTED);
        durLbl.setAlignmentX(LEFT_ALIGNMENT);

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(genreLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(durLbl);
        card.add(info, BorderLayout.CENTER);

        // Tombol Aksi Pesan
        JButton detailBtn = createCinemaButton("Lihat Jadwal & Pesan ➔", true);
        detailBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        detailBtn.setPreferredSize(new Dimension(0, 36));
        detailBtn.addActionListener(e -> {
            FilmDetailFrame detailFrame = new FilmDetailFrame(film);
            detailFrame.setVisible(true);
        });

        card.add(detailBtn, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Entry point mandiri (Main method) untuk pengujian langsung CustomerHomeFrame.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingTheme.install();
            CustomerHomeFrame frame = new CustomerHomeFrame();
            frame.setVisible(true);
        });
    }
}
