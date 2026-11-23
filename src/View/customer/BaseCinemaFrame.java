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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Superclass untuk semua JFrame bertema bioskop bergaya TIX ID.
 * Palet: Royal Navy (#001A3A), Warm Gold (#F5A623), Light Canvas (#F5F7FA), Crisp White (#FFFFFF).
 */
public abstract class BaseCinemaFrame extends JFrame {

    // Palet warna tema TIX ID
    public static final Color CINEMA_BG = ColorPalette.BACKGROUND;        // #F5F7FA
    public static final Color CINEMA_SURFACE = ColorPalette.NAVY;         // #001A3A (Header Royal Navy)
    public static final Color CINEMA_CARD = ColorPalette.SURFACE;         // #FFFFFF (Kartu putih bersih)
    public static final Color CINEMA_GOLD = ColorPalette.GOLD;            // #F5A623 (Aksen emas TIX ID)
    public static final Color CINEMA_GOLD_HOVER = ColorPalette.GOLD_HOVER;
    public static final Color CINEMA_RED = new Color(220, 38, 38);        // #DC2626
    public static final Color CINEMA_RED_HOVER = new Color(185, 28, 28);
    public static final Color CINEMA_TEXT = ColorPalette.TEXT;            // #0F172A
    public static final Color CINEMA_MUTED = ColorPalette.MUTED_TEXT;     // #64748B

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

        add(createCinemaHeader(), BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);

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

    protected JPanel createCinemaHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CINEMA_SURFACE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, CINEMA_GOLD),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel brandTitle = new JLabel("TIX ID  •  BIOSKOP NUSANTARA");
        brandTitle.setFont(SwingTheme.font(Font.BOLD, 18));
        brandTitle.setForeground(CINEMA_GOLD);

        JLabel brandSubtitle = new JLabel("| Layanan Pemesanan Tiket");
        brandSubtitle.setFont(SwingTheme.font(Font.PLAIN, 13));
        brandSubtitle.setForeground(new Color(203, 213, 225));

        brandPanel.add(brandTitle);
        brandPanel.add(brandSubtitle);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        setupHeaderRightContent(rightPanel);

        headerPanel.add(brandPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    protected void setupHeaderRightContent(JPanel rightPanel) {
        // Default kosong, dapat diisi oleh child class
    }

    /**
     * Helper method untuk membuat tombol bergaya TIX ID (Navy utama atau Putih sekunder).
     */
    public static JButton createCinemaButton(String text, boolean primary) {
        JButton button = new JButton(text);
        Color bg = primary ? ColorPalette.NAVY : ColorPalette.SURFACE;
        Color fg = primary ? Color.WHITE : ColorPalette.NAVY;
        Color hoverBg = primary ? ColorPalette.NAVY_HOVER : ColorPalette.SURFACE_HOVER;
        Color borderClr = primary ? ColorPalette.NAVY : new Color(203, 213, 225);

        button.setFont(SwingTheme.font(Font.BOLD, 13));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderClr, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));

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
     * Helper method untuk membuat panel kartu putih bersih bergaya TIX ID.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CINEMA_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorPalette.BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }
}
