package Controler;

import java.awt.Component;

import javax.swing.JOptionPane;

public final class AlertUtil {
    private AlertUtil() {
    }

    public static void showInformation(Component owner, String title, String message) {
        show(JOptionPane.INFORMATION_MESSAGE, owner, title, message);
    }

    public static void showWarning(Component owner, String title, String message) {
        show(JOptionPane.WARNING_MESSAGE, owner, title, message);
    }

    public static void showError(Component owner, String title, String message) {
        show(JOptionPane.ERROR_MESSAGE, owner, title, message);
    }

    private static void show(int messageType, Component owner, String title, String message) {
        if (title == null || title.isBlank() || message == null || message.isBlank()) {
            throw new IllegalArgumentException("Judul dan pesan notifikasi wajib diisi.");
        }
        JOptionPane.showMessageDialog(owner, message, title, messageType);
    }
}
