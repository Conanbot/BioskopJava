package Model.service;

import Controler.ConnectionProvider;
import Controler.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseHealthService {
    private final ConnectionProvider connectionProvider;

    public DatabaseHealthService() {
        this(DBConnection.getInstance()::getConnection);
    }

    public DatabaseHealthService(ConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider);
    }

    public void verifySchema() throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            verifyRequiredTables(connection);
            verifySeatUniquenessConstraint(connection);
        }
    }

    private void verifyRequiredTables(Connection connection) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS table_count
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name IN (
                      'users', 'films', 'studios', 'schedules',
                      'bookings', 'booking_seats', 'payments'
                  )
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (!result.next() || result.getInt("table_count") != 7) {
                throw new SQLException(
                        "Skema belum lengkap. Jalankan database/schema.sql pada MySQL.");
            }
        }
    }

    private void verifySeatUniquenessConstraint(Connection connection) throws SQLException {
        String sql = """
                SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') AS index_columns
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = 'booking_seats'
                  AND index_name = 'uq_booking_seats_schedule_seat'
                  AND non_unique = 0
                GROUP BY index_name
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (!result.next()
                    || !"schedule_id,seat_code".equals(result.getString("index_columns"))) {
                throw new SQLException(
                        "Aturan kursi ganda belum tersedia. Jalankan ulang database/schema.sql.");
            }
        }
    }
}
