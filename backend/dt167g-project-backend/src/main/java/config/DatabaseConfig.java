package config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConfig {
    private static final String DB_PATH = "database/procodersDB.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        Path dbFilePath = Paths.get(DB_PATH);
        if (Files.notExists(dbFilePath)) {
            logger.info("Database file not found, creating new database.");
            try {
                Files.createDirectories(dbFilePath.getParent());
                Files.createFile(dbFilePath);
                createSchema();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create database file", e);
                throw new RuntimeException("Failed to create database file", e);
            }
        } else {
            logger.info("Database file found.");
        }
    }

    private static void createSchema() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             InputStream is = DatabaseConfig.class.getResourceAsStream("/schema.sql");
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }
            String sql = sqlBuilder.toString();
            for (String statement : sql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    logger.log(Level.INFO, () -> "Executing SQL: " + statement);
                    stmt.execute(statement);
                }
            }
            logger.info("Database schema created successfully.");
        } catch (SQLException | IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
