package Controler;

import Model.entity.User;
import Model.enums.Role;

/**
 * Menyimpan informasi pengguna yang sedang login.
 *
 * <p>SessionManager adalah singleton yang menjadi sumber identitas terpercaya.
 * Controller dan service harus mengambil ID dan role dari sini,
 * bukan dari input form atau parameter navigasi.
 *
 * <p>Session dibuat hanya setelah autentikasi berhasil melalui AuthService.
 * Session dibersihkan saat logout.
 */
public final class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    private volatile User currentUser = null;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Membuat session setelah login berhasil.
     * Hanya dipanggil oleh LoginController setelah AuthService.login() sukses.
     */
    public void createSession(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Pengguna terautentikasi wajib diisi untuk membuat sesi.");
        }
        this.currentUser = user;
    }

    /**
     * Membersihkan session saat logout.
     */
    public void clearSession() {
        this.currentUser = null;
    }

    /**
     * Mengembalikan pengguna yang sedang login, atau null jika belum ada session.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Memeriksa apakah ada session aktif.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Memeriksa apakah pengguna yang login adalah Admin.
     * Mengembalikan false jika tidak ada session.
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    /**
     * Memeriksa apakah pengguna yang login adalah Customer.
     * Mengembalikan false jika tidak ada session.
     */
    public boolean isCustomer() {
        return currentUser != null && currentUser.getRole() == Role.CUSTOMER;
    }

    /**
     * Mengembalikan ID pengguna yang login.
     * Melempar IllegalStateException jika tidak ada session.
     * Gunakan isLoggedIn() sebelum memanggil ini.
     */
    public long getCurrentUserId() {
        if (currentUser == null) {
            throw new IllegalStateException("Tidak ada sesi aktif. Pengguna belum login.");
        }
        return currentUser.getId();
    }

    /**
     * Mengembalikan Role enum pengguna yang login (siap digunakan untuk BookingService).
     * Melempar IllegalStateException jika tidak ada session.
     */
    public Model.enums.Role getCurrentRole() {
        if (currentUser == null) {
            throw new IllegalStateException("Tidak ada sesi aktif. Pengguna belum login.");
        }
        return currentUser.getRole();
    }
}
