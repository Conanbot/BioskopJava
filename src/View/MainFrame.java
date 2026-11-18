package View;

import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public final class MainFrame extends javax.swing.JFrame {
    private final JPanel cardsPanel = new JPanel(new CardLayout());
    private final JLabel pageTitle = new JLabel();
    private final JButton diagnosticsButton = createNavigationButton("Diagnostik");
    private final JButton accountsButton = createNavigationButton("Login / Register");
    private final JButton customerButton = createNavigationButton("Customer");
    private final JButton adminButton = createNavigationButton("Admin");
    private final JButton filmsButton = createNavigationButton("Film");

    public MainFrame() {
        super("Sistem Booking Bioskop");
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("MainFrame harus dibuat pada Event Dispatch Thread.");
        }
        setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 500));
        setSize(900, 560);
        setLocationRelativeTo(null);

        cardsPanel.setBackground(SwingTheme.BACKGROUND);
        pageTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        pageTitle.setForeground(SwingTheme.TEXT);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SwingTheme.BACKGROUND);
        root.add(createSidebar(
                diagnosticsButton,
                accountsButton,
                customerButton,
                adminButton,
                filmsButton), BorderLayout.WEST);

        JPanel workspace = new JPanel(new BorderLayout(0, 22));
        workspace.setBackground(SwingTheme.BACKGROUND);
        workspace.setBorder(new EmptyBorder(30, 34, 30, 34));
        workspace.add(pageTitle, BorderLayout.NORTH);
        workspace.add(cardsPanel, BorderLayout.CENTER);
        root.add(workspace, BorderLayout.CENTER);
        setContentPane(root);
    }

    public JPanel getCardsPanel() {
        return cardsPanel;
    }

    public JLabel getPageTitle() {
        return pageTitle;
    }

    public JButton getDiagnosticsButton() {
        return diagnosticsButton;
    }

    public JButton getAccountsButton() {
        return accountsButton;
    }

    public JButton getCustomerButton() {
        return customerButton;
    }

    public JButton getAdminButton() {
        return adminButton;
    }

    public JButton getFilmsButton() {
        return filmsButton;
    }

    private static JButton createNavigationButton(String label) {
        JButton button = new JButton(label);
        SwingTheme.styleNavigationButton(button);
        button.setAlignmentX(LEFT_ALIGNMENT);
        return button;
    }

    private static JPanel createSidebar(
            JButton diagnosticsButton,
            JButton accountsButton,
            JButton customerButton,
            JButton adminButton,
            JButton filmsButton) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new javax.swing.BoxLayout(sidebar, javax.swing.BoxLayout.Y_AXIS));
        sidebar.setBackground(SwingTheme.SURFACE);
        sidebar.setBorder(new EmptyBorder(26, 18, 24, 18));
        sidebar.setPreferredSize(new Dimension(230, 0));

        JLabel brand = new JLabel("Sistem Booking");
        brand.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        brand.setForeground(SwingTheme.TEXT);
        JLabel subtitle = new JLabel("Bioskop");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitle.setForeground(SwingTheme.GOLD);
        JLabel sectionTitle = new JLabel("MENU");
        sectionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        sectionTitle.setForeground(SwingTheme.MUTED_TEXT);

        sidebar.add(brand);
        sidebar.add(javax.swing.Box.createVerticalStrut(4));
        sidebar.add(subtitle);
        sidebar.add(javax.swing.Box.createVerticalStrut(34));
        sidebar.add(sectionTitle);
        sidebar.add(javax.swing.Box.createVerticalStrut(12));
        addNavigationButton(sidebar, diagnosticsButton);
        addNavigationButton(sidebar, accountsButton);
        addNavigationButton(sidebar, customerButton);
        addNavigationButton(sidebar, adminButton);
        addNavigationButton(sidebar, filmsButton);
        return sidebar;
    }

    private static void addNavigationButton(JPanel sidebar, JButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        sidebar.add(button);
        sidebar.add(javax.swing.Box.createVerticalStrut(8));
    }
}
