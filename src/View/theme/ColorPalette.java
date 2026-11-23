package View.theme;

import java.awt.Color;

/**
 * Palet warna global bergaya TIX ID:
 * - Royal Navy (#001A3A) untuk header, sidebar, dan tombol utama
 * - Warm Gold (#F5A623) untuk aksen brand, badge, dan status aktif
 * - Light Canvas (#F5F7FA) & Crisp White (#FFFFFF) untuk permukaan konten yang bersih
 */
public final class ColorPalette {
    public static final Color NAVY = new Color(0, 26, 58);
    public static final Color NAVY_HOVER = new Color(12, 46, 92);
    public static final Color NAVY_PRESSED = new Color(0, 18, 42);

    public static final Color BACKGROUND = new Color(245, 247, 250);
    public static final Color SURFACE = new Color(255, 255, 255);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color TEXT = new Color(15, 23, 42);
    public static final Color MUTED_TEXT = new Color(100, 116, 139);

    public static final Color GOLD = new Color(245, 166, 35);
    public static final Color GOLD_HOVER = new Color(255, 184, 61);
    public static final Color GOLD_PRESSED = new Color(217, 140, 16);

    public static final Color FOCUS = new Color(37, 99, 235);
    public static final Color DISABLED = new Color(203, 213, 225);
    public static final Color SURFACE_HOVER = new Color(241, 245, 249);

    private ColorPalette() {
    }
}
