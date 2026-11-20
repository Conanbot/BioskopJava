package Model.service;

import Model.dao.UserDao;
import Model.entity.User;
import Model.enums.Role;
import Model.exception.ValidationException;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service untuk autentikasi pengguna: login dan registrasi.
 * Sesuai materi dasar OOP & database (password disimpan langsung/tanpa enkripsi tambahan).
 */
public class AuthService {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_USERNAME_LENGTH = 50;

    private final UserDao userDao;

    public AuthService() {
        this(new UserDao());
    }

    public AuthService(UserDao userDao) {
        if (userDao == null) {
            throw new IllegalArgumentException("UserDao wajib diisi.");
        }
        this.userDao = userDao;
    }

    /**
     * Melakukan verifikasi login.
     *
     * @param username username yang dimasukkan pengguna
     * @param rawPassword password yang dimasukkan pengguna (belum di-hash)
     * @return User yang terautentikasi
     * @throws ValidationException jika username atau password tidak valid
     * @throws AuthenticationException jika kredensial tidak cocok
     * @throws SQLException jika terjadi kesalahan database
     */
    public User login(String username, String rawPassword)
            throws ValidationException, AuthenticationException, SQLException {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username wajib diisi.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new ValidationException("Password wajib diisi.");
        }

        Optional<UserDao.UserCredential> credential =
                userDao.findCredentialByUsername(username.trim());

        if (credential.isEmpty()) {
            // Pesan umum: jangan ungkap apakah username atau password yang salah
            throw new AuthenticationException("Username atau password salah.");
        }

        UserDao.UserCredential cred = credential.get();
        // Cek kecocokan password secara langsung (tanpa hashing)
        boolean passwordMatches = rawPassword.equals(cred.password());

        if (!passwordMatches) {
            throw new AuthenticationException("Username atau password salah.");
        }

        return new User(cred.id(), cred.username(), cred.role(), java.time.LocalDateTime.now());
    }

    /**
     * Mendaftarkan pengguna baru sebagai CUSTOMER.
     *
     * @param username username yang diinginkan
     * @param rawPassword password yang diinginkan
     * @param confirmPassword konfirmasi password
     * @return User baru yang sudah disimpan
     * @throws ValidationException jika data tidak valid
     * @throws SQLException jika terjadi kesalahan database atau username sudah ada
     */
    public User register(String username, String rawPassword, String confirmPassword)
            throws ValidationException, SQLException {
        validateRegistrationInput(username, rawPassword, confirmPassword);

        String trimmedUsername = username.trim();
        if (userDao.usernameExists(trimmedUsername)) {
            throw new ValidationException("Username '" + trimmedUsername + "' sudah digunakan. Pilih username lain.");
        }

        return userDao.insertCustomer(trimmedUsername, rawPassword);
    }

    /**
     * Menghasilkan atau mengembalikan string password.
     */
    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password tidak boleh kosong.");
        }
        return rawPassword;
    }

    private void validateRegistrationInput(String username, String rawPassword, String confirmPassword)
            throws ValidationException {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username wajib diisi.");
        }
        if (username.trim().length() > MAX_USERNAME_LENGTH) {
            throw new ValidationException("Username maksimal " + MAX_USERNAME_LENGTH + " karakter.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new ValidationException("Password wajib diisi.");
        }
        if (rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD_LENGTH + " karakter.");
        }
        if (!rawPassword.equals(confirmPassword)) {
            throw new ValidationException("Konfirmasi password tidak sesuai.");
        }
    }

    /**
     * Exception khusus untuk kegagalan autentikasi (kredensial salah).
     * Terpisah dari ValidationException agar View dapat menampilkan pesan yang tepat.
     */
    public static final class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
