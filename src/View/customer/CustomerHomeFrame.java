/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * JFrame Beranda Layanan Pelanggan Bioskop (Customer Home Portal).
 * Diformat sebagai NetBeans JFrame Form (Matisse GUI Builder).
 * Memiliki tab Source dan Design di NetBeans IDE.
 *
 * @author human
 */
public class CustomerHomeFrame extends javax.swing.JFrame {

    private final FilmDao filmDao = new FilmDao();
    private List<Film> allFilms = new ArrayList<>();
    private String selectedStatusTab = "ALL"; // ALL, NOW_SHOWING, COMING_SOON

    /**
     * Creates new form CustomerHomeFrame
     */
    public CustomerHomeFrame() {
        initComponents();
        postInitCustomStyles();
        filmDao.ensureDemoDataAvailable();
        loadFilmsFromDatabase();
    }

    private void postInitCustomStyles() {
        setLocationRelativeTo(null);
        getContentPane().setBackground(BaseCinemaFrame.CINEMA_BG);

        // Header style
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, BaseCinemaFrame.CINEMA_GOLD),
                new EmptyBorder(0, 18, 0, 18)
        ));
        mainContentPanel.setBorder(new EmptyBorder(16, 20, 14, 20));
        logoIcon.setVisible(false);

        SwingTheme.styleButton(historyButton, false);
        historyButton.setPreferredSize(new Dimension(160, 38));
        historyButton.addActionListener(e -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                AlertUtil.showWarning(this, "Login Diperlukan", "Silakan login di menu utama untuk melihat riwayat tiket Anda.");
                return;
            }
            new BookingHistoryFrame(currentUser).setVisible(true);
        });

        // Hero banner style
        heroBannerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 26, 58), 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Tab buttons styling
        styleTabButton(tabAllButton, true);
        styleTabButton(tabNowButton, false);
        styleTabButton(tabSoonButton, false);

        tabAllButton.addActionListener(e -> {
            selectedStatusTab = "ALL";
            applyTabStyle(tabAllButton, tabNowButton, tabSoonButton);
            filterAndDisplayFilms();
        });
        tabNowButton.addActionListener(e -> {
            selectedStatusTab = "NOW_SHOWING";
            applyTabStyle(tabNowButton, tabAllButton, tabSoonButton);
            filterAndDisplayFilms();
        });
        tabSoonButton.addActionListener(e -> {
            selectedStatusTab = "COMING_SOON";
            applyTabStyle(tabSoonButton, tabAllButton, tabNowButton);
            filterAndDisplayFilms();
        });

        // Search field styling
        searchField.setBackground(new Color(255, 255, 255));
        searchField.setForeground(BaseCinemaFrame.CINEMA_TEXT);
        searchField.setCaretColor(BaseCinemaFrame.CINEMA_GOLD);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterAndDisplayFilms();
            }
        });

        // Genre filter combo styling
        genreFilterCombo.setBackground(new Color(255, 255, 255));
        genreFilterCombo.setForeground(BaseCinemaFrame.CINEMA_TEXT);
        genreFilterCombo.addActionListener(e -> filterAndDisplayFilms());

        // Scroll pane speed
        moviesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        moviesScrollPane.getViewport().setBackground(BaseCinemaFrame.CINEMA_BG);
        SwingTheme.applyInterFont(this);
    }

    private void styleTabButton(JButton btn, boolean active) {
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setBackground(active ? BaseCinemaFrame.CINEMA_GOLD : BaseCinemaFrame.CINEMA_CARD);
        btn.setForeground(active ? BaseCinemaFrame.CINEMA_BG : BaseCinemaFrame.CINEMA_TEXT);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? BaseCinemaFrame.CINEMA_GOLD : BaseCinemaFrame.CINEMA_MUTED, 1),
                new EmptyBorder(8, 14, 8, 14)
        ));
    }

    private void applyTabStyle(JButton active, JButton... others) {
        styleTabButton(active, true);
        for (JButton b : others) {
            styleTabButton(b, false);
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

    private void filterAndDisplayFilms() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedGenre = (String) genreFilterCombo.getSelectedItem();

        List<Film> filtered = allFilms.stream()
                .filter(film -> {
                    if ("ALL".equals(selectedStatusTab)) {
                        return true;
                    }
                    return selectedStatusTab.equalsIgnoreCase(film.getStatus());
                })
                .filter(film -> {
                    if (keyword.isEmpty()) {
                        return true;
                    }
                    return film.getTitle().toLowerCase().contains(keyword)
                            || film.getGenre().toLowerCase().contains(keyword);
                })
                .filter(film -> {
                    if (selectedGenre == null || "Semua Genre".equalsIgnoreCase(selectedGenre)) {
                        return true;
                    }
                    return film.getGenre().toLowerCase().contains(selectedGenre.toLowerCase());
                })
                .collect(Collectors.toList());

        displayFilmCards(filtered);
    }

    private void displayFilmCards(List<Film> films) {
        moviesGridPanel.removeAll();

        if (films.isEmpty()) {
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            emptyPanel.setOpaque(false);
            JLabel noData = new JLabel("Tidak ada film yang cocok dengan kriteria pencarian.");
            noData.setFont(new Font("Inter", Font.PLAIN, 14));
            noData.setForeground(BaseCinemaFrame.CINEMA_MUTED);
            emptyPanel.add(noData);
            moviesGridPanel.add(emptyPanel);
        } else {
            for (Film film : films) {
                moviesGridPanel.add(createMovieCard(film));
            }
        }

        resultCountLabel.setText("Menampilkan " + films.size() + " film.");
        SwingTheme.applyInterFont(moviesGridPanel);
        moviesGridPanel.revalidate();
        moviesGridPanel.repaint();
    }

    private JPanel createMovieCard(Film film) {
        JPanel card = BaseCinemaFrame.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel posterArea = new JPanel(new BorderLayout());
        posterArea.setPreferredSize(new Dimension(0, 140));
        posterArea.setBackground(new Color(241, 245, 249));
        posterArea.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        JLabel icon = new JLabel("POSTER", SwingConstants.CENTER);
        icon.setFont(SwingTheme.font(Font.BOLD, 13));
        icon.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        posterArea.add(icon, BorderLayout.CENTER);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        badgePanel.setOpaque(false);
        JLabel ageBadge = new JLabel(" " + film.getAgeRating() + " ");
        ageBadge.setFont(SwingTheme.font(Font.BOLD, 10));
        ageBadge.setOpaque(true);
        ageBadge.setBackground(BaseCinemaFrame.CINEMA_RED);
        ageBadge.setForeground(Color.WHITE);
        badgePanel.add(ageBadge);
        posterArea.add(badgePanel, BorderLayout.NORTH);

        card.add(posterArea, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel titleLbl = new JLabel(film.getTitle());
        titleLbl.setFont(SwingTheme.font(Font.BOLD, 15));
        titleLbl.setForeground(SwingTheme.NAVY);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel genreLbl = new JLabel("Genre: " + film.getGenre());
        genreLbl.setFont(SwingTheme.font(Font.PLAIN, 12));
        genreLbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        genreLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel durLbl = new JLabel("Durasi: " + film.getDurationMinutes() + " Menit");
        durLbl.setFont(SwingTheme.font(Font.PLAIN, 12));
        durLbl.setForeground(BaseCinemaFrame.CINEMA_MUTED);
        durLbl.setAlignmentX(LEFT_ALIGNMENT);

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(genreLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(durLbl);
        card.add(info, BorderLayout.CENTER);

        JButton detailBtn = BaseCinemaFrame.createCinemaButton("Lihat Jadwal & Pesan", true);
        detailBtn.setFont(new Font("Inter", Font.BOLD, 12));
        detailBtn.setPreferredSize(new Dimension(0, 36));
        detailBtn.addActionListener(e -> {
            FilmDetailFrame detailFrame = new FilmDetailFrame(film);
            detailFrame.setVisible(true);
        });

        card.add(detailBtn, BorderLayout.SOUTH);
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
        brandPanel = new javax.swing.JPanel();
        logoIcon = new javax.swing.JLabel();
        brandTitle = new javax.swing.JLabel();
        brandSubtitle = new javax.swing.JLabel();
        headerRightPanel = new javax.swing.JPanel();
        historyButton = new javax.swing.JButton();
        mainContentPanel = new javax.swing.JPanel();
        heroBannerPanel = new javax.swing.JPanel();
        promoTag = new javax.swing.JLabel();
        promoTitle = new javax.swing.JLabel();
        promoDesc = new javax.swing.JLabel();
        centerContainer = new javax.swing.JPanel();
        filterBarPanel = new javax.swing.JPanel();
        tabButtonsPanel = new javax.swing.JPanel();
        tabAllButton = new javax.swing.JButton();
        tabNowButton = new javax.swing.JButton();
        tabSoonButton = new javax.swing.JButton();
        searchControlsPanel = new javax.swing.JPanel();
        searchIcon = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        genreFilterCombo = new javax.swing.JComboBox<>();
        moviesScrollPane = new javax.swing.JScrollPane();
        moviesGridPanel = new javax.swing.JPanel();
        resultCountLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Portal Pemesanan Tiket Bioskop - Customer Home");
        setBackground(new java.awt.Color(245, 247, 250));
        setMinimumSize(new java.awt.Dimension(750, 550));
        setPreferredSize(new java.awt.Dimension(1000, 750));

        headerPanel.setBackground(new java.awt.Color(0, 26, 58));
        headerPanel.setPreferredSize(new java.awt.Dimension(0, 60));
        headerPanel.setLayout(new java.awt.BorderLayout());

        brandPanel.setOpaque(false);
        brandPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 14));

        logoIcon.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        logoIcon.setText("POSTER");
        brandPanel.add(logoIcon);

        brandTitle.setFont(new java.awt.Font("Inter", 1, 18)); // NOI18N
        brandTitle.setForeground(new java.awt.Color(245, 166, 35));
        brandTitle.setText("BIOSKOP NUSANTARA");
        brandPanel.add(brandTitle);

        brandSubtitle.setFont(new java.awt.Font("Inter", 0, 13)); // NOI18N
        brandSubtitle.setForeground(new java.awt.Color(203, 213, 225));
        brandSubtitle.setText("| Layanan Pemesanan Tiket Customer");
        brandPanel.add(brandSubtitle);

        headerPanel.add(brandPanel, java.awt.BorderLayout.WEST);

        headerRightPanel.setOpaque(false);
        headerRightPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 12));

        historyButton.setFont(new java.awt.Font("Inter", 1, 13)); // NOI18N
        historyButton.setText("Riwayat Booking");
        headerRightPanel.add(historyButton);

        headerPanel.add(headerRightPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        mainContentPanel.setBackground(new java.awt.Color(245, 247, 250));
        mainContentPanel.setLayout(new java.awt.BorderLayout(0, 14));

        heroBannerPanel.setBackground(new java.awt.Color(0, 26, 58));
        heroBannerPanel.setPreferredSize(new java.awt.Dimension(0, 90));
        heroBannerPanel.setLayout(new javax.swing.BoxLayout(heroBannerPanel, javax.swing.BoxLayout.Y_AXIS));

        promoTag.setFont(new java.awt.Font("Inter", 1, 11)); // NOI18N
        promoTag.setForeground(new java.awt.Color(245, 166, 35));
        promoTag.setText("PREMIERE ENTERTAINMENT");
        heroBannerPanel.add(promoTag);

        promoTitle.setFont(new java.awt.Font("Inter", 1, 17)); // NOI18N
        promoTitle.setForeground(new java.awt.Color(255, 255, 255));
        promoTitle.setText("Nikmati Film Pilihan Terbaik dengan Kualitas Studio Premium");
        heroBannerPanel.add(promoTitle);

        promoDesc.setFont(new java.awt.Font("Inter", 0, 12)); // NOI18N
        promoDesc.setForeground(new java.awt.Color(203, 213, 225));
        promoDesc.setText("Pilih kursi favorit Anda sekarang secara langsung & bayar mudah via QRIS / Tunai.");
        heroBannerPanel.add(promoDesc);

        mainContentPanel.add(heroBannerPanel, java.awt.BorderLayout.NORTH);

        centerContainer.setOpaque(false);
        centerContainer.setLayout(new java.awt.BorderLayout(0, 12));

        filterBarPanel.setOpaque(false);
        filterBarPanel.setLayout(new java.awt.BorderLayout());

        tabButtonsPanel.setOpaque(false);
        tabButtonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        tabAllButton.setText("Semua Film");
        tabButtonsPanel.add(tabAllButton);

        tabNowButton.setText("Sedang Tayang");
        tabButtonsPanel.add(tabNowButton);

        tabSoonButton.setText("Akan Datang");
        tabButtonsPanel.add(tabSoonButton);

        filterBarPanel.add(tabButtonsPanel, java.awt.BorderLayout.WEST);

        searchControlsPanel.setOpaque(false);
        searchControlsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));

        searchIcon.setText("Cari:");
        searchControlsPanel.add(searchIcon);

        searchField.setColumns(18);
        searchControlsPanel.add(searchField);

        genreFilterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua Genre", "Action", "Sci-Fi", "Comedy", "Horror", "Drama" }));
        searchControlsPanel.add(genreFilterCombo);

        filterBarPanel.add(searchControlsPanel, java.awt.BorderLayout.EAST);

        centerContainer.add(filterBarPanel, java.awt.BorderLayout.NORTH);

        moviesScrollPane.setBorder(null);

        moviesGridPanel.setBackground(new java.awt.Color(245, 247, 250));
        moviesGridPanel.setLayout(new java.awt.GridLayout(0, 3, 16, 16));
        moviesScrollPane.setViewportView(moviesGridPanel);

        centerContainer.add(moviesScrollPane, java.awt.BorderLayout.CENTER);

        resultCountLabel.setFont(new java.awt.Font("Inter", 0, 12)); // NOI18N
        resultCountLabel.setForeground(new java.awt.Color(100, 116, 139));
        resultCountLabel.setText("Menampilkan film...");
        centerContainer.add(resultCountLabel, java.awt.BorderLayout.SOUTH);

        mainContentPanel.add(centerContainer, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainContentPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            SwingTheme.install();
            new CustomerHomeFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel brandPanel;
    private javax.swing.JLabel brandSubtitle;
    private javax.swing.JLabel brandTitle;
    private javax.swing.JPanel centerContainer;
    private javax.swing.JPanel filterBarPanel;
    private javax.swing.JComboBox<String> genreFilterCombo;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel headerRightPanel;
    private javax.swing.JPanel heroBannerPanel;
    private javax.swing.JButton historyButton;
    private javax.swing.JLabel logoIcon;
    private javax.swing.JPanel mainContentPanel;
    private javax.swing.JPanel moviesGridPanel;
    private javax.swing.JScrollPane moviesScrollPane;
    private javax.swing.JLabel promoDesc;
    private javax.swing.JLabel promoTag;
    private javax.swing.JLabel promoTitle;
    private javax.swing.JLabel resultCountLabel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchIcon;
    private javax.swing.JButton tabAllButton;
    private javax.swing.JPanel tabButtonsPanel;
    private javax.swing.JButton tabNowButton;
    private javax.swing.JButton tabSoonButton;
    private javax.swing.JPanel searchControlsPanel;
    // End of variables declaration//GEN-END:variables
}
