package Controler;

import Model.entity.User;
import Model.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian SessionManager")
class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }

    @AfterEach
    void tearDown() {
        sessionManager.clearSession();
    }

    @Test
    @DisplayName("Awal inisialisasi belum login")
    void testInitialStateNotLoggedIn() {
        assertFalse(sessionManager.isLoggedIn());
        assertFalse(sessionManager.isAdmin());
        assertFalse(sessionManager.isCustomer());
        assertNull(sessionManager.getCurrentUser());
        assertThrows(IllegalStateException.class, () -> sessionManager.getCurrentUserId());
        assertThrows(IllegalStateException.class, () -> sessionManager.getCurrentRole());
    }

    @Test
    @DisplayName("Login sebagai Customer mengaktifkan session dengan role CUSTOMER")
    void testCustomerSession() {
        User customer = new User(10L, "customer1", Role.CUSTOMER, LocalDateTime.now());
        sessionManager.createSession(customer);

        assertTrue(sessionManager.isLoggedIn());
        assertTrue(sessionManager.isCustomer());
        assertFalse(sessionManager.isAdmin());
        assertEquals(10L, sessionManager.getCurrentUserId());
        assertEquals(Role.CUSTOMER, sessionManager.getCurrentRole());
        assertEquals(customer, sessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("Login sebagai Admin mengaktifkan session dengan role ADMIN")
    void testAdminSession() {
        User admin = new User(1L, "admin", Role.ADMIN, LocalDateTime.now());
        sessionManager.createSession(admin);

        assertTrue(sessionManager.isLoggedIn());
        assertTrue(sessionManager.isAdmin());
        assertFalse(sessionManager.isCustomer());
        assertEquals(1L, sessionManager.getCurrentUserId());
        assertEquals(Role.ADMIN, sessionManager.getCurrentRole());
    }

    @Test
    @DisplayName("Logout membersihkan seluruh data sesi")
    void testClearSession() {
        User user = new User(5L, "user5", Role.CUSTOMER, LocalDateTime.now());
        sessionManager.createSession(user);
        assertTrue(sessionManager.isLoggedIn());

        sessionManager.clearSession();
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("Membuat session dengan null melempar IllegalArgumentException")
    void testCreateSessionWithNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.createSession(null));
    }
}
