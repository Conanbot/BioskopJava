package Controler;

import View.theme.SwingTheme;

import java.awt.CardLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class NavigationController {
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final JLabel pageTitle;
    private final Map<String, Route> routes = new LinkedHashMap<>();

    public NavigationController(CardLayout cardLayout, JPanel cardPanel, JLabel pageTitle) {
        this.cardLayout = Objects.requireNonNull(cardLayout);
        this.cardPanel = Objects.requireNonNull(cardPanel);
        this.pageTitle = Objects.requireNonNull(pageTitle);
        if (cardPanel.getLayout() != cardLayout) {
            throw new IllegalArgumentException("CardLayout harus dipasang pada panel navigasi.");
        }
    }

    public void registerPage(String id, String title, JPanel view, JButton navigationButton) {
        requireEventDispatchThread();
        if (id == null || id.isBlank() || title == null || title.isBlank()) {
            throw new IllegalArgumentException("ID dan judul halaman wajib diisi.");
        }
        Objects.requireNonNull(view, "Tampilan halaman wajib diisi.");
        Objects.requireNonNull(navigationButton, "Tombol navigasi wajib diisi.");
        if (routes.containsKey(id)) {
            throw new IllegalStateException("ID halaman sudah terdaftar: " + id);
        }

        routes.put(id, new Route(title, navigationButton));
        cardPanel.add(view, id);
        navigationButton.addActionListener(event -> showPage(id));
    }

    public void showPage(String id) {
        requireEventDispatchThread();
        Route route = routes.get(id);
        if (route == null) {
            throw new IllegalArgumentException("Halaman tidak terdaftar: " + id);
        }

        cardLayout.show(cardPanel, id);
        pageTitle.setText(route.title());
        routes.forEach((routeId, registeredRoute) ->
                SwingTheme.setNavigationActive(
                        registeredRoute.navigationButton(),
                        routeId.equals(id)));
    }

    private static void requireEventDispatchThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Navigasi Swing harus dijalankan pada Event Dispatch Thread.");
        }
    }

    private record Route(String title, JButton navigationButton) {
    }
}
