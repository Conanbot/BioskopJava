package View.customer;

import View.theme.ColorPalette;
import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Superclass untuk semua JFrame bertema bioskop.
 * Menerapkan konsep OOP:
 * - Inheritance (Superclass JFrame)
 * - Encapsulation (protected styling & helper methods)
 * - Polymorphism (dapat di-override oleh Subclass)
 * - Tema visual Bioskop: Dark charcoal, Velvet red, & Champagne Gold.
 */
public abstract class BaseCinemaFrame extends JFrame {

    // Palet warna khusus tema bioskop
    public static final Color CINEMA_BG = new Color(18, 19, 22);
    public static final Color CINEMA_SURFACE = new Color(27, 30, 35);
    public static final Color CINEMA_CARD = new Color(34, 38, 44);
    public static final Color CINEMA_GOLD = new Color(214, 169, 75);
    public static final Color CINEMA_GOLD_HOVER = new Color(235, 195, 110);
    public static final Color CINEMA_RED = new Color(158, 42, 43);
    public static final Color CINEMA_RED_HOVER = new Color(185, 55, 56);
    public static final Color CINEMA_TEXT = new Color(245, 241, 232);
    public static final Color CINEMA_MUTED = new Color(160, 165, 172);

    protected final JPanel mainContentPanel = new JPanel();

    public BaseCinemaFrame(String title, int width, int height) {
        super(title);
        initializeBaseFrame(width, height);
    }

    private void initializeBaseFrame(int width, int height) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(width, height);
        setMinimumSize(new Dimension(Math.min(600, width), Math.min(450, height)));
        setLocationRelativeTo(null);
        getContentPane().setBackground(CINEMA_BG);
        setLayout(new BorderLayout());

        mainContentPanel.setBackground(CINEMA_BG);
        mainContentPanel.setLayout(new BorderLayout());

        // Header bioskop default
        add(createCinemaHeader(), BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);

        // Shortcut Escape untuk kembali/tutup
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });
        setFocusable(true);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            SwingTheme.applyInterFont(this);
        }
        super.setVisible(visible);
    }

    /**
     * Membuat header khas bioskop dengan logo dan aksen garis emas.
     */
    protected JPanel createCinemaHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CINEMA_SURFACE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, CINEMA_GOLD),
                new EmptyBorder(14, 24, 14, 24)
        ));

        // Brand bioskop di sebelah kiri
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("🎬");
        logoIcon.setFont(SwingTheme.font(Font.PLAIN, 24));

        JLabel brandTitle = new JLabel("BIOSKOP NUSANTARA");
        brandTitle.setFont(SwingTheme.font(Font.BOLD, 18));
        brandTitle.setForeground(CINEMA_GOLD);

        JLabel brandSubtitle = new JLabel("| Layanan Pemesanan Tiket");
        brandSubtitle.setFont(SwingTheme.font(Font.PLAIN, 13));
        brandSubtitle.setForeground(CINEMA_MUTED);

        brandPanel.add(logoIcon);
        brandPanel.add(brandTitle);
        brandPanel.add(brandSubtitle);

        // Panel kanan untuk tombol navigasi atau info user
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        setupHeaderRightContent(rightPanel);

        headerPanel.add(brandPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Subclass dapat meng-override method ini untuk menambahkan tombol di header sebelah kanan.
     */
    protected void setupHeaderRightContent(JPanel rightPanel) {
        // Default kosong, dapat diisi oleh child class
    }

    /**
     * Helper method untuk membuat tombol bergaya bioskop emas atau sekunder.
     */
    public static JButton createCinemaButton(String text, boolean primary) {
        JButton button = new JButton(text);
        Color bg = primary ? CINEMA_GOLD : CINEMA_CARD;
        Color fg = primary ? CINEMA_BG : CINEMA_TEXT;
        Color hoverBg = primary ? CINEMA_GOLD_HOVER : new Color(48, 54, 62);

        button.setFont(SwingTheme.font(Font.BOLD, 13));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? CINEMA_GOLD : CINEMA_MUTED, 1),
                new EmptyBorder(8, 18, 8, 18)
        ));

        // Efek animasi hover halus
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bg);
                }
            }
        });

        return button;
    }

    /**
     * Helper method untuk membuat panel kartu (Card) dengan tema bioskop.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CINEMA_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 50, 58), 1),
                new EmptyBorder(14, 16, 14, 16)
        ));
        return card;
    }
}
