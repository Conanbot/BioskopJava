package View.admin;

import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Panel induk untuk modul Admin. Berisi navigasi samping dan CardLayout untuk sub-panel.
 * Sub-panel: Dashboard, Studio, Jadwal, Cancel/Refund Booking.
 */
public class AdminPanel extends javax.swing.JPanel {
    public static final String CARD_DASHBOARD = "dashboard";
    public static final String CARD_STUDIO = "studio";
    public static final String CARD_SCHEDULE = "schedule";
    public static final String CARD_CANCEL_REFUND = "cancelrefund";

    private CardLayout innerCardLayout;

    public AdminPanel() {
        initComponents();
        postInit();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        innerCardLayout = new CardLayout();
        innerCards = new JPanel(innerCardLayout);
        sidebarPanel = new JPanel();

        dashboardPanel = new AdminDashboardPanel();
        studioPanel = new StudioManagementPanel();
        schedulePanel = new ScheduleManagementPanel();
        cancelRefundPanel = new AdminCancelRefundPanel();

        dashboardButton = createNavBtn("Dashboard");
        studioButton = createNavBtn("Studio");
        scheduleButton = createNavBtn("Jadwal Tayang");
        cancelRefundButton = createNavBtn("Batalkan / Refund");
        logoutButton = createNavBtn("Logout");

        usernameLabel = new JLabel();

        setLayout(new BorderLayout());

        innerCards.setBackground(SwingTheme.BACKGROUND);
        innerCards.add(dashboardPanel, CARD_DASHBOARD);
        innerCards.add(studioPanel, CARD_STUDIO);
        innerCards.add(schedulePanel, CARD_SCHEDULE);
        innerCards.add(cancelRefundPanel, CARD_CANCEL_REFUND);

        add(buildSidebar(), BorderLayout.WEST);
        add(innerCards, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void postInit() {
        setBackground(SwingTheme.BACKGROUND);
        showDashboard();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SwingTheme.SURFACE);
        sidebar.setBorder(new EmptyBorder(20, 14, 20, 14));
        sidebar.setPreferredSize(new Dimension(190, 0));

        JLabel adminTitle = new JLabel("Admin");
        adminTitle.setFont(new Font("Inter", Font.BOLD, 18));
        adminTitle.setForeground(SwingTheme.GOLD);
        adminTitle.setAlignmentX(LEFT_ALIGNMENT);

        usernameLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        usernameLabel.setForeground(SwingTheme.MUTED_TEXT);
        usernameLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel menuLabel = new JLabel("MENU");
        menuLabel.setFont(new Font("Inter", Font.BOLD, 11));
        menuLabel.setForeground(SwingTheme.MUTED_TEXT);
        menuLabel.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(adminTitle);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(usernameLabel);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(menuLabel);
        sidebar.add(Box.createVerticalStrut(8));
        addSidebarBtn(sidebar, dashboardButton);
        addSidebarBtn(sidebar, studioButton);
        addSidebarBtn(sidebar, scheduleButton);
        addSidebarBtn(sidebar, cancelRefundButton);
        sidebar.add(Box.createVerticalGlue());
        addSidebarBtn(sidebar, logoutButton);

        return sidebar;
    }

    private void addSidebarBtn(JPanel sidebar, JButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(6));
    }

    private JButton createNavBtn(String label) {
        JButton btn = new JButton(label);
        SwingTheme.styleNavigationButton(btn);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    public void showDashboard() {
        innerCardLayout.show(innerCards, CARD_DASHBOARD);
        setActiveButton(dashboardButton);
    }

    public void showStudio() {
        innerCardLayout.show(innerCards, CARD_STUDIO);
        setActiveButton(studioButton);
    }

    public void showSchedule() {
        innerCardLayout.show(innerCards, CARD_SCHEDULE);
        setActiveButton(scheduleButton);
    }

    public void showCancelRefund() {
        innerCardLayout.show(innerCards, CARD_CANCEL_REFUND);
        setActiveButton(cancelRefundButton);
    }

    private void setActiveButton(JButton active) {
        JButton[] all = {dashboardButton, studioButton, scheduleButton, cancelRefundButton, logoutButton};
        for (JButton btn : all) {
            SwingTheme.setNavigationActive(btn, btn == active);
        }
    }

    public void setUsernameDisplay(String username) {
        usernameLabel.setText("@" + username);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public AdminDashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public StudioManagementPanel getStudioPanel() {
        return studioPanel;
    }

    public ScheduleManagementPanel getSchedulePanel() {
        return schedulePanel;
    }

    public AdminCancelRefundPanel getCancelRefundPanel() {
        return cancelRefundPanel;
    }

    public JButton getDashboardButton() {
        return dashboardButton;
    }

    public JButton getStudioButton() {
        return studioButton;
    }

    public JButton getScheduleButton() {
        return scheduleButton;
    }

    public JButton getCancelRefundButton() {
        return cancelRefundButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelRefundButton;
    private javax.swing.JButton dashboardButton;
    private AdminDashboardPanel dashboardPanel;
    private javax.swing.JPanel innerCards;
    private javax.swing.JButton logoutButton;
    private AdminCancelRefundPanel cancelRefundPanel;
    private ScheduleManagementPanel schedulePanel;
    private javax.swing.JButton scheduleButton;
    private javax.swing.JPanel sidebarPanel;
    private StudioManagementPanel studioPanel;
    private javax.swing.JButton studioButton;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
