package mvcgui;

import Controler.MainFrameController;
import View.MainFrame;
import View.theme.SwingTheme;

import javax.swing.SwingUtilities;

public final class MVCGUI {
    private MVCGUI() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingTheme.install();
            MainFrame frame = new MainFrame();
            new MainFrameController(frame).initialize();
            frame.setVisible(true);
        });
    }
}
