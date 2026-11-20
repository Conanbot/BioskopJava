package Model;

import Model.dao.UserDao;
import Model.exception.ValidationException;
import Model.service.AdminProvisioningService;
import Model.service.AdminProvisioningService.ProvisionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Mekanisme Provisioning Akun Admin")
class AdminProvisioningTest {

    private AdminProvisioningService provisioningService;
    private FakeAdminUserDao fakeUserDao;

    @BeforeEach
    void setUp() {
        fakeUserDao = new FakeAdminUserDao();
        provisioningService = new AdminProvisioningService(fakeUserDao);
    }

    @Test
    @DisplayName("Admin baru dibuat dengan password tersimpan")
    void testProvisionAdminSuccess() throws Exception {
        ProvisionStatus status = provisioningService.provisionAdmin("admin", "AdminSecret123");

        assertEquals(ProvisionStatus.SUCCESS, status);
        assertEquals("admin", fakeUserDao.savedUsername);
        assertEquals("AdminSecret123", fakeUserDao.savedPasswordHash);
    }

    @Test
    @DisplayName("Password admin yang tersimpan dapat diverifikasi")
    void testPasswordVerifiable() throws Exception {
        provisioningService.provisionAdmin("admin", "SuperSecurePassword99");
        assertEquals("SuperSecurePassword99", fakeUserDao.savedPasswordHash);
    }

    @Test
    @DisplayName("Proses provisioning berulang bersifat idempoten dan tidak membuat duplikasi")
    void testRepeatedProvisioningDoesNotDuplicate() throws Exception {
        // Eksekusi pertama: berhasil membuat akun
        ProvisionStatus firstRun = provisioningService.provisionAdmin("admin", "AdminPass123");
        assertEquals(ProvisionStatus.SUCCESS, firstRun);

        // Simulasi bahwa user sekarang sudah tercatat di database
        fakeUserDao.userExists = true;

        // Eksekusi kedua: aman, mendeteksi akun sudah ada tanpa melempar duplikasi
        ProvisionStatus secondRun = provisioningService.provisionAdmin("admin", "AdminPass123");
        assertEquals(ProvisionStatus.ALREADY_EXISTS, secondRun);
    }

    @Test
    @DisplayName("Provisioning menolak username kosong")
    void testProvisioningEmptyUsernameThrows() {
        assertThrows(ValidationException.class, () ->
                provisioningService.provisionAdmin("", "ValidPassword123"));
        assertThrows(ValidationException.class, () ->
                provisioningService.provisionAdmin(null, "ValidPassword123"));
    }

    @Test
    @DisplayName("Provisioning menolak password kurang dari 6 karakter")
    void testProvisioningShortPasswordThrows() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                provisioningService.provisionAdmin("admin", "12345"));
        assertTrue(ex.getMessage().contains("minimal 6 karakter"));
    }

    // Fake UserDao untuk isolasi unit test
    private static class FakeAdminUserDao extends UserDao {
        boolean userExists = false;
        String savedUsername = null;
        String savedPasswordHash = null;

        @Override
        public boolean usernameExists(String username) {
            return userExists;
        }

        @Override
        public boolean provisionAdmin(String username, String passwordHash) {
            if (userExists) {
                return false;
            }
            this.savedUsername = username;
            this.savedPasswordHash = passwordHash;
            this.userExists = true;
            return true;
        }
    }
}
