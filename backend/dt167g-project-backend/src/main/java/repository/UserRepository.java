package repository;


import config.DatabaseConfig;
import model.User;
import util.LoggerUtility;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

// This class is responsible for interacting with the database and performing CRUD operations on the user
public class UserRepository {

    // Create a logger object
    private static final Logger logger = LoggerUtility.getLogger();

    // Create the user table
    public void createUserTable() {
        try (Connection connection = DatabaseConfig.getConnection()) {
            // Load the schema.sql file from the resources folder
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema.sql");
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: schema.sql");
            }
            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
                logger.info("User table created successfully.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating user table", e);
        }
    }

    // Save the user to the database
    public boolean saveUser(User user) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                     "INSERT INTO users (firstName, lastName, username, password) VALUES (?, ?, ?, ?)"
             )) {
            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getPassword());
            pstmt.executeUpdate();
            logger.info("User saved successfully: " + user.getUsername());
            return true;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error saving user: " + user.getUsername(), e);
            return false;
        }
    }

    // Retrieve the user from the database by username
    public User findUserByUsername(String username) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT * FROM users WHERE username = ?"
             )) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("username"),
                        rs.getString("password")
                );
                logger.info("User found: " + username);
                return user;
            } else {
                logger.warning("User not found: " + username);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding user: " + username, e);
        }
        return null;
    }
}

