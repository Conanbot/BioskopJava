package Controler;

import java.awt.CardLayout;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class NavigationController {
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final Set<String> registeredPages = new HashSet<>();

    public NavigationController(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = Objects.requireNonNull(cardLayout);
        this.cardPanel = Objects.requireNonNull(cardPanel);
        if (cardPanel.getLayout() != cardLayout) {
            throw new IllegalArgumentException("CardLayout harus dipasang pada panel navigasi.");
        }
    }

    public void registerPage(String id, JPanel view) {
        requireEventDispatchThread();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID halaman wajib diisi.");
        }
        Objects.requireNonNull(view, "Tampilan halaman wajib diisi.");
        if (!registeredPages.add(id)) {
            throw new IllegalStateException("ID halaman sudah terdaftar: " + id);
        }

        cardPanel.add(view, id);
    }

    public void showPage(String id) {
        requireEventDispatchThread();
        if (!registeredPages.contains(id)) {
            throw new IllegalArgumentException("Halaman tidak terdaftar: " + id);
        }

        cardLayout.show(cardPanel, id);
    }

    private static void requireEventDispatchThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Navigasi Swing harus dijalankan pada Event Dispatch Thread.");
        }
    }
}
