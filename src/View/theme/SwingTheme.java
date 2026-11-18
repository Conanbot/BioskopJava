package View.theme;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

public final class SwingTheme {
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
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Panel.background", SURFACE);
        UIManager.put("Button.background", GOLD);
        UIManager.put("Button.foreground", BACKGROUND);
        UIManager.put("Button.focus", FOCUS);
        UIManager.put("Button.disabledText", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("OptionPane.buttonFont", new Font(Font.SANS_SERIF, Font.BOLD, 14));
        UIManager.put("OptionPane.messageFont", new Font(Font.SANS_SERIF, Font.PLAIN, 14));
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

        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(normalBorder);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMinimumSize(new Dimension(120, 44));
        button.setPreferredSize(new Dimension(210, 46));
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
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMinimumSize(new Dimension(180, 44));
        button.setPreferredSize(new Dimension(190, 46));
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
