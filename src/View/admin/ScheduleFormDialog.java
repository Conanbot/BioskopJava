package View.admin;

import Model.dao.ScheduleDao;
import Model.entity.Schedule;
import Model.entity.Studio;
import View.theme.SwingTheme;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 * Dialog form untuk Tambah/Edit Jadwal Tayang.
 * Film dipilih dari dropdown yang diisi data dari tabel films.
 * Studio dipilih dari dropdown yang diisi data studio tersedia.
 * Total kursi dihitung otomatis dari dimensi studio yang dipilih.
 */
public final class ScheduleFormDialog extends JDialog {
    private final JComboBox<ScheduleDao.FilmSummary> filmCombo = new JComboBox<>();
    private final JComboBox<Studio> studioCombo = new JComboBox<>();
    private final JTextField showDateField = new JTextField("yyyy-MM-dd", 14);
    private final JTextField showTimeField = new JTextField("HH:mm", 8);
    private final JTextField ticketPriceField = new JTextField(14);
    private final JLabel totalSeatsLabel = new JLabel("—");
    private final JButton saveButton;
    private final JButton cancelButton = new JButton("Batal");
    private final JLabel errorLabel = new JLabel(" ");

    private boolean confirmed = false;

    /**
     * @param owner jendela induk
     * @param editSchedule null untuk Tambah, diisi untuk Edit
     * @param films daftar film untuk dropdown
     * @param studios daftar studio untuk dropdown
     */
    public ScheduleFormDialog(
            Window owner,
            Schedule editSchedule,
            List<ScheduleDao.FilmSummary> films,
            List<Studio> studios) {
        super(owner, editSchedule == null ? "Tambah Jadwal" : "Edit Jadwal",
                ModalityType.APPLICATION_MODAL);

        this.saveButton = new JButton(editSchedule == null ? "Simpan" : "Perbarui");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Populate combos
        for (ScheduleDao.FilmSummary f : films) filmCombo.addItem(f);
        for (Studio s : studios) {
            studioCombo.addItem(s);
        }

        // Auto-update total seats when studio changes
        studioCombo.addActionListener(e -> updateTotalSeats());
        updateTotalSeats();

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(SwingTheme.SURFACE);
        content.setBorder(new EmptyBorder(28, 32, 24, 32));

        content.add(buildTitle(editSchedule), BorderLayout.NORTH);
        content.add(buildFields(), BorderLayout.CENTER);
        content.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setMinimumSize(new Dimension(480, 420));
        setLocationRelativeTo(owner);

        if (editSchedule != null) {
            prefillForm(editSchedule, films, studios);
        }
    }

    private JPanel buildTitle(Schedule editSchedule) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel(editSchedule == null ? "Tambah Jadwal Tayang" : "Edit Jadwal Tayang");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(SwingTheme.TEXT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        return panel;
    }

    private JPanel buildFields() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints lblGbc = new GridBagConstraints();
        lblGbc.gridx = 0;
        lblGbc.anchor = GridBagConstraints.WEST;
        lblGbc.insets = new Insets(0, 0, 4, 12);

        GridBagConstraints fldGbc = new GridBagConstraints();
        fldGbc.gridx = 1;
        fldGbc.fill = GridBagConstraints.HORIZONTAL;
        fldGbc.weightx = 1.0;
        fldGbc.insets = new Insets(0, 0, 14, 0);

        styleField(showDateField);
        styleField(showTimeField);
        styleField(ticketPriceField);

        filmCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        filmCombo.setBackground(SwingTheme.BACKGROUND);
        filmCombo.setForeground(SwingTheme.TEXT);

        studioCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        studioCombo.setBackground(SwingTheme.BACKGROUND);
        studioCombo.setForeground(SwingTheme.TEXT);

        totalSeatsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        totalSeatsLabel.setForeground(SwingTheme.GOLD);

        int row = 0;
        addRow(panel, lblGbc, fldGbc, row++, "Film", filmCombo);
        addRow(panel, lblGbc, fldGbc, row++, "Studio", studioCombo);
        addRow(panel, lblGbc, fldGbc, row++, "Tanggal (yyyy-MM-dd)", showDateField);
        addRow(panel, lblGbc, fldGbc, row++, "Jam (HH:mm)", showTimeField);
        addRow(panel, lblGbc, fldGbc, row++, "Harga Tiket (Rp)", ticketPriceField);

        lblGbc.gridy = row;
        panel.add(createLabel("Total Kursi"), lblGbc);
        fldGbc.gridy = row++;
        fldGbc.fill = GridBagConstraints.NONE;
        panel.add(totalSeatsLabel, fldGbc);

        // Error label
        GridBagConstraints errGbc = new GridBagConstraints();
        errGbc.gridx = 0; errGbc.gridy = row;
        errGbc.gridwidth = 2;
        errGbc.anchor = GridBagConstraints.WEST;
        errGbc.insets = new Insets(4, 0, 0, 0);
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        errorLabel.setForeground(new java.awt.Color(220, 80, 80));
        panel.add(errorLabel, errGbc);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints lblGbc, GridBagConstraints fldGbc,
                        int row, String label, java.awt.Component field) {
        lblGbc.gridy = row;
        panel.add(createLabel(label), lblGbc);
        fldGbc.gridy = row;
        fldGbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, fldGbc);
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        SwingTheme.styleButton(saveButton, true);
        SwingTheme.styleButton(cancelButton, false);

        saveButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });
        cancelButton.addActionListener(e -> dispose());

        footer.add(cancelButton);
        footer.add(saveButton);
        return footer;
    }

    private void prefillForm(Schedule schedule, List<ScheduleDao.FilmSummary> films, List<Studio> studios) {
        // Select film
        for (int i = 0; i < filmCombo.getItemCount(); i++) {
            if (filmCombo.getItemAt(i).id() == schedule.getFilmId()) {
                filmCombo.setSelectedIndex(i);
                break;
            }
        }
        // Select studio
        for (int i = 0; i < studioCombo.getItemCount(); i++) {
            if (studioCombo.getItemAt(i).getId() == schedule.getStudioId()) {
                studioCombo.setSelectedIndex(i);
                break;
            }
        }
        showDateField.setText(schedule.getShowDate().toString());
        showTimeField.setText(schedule.getShowTime().toString());
        ticketPriceField.setText(schedule.getTicketPrice().toPlainString());
    }

    private void updateTotalSeats() {
        Studio selected = (Studio) studioCombo.getSelectedItem();
        if (selected != null) {
            totalSeatsLabel.setText(String.valueOf(selected.getTotalCapacity())
                    + " (" + selected.getSeatRows() + " × " + selected.getSeatCols() + ")");
        } else {
            totalSeatsLabel.setText("—");
        }
    }

    private boolean validateInput() {
        if (filmCombo.getSelectedItem() == null) {
            errorLabel.setText("Film wajib dipilih.");
            return false;
        }
        if (studioCombo.getSelectedItem() == null) {
            errorLabel.setText("Studio wajib dipilih.");
            return false;
        }
        try {
            LocalDate.parse(showDateField.getText().trim());
        } catch (DateTimeParseException e) {
            errorLabel.setText("Format tanggal tidak valid. Gunakan yyyy-MM-dd.");
            return false;
        }
        try {
            LocalTime.parse(showTimeField.getText().trim());
        } catch (DateTimeParseException e) {
            errorLabel.setText("Format jam tidak valid. Gunakan HH:mm.");
            return false;
        }
        try {
            BigDecimal price = new BigDecimal(ticketPriceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                errorLabel.setText("Harga tiket harus lebih dari nol.");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Harga tiket harus berupa angka valid.");
            return false;
        }
        errorLabel.setText(" ");
        return true;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        label.setForeground(SwingTheme.TEXT);
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        field.setBackground(SwingTheme.BACKGROUND);
        field.setForeground(SwingTheme.TEXT);
        field.setCaretColor(SwingTheme.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(100, 95, 85)),
                new EmptyBorder(8, 10, 8, 10)));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean isConfirmed() { return confirmed; }

    public long getSelectedFilmId() {
        ScheduleDao.FilmSummary film = (ScheduleDao.FilmSummary) filmCombo.getSelectedItem();
        return film != null ? film.id() : -1;
    }

    public Studio getSelectedStudio() {
        return (Studio) studioCombo.getSelectedItem();
    }

    public LocalDate getShowDate() {
        return LocalDate.parse(showDateField.getText().trim());
    }

    public LocalTime getShowTime() {
        return LocalTime.parse(showTimeField.getText().trim());
    }

    public BigDecimal getTicketPrice() {
        return new BigDecimal(ticketPriceField.getText().trim());
    }

    public void showError(String message) {
        errorLabel.setText(message == null ? " " : message);
    }
}
