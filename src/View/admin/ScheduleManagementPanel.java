package View.admin;

import Model.entity.Schedule;
import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Panel CRUD Jadwal Tayang untuk Admin.
 * Menampilkan daftar jadwal dengan informasi film, studio, waktu, dan harga.
 */
public class ScheduleManagementPanel extends javax.swing.JPanel {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");



    private final String[] COLUMNS = {
            "ID", "Film", "Studio", "Tanggal", "Jam", "Harga (Rp)", "Total Kursi", "Tersedia"
    };
    private DefaultTableModel tableModel;

    public ScheduleManagementPanel() {
        initComponents();
        postInit();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        addButton = new JButton("Tambah");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Hapus");
        refreshButton = new JButton("Refresh");
        statusLabel = new JLabel(" ");

        setLayout(new BorderLayout(0, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void postInit() {
        setBackground(SwingTheme.BACKGROUND);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        setupTable();
        updateButtonStates();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Kelola Jadwal Tayang");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(SwingTheme.TEXT);

        JLabel subtitle = new JLabel("Tambah, edit, atau hapus jadwal tayang film.");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(SwingTheme.MUTED_TEXT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        SwingTheme.styleButton(refreshButton, false);
        actionPanel.add(refreshButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildTablePanel() {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(80, 76, 68)));
        scrollPane.getViewport().setBackground(SwingTheme.SURFACE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonRow.setOpaque(false);

        SwingTheme.styleButton(addButton, true);
        SwingTheme.styleButton(editButton, false);
        SwingTheme.styleButton(deleteButton, false);

        buttonRow.add(addButton);
        buttonRow.add(editButton);
        buttonRow.add(deleteButton);

        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        statusLabel.setForeground(SwingTheme.MUTED_TEXT);

        footer.add(buttonRow);
        footer.add(Box.createVerticalStrut(8));
        footer.add(statusLabel);

        return footer;
    }

    private void setupTable() {
        table.setBackground(SwingTheme.SURFACE);
        table.setForeground(SwingTheme.TEXT);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        table.getTableHeader().setBackground(SwingTheme.SURFACE);
        table.getTableHeader().setForeground(SwingTheme.MUTED_TEXT);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        table.setSelectionBackground(new java.awt.Color(214, 169, 75, 60));
        table.setSelectionForeground(SwingTheme.TEXT);
        table.setGridColor(new java.awt.Color(60, 56, 50));
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(70);

        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates());
    }

    // ── Data population ───────────────────────────────────────────────────────

    public void populateTable(List<Schedule> schedules) {
        tableModel.setRowCount(0);
        for (Schedule s : schedules) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getFilmTitle() != null ? s.getFilmTitle() : "Film #" + s.getFilmId(),
                    s.getStudioName() != null ? s.getStudioName() : "Studio #" + s.getStudioId(),
                    s.getShowDate().format(DATE_FMT),
                    s.getShowTime().format(TIME_FMT),
                    String.format("%,.0f", s.getTicketPrice()),
                    s.getTotalSeats(),
                    s.getAvailableSeats()
            });
        }
        updateButtonStates();
    }

    public long getSelectedScheduleId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        return (long) tableModel.getValueAt(row, 0);
    }

    private void updateButtonStates() {
        boolean hasSelection = table.getSelectedRow() >= 0;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    // ── Status messages ───────────────────────────────────────────────────────

    public void setStatus(String message) {
        statusLabel.setText(message == null ? " " : message);
        statusLabel.setForeground(SwingTheme.MUTED_TEXT);
    }

    public void setStatusSuccess(String message) {
        statusLabel.setText(message == null ? " " : message);
        statusLabel.setForeground(new java.awt.Color(80, 200, 120));
    }

    public void setStatusError(String message) {
        statusLabel.setText(message == null ? " " : message);
        statusLabel.setForeground(new java.awt.Color(220, 80, 80));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public JButton getAddButton() { return addButton; }
    public JButton getEditButton() { return editButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getRefreshButton() { return refreshButton; }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
