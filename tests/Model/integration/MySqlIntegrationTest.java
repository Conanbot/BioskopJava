package Model.integration;

import Controler.DBConnection;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Integration Test dengan MySQL Database")
class MySqlIntegrationTest {

    private static boolean dbAvailable = false;

    @BeforeAll
    static void checkDatabaseAvailability() {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            dbAvailable = (conn != null && !conn.isClosed());
        } catch (Exception ex) {
            dbAvailable = false;
        }
    }

    @Test
    @DisplayName("Pemeriksaan koneksi dan ketersediaan tabel inti MySQL")
    void testLiveDatabaseConnectionAndCoreTables() throws Exception {
        Assumptions.assumeTrue(dbAvailable,
                "MySQL database tidak berjalan pada localhost:3306 atau kredensial belum dikonfigurasi. Pengujian integrasi dilewati.");

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
            assertTrue(rs.next(), "Tabel database harus tersedia jika MySQL terhubung.");
        }
    }

    @Test
    @DisplayName("Pemeriksaan validasi skema lengkap melalui DatabaseHealthService")
    void testDatabaseHealthServiceVerifySchema() throws Exception {
        Assumptions.assumeTrue(dbAvailable, "MySQL database tidak tersedia. Pengujian integrasi dilewati.");

        Model.service.DatabaseHealthService healthService = new Model.service.DatabaseHealthService();
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> healthService.verifySchema(),
                "Skema database dan constraint keunikan kursi harus lolos validasi.");
    }
}
