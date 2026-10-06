package com.hotelmanagement.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reusable Database Connection manager handling JDBC connectivity,
 * connection properties loading, and resource management.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final Properties properties = new Properties();
    private static DatabaseConnection instance;

    static {
        loadProperties();
    }

    private DatabaseConnection() {
        try {
            String driver = properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found in classpath!", e);
            throw new RuntimeException("MySQL JDBC Driver missing. Please verify Maven dependencies.", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private static void loadProperties() {
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                properties.load(is);
                LOGGER.info("Successfully loaded database configuration from application.properties.");
            } else {
                LOGGER.warning("application.properties not found on classpath, utilizing default fallback parameters.");
                setDefaultProperties();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read application.properties, using defaults.", e);
            setDefaultProperties();
        }
    }

    private static void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/hotel_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "rootpassword");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Obtains an active JDBC Connection to the MySQL database.
     * Callers must close the Connection using try-with-resources.
     *
     * @return Connection active database connection
     * @throws SQLException when connection fails
     */
    public Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.username");
        String pass = properties.getProperty("db.password");
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Tests the database connection on startup.
     *
     * @return boolean true if connected, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
