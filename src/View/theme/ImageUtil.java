package View.theme;

import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Utilitas untuk memuat dan mengatur ukuran poster film.
 */
public final class ImageUtil {

    private ImageUtil() {
    }

    /**
     * Memuat gambar dari path file atau URL/resource dan menskalakannya sesuai target width & height.
     * Mengembalikan null jika path kosong atau gambar gagal dimuat.
     */
    public static ImageIcon loadScaledPoster(String path, int targetWidth, int targetHeight) {
        if (path == null || path.isBlank()) {
            return null;
        }

        try {
            Image img = null;
            File file = new File(path.trim());
            if (file.exists() && file.isFile()) {
                img = ImageIO.read(file);
            } else {
                // Coba muat dari ClassLoader resource
                java.net.URL url = ImageUtil.class.getResource(path.startsWith("/") ? path : "/" + path);
                if (url != null) {
                    img = ImageIO.read(url);
                } else if (path.startsWith("http://") || path.startsWith("https://")) {
                    img = ImageIO.read(new java.net.URI(path).toURL());
                }
            }

            if (img != null) {
                Image scaled = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignored) {
            // Jika gagal muat, biarkan null agar UI memakai placeholder
        }
        return null;
    }
}
