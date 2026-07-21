package edu.utem.ftmk.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnection {

    private final DatabaseConfig config;

    @Autowired
    public DatabaseConnection(DatabaseConfig config) {
        this.config = config;
        try {
            Class.forName(config.getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found: " + config.getDriverClassName(), e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getUrl(),
                config.getUsername(),
                config.getPassword()
        );
    }

    public void addDurationColumnIfNeeded() {
        String query = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'nutritional_llm' " +
                "AND TABLE_NAME = 'experiment' " +
                "AND COLUMN_NAME = 'duration_ms'";

        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("ALTER TABLE experiment ADD COLUMN duration_ms INT NULL DEFAULT NULL");
            }
        } catch (SQLException e) {
            try (Connection conn = getConnection();
                 java.sql.Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE experiment ADD COLUMN duration_ms INT NULL DEFAULT NULL");
            } catch (SQLException ex) {
                if (ex.getMessage() == null || !ex.getMessage().contains("Duplicate column")) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void updateStatusConstraintIfNeeded() {
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            try {
                stmt.executeUpdate("ALTER TABLE experiment DROP CONSTRAINT chk_experiment_status");
            } catch (SQLException e) {
            }

            try {
                stmt.executeUpdate(
                        "ALTER TABLE experiment ADD CONSTRAINT chk_experiment_status " +
                        "CHECK (status in ('pending','running','completed','failed','cancelled'))"
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}