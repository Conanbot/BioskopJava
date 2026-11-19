package Model.service;

import Model.dao.UserDao;
import Model.entity.User;
import Model.enums.Role;
import Model.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Logika AuthService")
class AuthServiceTest {

    private AuthService authService;
    private FakeUserDao fakeUserDao;

    @BeforeEach
    void setUp() {
        fakeUserDao = new FakeUserDao();
        authService = new AuthService(fakeUserDao);
    }

    @Test
    @DisplayName("Registrasi dengan data valid berhasil dan password ter-hash")
    void testRegisterValidSuccess() throws Exception {
        User user = authService.register("newuser", "password123", "password123");

        assertNotNull(user);
        assertEquals("newuser", user.getUsername());
        assertEquals(Role.CUSTOMER, user.getRole());
        assertNotNull(fakeUserDao.savedHash);
        assertNotEquals("password123", fakeUserDao.savedHash);
        assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw("password123", fakeUserDao.savedHash));
    }

    @Test
    @DisplayName("Registrasi gagal jika password kurang dari 6 karakter")
    void testRegisterPasswordTooShort() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("newuser", "12345", "12345"));
        assertTrue(ex.getMessage().contains("minimal 6 karakter"));
    }

    @Test
    @DisplayName("Registrasi gagal jika konfirmasi password tidak sama")
    void testRegisterPasswordMismatch() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("newuser", "password123", "different123"));
        assertTrue(ex.getMessage().contains("tidak sesuai"));
    }

    @Test
    @DisplayName("Registrasi gagal jika username kosong")
    void testRegisterEmptyUsername() {
        assertThrows(ValidationException.class, () ->
                authService.register("", "password123", "password123"));
    }

    @Test
    @DisplayName("Registrasi gagal jika username sudah terdaftar")
    void testRegisterDuplicateUsername() {
        fakeUserDao.userExists = true;
        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("existing", "password123", "password123"));
        assertTrue(ex.getMessage().contains("sudah digunakan"));
    }

    @Test
    @DisplayName("Login berhasil dengan kredensial cocok")
    void testLoginSuccess() throws Exception {
        String hash = AuthService.hashPassword("secret123");
        fakeUserDao.foundCredential = new UserDao.UserCredential(1L, "admin", hash, Role.ADMIN);

        User loggedIn = authService.login("admin", "secret123");
        assertNotNull(loggedIn);
        assertEquals("admin", loggedIn.getUsername());
        assertEquals(Role.ADMIN, loggedIn.getRole());
    }

    @Test
    @DisplayName("Login gagal jika akun tidak ditemukan")
    void testLoginUserNotFound() {
        fakeUserDao.foundCredential = null;
        assertThrows(AuthService.AuthenticationException.class, () ->
                authService.login("unknown", "secret123"));
    }

    @Test
    @DisplayName("Login gagal jika password salah")
    void testLoginWrongPassword() {
        String hash = AuthService.hashPassword("correctPassword");
        fakeUserDao.foundCredential = new UserDao.UserCredential(2L, "user2", hash, Role.CUSTOMER);

        assertThrows(AuthService.AuthenticationException.class, () ->
                authService.login("user2", "wrongPassword"));
    }

    @Test
    @DisplayName("Login validasi input kosong")
    void testLoginEmptyFields() {
        assertThrows(ValidationException.class, () -> authService.login("", "secret"));
        assertThrows(ValidationException.class, () -> authService.login("user", ""));
    }

    // Fake DAO untuk isolasi unit test tanpa dependensi ke MySQL
    private static class FakeUserDao extends UserDao {
        boolean userExists = false;
        String savedHash = null;
        UserCredential foundCredential = null;

        @Override
        public boolean usernameExists(String username) {
            return userExists;
        }

        @Override
        public User insertCustomer(String username, String passwordHash) {
            this.savedHash = passwordHash;
            return new User(100L, username, Role.CUSTOMER, java.time.LocalDateTime.now());
        }

        @Override
        public Optional<UserCredential> findCredentialByUsername(String username) {
            return Optional.ofNullable(foundCredential);
        }
    }
}
