package View.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;

public final class SwingTheme {
    private static final String[] BUNDLED_INTER_FONTS = {
            "/resources/fonts/Inter_18pt-Regular.ttf",
            "/resources/fonts/Inter_18pt-Bold.ttf",
            "/resources/fonts/Inter_18pt-Italic.ttf",
            "/resources/fonts/Inter_18pt-BoldItalic.ttf"
    };

    private static volatile String activeFontFamily = "Inter";
    private static volatile boolean fontsLoaded = false;

    public static final Color BACKGROUND = ColorPalette.BACKGROUND;
    public static final Color SURFACE = ColorPalette.SURFACE;
    public static final Color TEXT = ColorPalette.TEXT;
    public static final Color MUTED_TEXT = ColorPalette.MUTED_TEXT;
    public static final Color GOLD = ColorPalette.GOLD;
    public static final Color GOLD_HOVER = ColorPalette.GOLD_HOVER;
    public static final Color FOCUS = ColorPalette.FOCUS;

    private SwingTheme() {
    }

    public static void install() {
        loadBundledFonts();
        installGlobalFont(activeFontFamily);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Panel.background", SURFACE);
        UIManager.put("Button.background", GOLD);
        UIManager.put("Button.foreground", BACKGROUND);
        UIManager.put("Button.focus", FOCUS);
        UIManager.put("Button.disabledText", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("OptionPane.buttonFont", font(Font.BOLD, 14));
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 14));
    }

    public static String getFontFamily() {
        loadBundledFonts();
        return activeFontFamily;
    }

    public static Font font(int style, int size) {
        return new Font(getFontFamily(), style, size);
    }

    public static void applyInterFont(Component root) {
        if (root == null) {
            return;
        }
        String family = getFontFamily();
        Font current = root.getFont();
        if (current != null) {
            root.setFont(new Font(family, current.getStyle(), current.getSize()));
        }
        if (root instanceof JTable table && table.getTableHeader() != null) {
            Font headerFont = table.getTableHeader().getFont();
            if (headerFont != null) {
                table.getTableHeader().setFont(
                        new Font(family, headerFont.getStyle(), headerFont.getSize()));
            }
        }
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyInterFont(child);
            }
        }
    }

    private static synchronized void loadBundledFonts() {
        if (fontsLoaded) {
            return;
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String registeredFamily = null;
        for (String resourcePath : BUNDLED_INTER_FONTS) {
            try (InputStream stream = SwingTheme.class.getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    continue;
                }
                Font loaded = Font.createFont(Font.TRUETYPE_FONT, stream);
                ge.registerFont(loaded);
                if (registeredFamily == null) {
                    registeredFamily = loaded.getFamily();
                }
            } catch (FontFormatException | IOException ignored) {
                // Fallback to system font if bundled resource cannot be loaded
            }
        }
        if (registeredFamily != null && !registeredFamily.isBlank()) {
            activeFontFamily = registeredFamily;
        }
        fontsLoaded = true;
    }

    private static void installGlobalFont(String fontFamily) {
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);
            if (value instanceof FontUIResource existingFont) {
                defaults.put(key, new FontUIResource(
                        fontFamily,
                        existingFont.getStyle(),
                        existingFont.getSize()));
            }
        }
    }

    public static void styleButton(JButton button, boolean primary) {
        Color background = primary ? GOLD : SURFACE;
        Color foreground = primary ? BACKGROUND : TEXT;
        Border normalBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD),
                BorderFactory.createEmptyBorder(9, 16, 9, 16));
        Border focusBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FOCUS, 2),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(primary ? BACKGROUND : FOCUS),
                        BorderFactory.createEmptyBorder(7, 14, 7, 14)));

        button.setFont(font(Font.BOLD, 14));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(normalBorder);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMinimumSize(new Dimension(100, 40));
        button.setPreferredSize(new Dimension(160, 42));
        button.addPropertyChangeListener("enabled", event -> {
            button.setBackground(button.isEnabled() ? background : ColorPalette.DISABLED);
            button.setForeground(button.isEnabled() ? foreground : TEXT);
            button.setCursor(Cursor.getPredefinedCursor(
                    button.isEnabled() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        });

        button.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                button.setBorder(focusBorder);
            }

            @Override
            public void focusLost(FocusEvent event) {
                button.setBorder(normalBorder);
            }
        });
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(primary ? GOLD_HOVER : ColorPalette.SURFACE_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(button.isEnabled() ? background : new Color(85, 81, 74));
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(primary ? ColorPalette.GOLD_PRESSED : BACKGROUND);
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(button.contains(event.getPoint())
                            ? (primary ? GOLD_HOVER : ColorPalette.SURFACE_HOVER)
                            : background);
                }
            }
        });
    }

    public static void styleNavigationButton(JButton button) {
        button.setFont(font(Font.BOLD, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMinimumSize(new Dimension(160, 42));
        button.setPreferredSize(new Dimension(170, 42));
        button.setBorder(navigationBorder(false));
        setNavigationActive(button, false);
        button.addPropertyChangeListener("enabled", event -> {
            button.setCursor(Cursor.getPredefinedCursor(
                    button.isEnabled() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            if (!button.isEnabled()) {
                button.setBackground(ColorPalette.DISABLED);
                button.setForeground(TEXT);
            } else {
                setNavigationActive(
                        button,
                        Boolean.TRUE.equals(button.getClientProperty("navigationActive")));
            }
        });
        button.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                button.setBorder(navigationFocusBorder());
            }

            @Override
            public void focusLost(FocusEvent event) {
                button.setBorder(navigationBorder(
                        Boolean.TRUE.equals(button.getClientProperty("navigationActive"))));
            }
        });
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(Boolean.TRUE.equals(button.getClientProperty("navigationActive"))
                            ? GOLD_HOVER : ColorPalette.SURFACE_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                if (button.isEnabled()) {
                    setNavigationActive(
                            button,
                            Boolean.TRUE.equals(button.getClientProperty("navigationActive")));
                }
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(BACKGROUND);
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(button.contains(event.getPoint())
                            ? GOLD_HOVER
                            : (Boolean.TRUE.equals(button.getClientProperty("navigationActive"))
                                    ? GOLD : SURFACE));
                }
            }
        });
    }

    public static void setNavigationActive(JButton button, boolean active) {
        button.putClientProperty("navigationActive", active);
        button.setBackground(active ? GOLD : SURFACE);
        button.setForeground(active ? BACKGROUND : TEXT);
        button.setBorder(navigationBorder(active));
    }

    private static Border navigationBorder(boolean active) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? GOLD : SURFACE),
                BorderFactory.createEmptyBorder(9, 12, 9, 12));
    }

    private static Border navigationFocusBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FOCUS, 2),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BACKGROUND),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)));
    }
}
