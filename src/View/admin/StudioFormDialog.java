package View.admin;

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 * Dialog form untuk Tambah/Edit Studio.
 * Dibuat secara programmatic (tanpa NetBeans GUI Builder).
 */
public final class StudioFormDialog extends JDialog {
    private final JTextField nameField = new JTextField(24);
    private final JSpinner rowsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    private final JSpinner colsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
    private final JButton saveButton;
    private final JButton cancelButton = new JButton("Batal");
    private final JLabel errorLabel = new JLabel(" ");

    private boolean confirmed = false;

    /**
     * @param owner jendela induk
     * @param editStudio null untuk form Tambah, diisi untuk form Edit
     */
    public StudioFormDialog(Window owner, Studio editStudio) {
        super(owner, editStudio == null ? "Tambah Studio" : "Edit Studio",
                ModalityType.APPLICATION_MODAL);

        this.saveButton = new JButton(editStudio == null ? "Simpan" : "Perbarui");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(SwingTheme.SURFACE);
        content.setBorder(new EmptyBorder(28, 32, 24, 32));

        content.add(buildTitle(editStudio), BorderLayout.NORTH);
        content.add(buildFields(), BorderLayout.CENTER);
        content.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setMinimumSize(new Dimension(380, 280));
        setLocationRelativeTo(owner);

        if (editStudio != null) {
            prefillForm(editStudio);
        }
    }

    private JPanel buildTitle(Studio editStudio) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel(editStudio == null ? "Tambah Studio Baru" : "Edit Data Studio");
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

        styleField(nameField);

        rowsSpinner.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        colsSpinner.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        rowsSpinner.setPreferredSize(new Dimension(100, 38));
        colsSpinner.setPreferredSize(new Dimension(100, 38));

        lblGbc.gridy = 0; fldGbc.gridy = 0;
        panel.add(createLabel("Nama Studio"), lblGbc);
        panel.add(nameField, fldGbc);

        lblGbc.gridy = 1; fldGbc.gridy = 1;
        panel.add(createLabel("Jumlah Baris Kursi"), lblGbc);
        fldGbc.fill = GridBagConstraints.NONE;
        panel.add(rowsSpinner, fldGbc);

        lblGbc.gridy = 2; fldGbc.gridy = 2;
        panel.add(createLabel("Jumlah Kolom Kursi"), lblGbc);
        panel.add(colsSpinner, fldGbc);

        // Error label spans both columns
        GridBagConstraints errGbc = new GridBagConstraints();
        errGbc.gridx = 0; errGbc.gridy = 3;
        errGbc.gridwidth = 2;
        errGbc.anchor = GridBagConstraints.WEST;
        errGbc.insets = new Insets(4, 0, 0, 0);
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        errorLabel.setForeground(new java.awt.Color(220, 80, 80));
        panel.add(errorLabel, errGbc);

        return panel;
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

    private void prefillForm(Studio studio) {
        nameField.setText(studio.getName());
        rowsSpinner.setValue(studio.getSeatRows());
        colsSpinner.setValue(studio.getSeatCols());
    }

    private boolean validateInput() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Nama studio wajib diisi.");
            return false;
        }
        if (name.length() > 80) {
            errorLabel.setText("Nama studio maksimal 80 karakter.");
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
    public String getStudioName() { return nameField.getText().trim(); }
    public int getSeatRows() { return (int) rowsSpinner.getValue(); }
    public int getSeatCols() { return (int) colsSpinner.getValue(); }

    public void showError(String message) {
        errorLabel.setText(message == null ? " " : message);
    }
}
