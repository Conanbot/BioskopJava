package View.theme;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Utilitas untuk memuat, menskalakan (aspect-fill), meng-cache, dan menyalin poster film.
 */
public final class ImageUtil {

    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();

    private ImageUtil() {
    }

    /**
     * Memuat gambar poster dari berbagai sumber (path absolut, relatif ke src/,
     * nama file di src/poster/, classpath resource, atau URL HTTP/HTTPS),
     * lalu menskalakannya secara proporsional (aspect-fill / center-crop) sesuai target width & height.
     * Mengembalikan null jika path kosong atau gambar gagal dimuat.
     */
    public static ImageIcon loadScaledPoster(String path, int targetWidth, int targetHeight) {
        if (path == null || path.isBlank() || targetWidth <= 0 || targetHeight <= 0) {
            return null;
        }

        String trimmed = path.trim();
        String cacheKey = trimmed + "|" + targetWidth + "x" + targetHeight;
        ImageIcon cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            BufferedImage img = loadRawImage(trimmed);
            if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
                return null;
            }

            ImageIcon icon = new ImageIcon(scaleCover(img, targetWidth, targetHeight));
            CACHE.put(cacheKey, icon);
            return icon;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static void clearCacheForPath(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        String prefix = path.trim() + "|";
        CACHE.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /**
     * Menyalin file poster eksternal ke folder src/poster/ (dan build/classes/poster/ jika ada),
     * lalu mengembalikan path relatif standar "poster/<nama_file>".
     */
    public static String copyPosterToProject(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.isFile()) {
            throw new IOException("File poster tidak valid.");
        }

        Path posterDir = Path.of("src", "poster").toAbsolutePath().normalize();
        Files.createDirectories(posterDir);

        Path sourcePath = sourceFile.toPath().toAbsolutePath().normalize();
        String targetFileName;
        Path destPath;

        if (sourcePath.startsWith(posterDir) && sourcePath.getParent().equals(posterDir)) {
            targetFileName = sourceFile.getName();
            destPath = sourcePath;
        } else {
            targetFileName = sanitizeFileName(sourceFile.getName());
            destPath = posterDir.resolve(targetFileName);
            if (Files.exists(destPath) && Files.size(destPath) != Files.size(sourcePath)) {
                int dot = targetFileName.lastIndexOf('.');
                String base = dot > 0 ? targetFileName.substring(0, dot) : targetFileName;
                String ext = dot > 0 ? targetFileName.substring(dot) : ".png";
                targetFileName = base + "_" + System.currentTimeMillis() + ext;
                destPath = posterDir.resolve(targetFileName);
            }
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        }

        Path buildClassesDir = Path.of("build", "classes");
        if (Files.isDirectory(buildClassesDir)) {
            Path buildPosterDir = buildClassesDir.resolve("poster");
            Files.createDirectories(buildPosterDir);
            Files.copy(destPath, buildPosterDir.resolve(targetFileName), StandardCopyOption.REPLACE_EXISTING);
        }

        String relative = "poster/" + targetFileName;
        clearCacheForPath(relative);
        return relative;
    }

    private static BufferedImage loadRawImage(String trimmed) throws Exception {
        // 1. Direct file (path absolut atau relatif dari project root, mis. "src/poster/foo.png")
        File direct = new File(trimmed);
        if (direct.isFile()) {
            return ImageIO.read(direct);
        }

        // 2. Relatif terhadap folder src/ (mis. "poster/dune_part_two.png")
        String normalizedRel = trimmed.replace('\\', '/');
        while (normalizedRel.startsWith("/")) {
            normalizedRel = normalizedRel.substring(1);
        }
        File srcRel = new File("src", normalizedRel);
        if (srcRel.isFile()) {
            return ImageIO.read(srcRel);
        }

        // 3. Nama file langsung di src/poster/ (mis. "dune_part_two.png")
        File bareInPoster = new File("src/poster", normalizedRel);
        if (bareInPoster.isFile()) {
            return ImageIO.read(bareInPoster);
        }

        // 4. Classpath resource (mis. "/poster/dune_part_two.png")
        URL res = ImageUtil.class.getResource("/" + normalizedRel);
        if (res == null && !normalizedRel.startsWith("poster/")) {
            res = ImageUtil.class.getResource("/poster/" + normalizedRel);
        }
        if (res != null) {
            return ImageIO.read(res);
        }

        // 5. URL HTTP/HTTPS
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return ImageIO.read(new URI(trimmed).toURL());
        }

        return null;
    }

    private static Image scaleCover(BufferedImage src, int targetW, int targetH) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        double scale = Math.max((double) targetW / srcW, (double) targetH / srcH);
        int drawW = (int) Math.ceil(srcW * scale);
        int drawH = (int) Math.ceil(srcH * scale);
        int x = (targetW - drawW) / 2;
        int y = (targetH - drawH) / 2;

        BufferedImage canvas = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, x, y, drawW, drawH, null);
        g2.dispose();
        return canvas;
    }

    private static String sanitizeFileName(String original) {
        int dot = original.lastIndexOf('.');
        String base = dot > 0 ? original.substring(0, dot) : original;
        String ext = dot > 0 ? original.substring(dot).toLowerCase(Locale.ROOT) : ".png";
        String cleanBase = base.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (cleanBase.isEmpty()) {
            cleanBase = "poster_" + System.currentTimeMillis();
        }
        return cleanBase + ext;
    }
}
