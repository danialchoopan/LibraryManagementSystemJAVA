package com.library.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static HikariDataSource dataSource;
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.error("Unable to find database.properties");
                throw new RuntimeException("Unable to find database.properties");
            }
            properties.load(input);
            initializeDataSource();
        } catch (IOException e) {
            logger.error("Failed to load database properties", e);
            throw new RuntimeException("Failed to load database properties", e);
        }
    }

    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("db.url"));
        config.setUsername(properties.getProperty("db.username"));
        config.setPassword(properties.getProperty("db.password"));
        config.setDriverClassName(properties.getProperty("db.driver"));

        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("db.pool.minimumIdle", "5")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("db.pool.idleTimeout", "300000")));
        config.setMaxLifetime(Long.parseLong(properties.getProperty("db.pool.maxLifetime", "1800000")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("db.pool.connectionTimeout", "30000")));

        config.setPoolName("LibraryDBPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        logger.info("HikariCP connection pool initialized successfully");
        initializeSchema();
    }

    private static void initializeSchema() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             InputStream schemaStream = DatabaseConnection.class.getClassLoader().getResourceAsStream("db/h2/schema.sql")) {
            if (schemaStream == null) {
                logger.warn("Schema file not found: db/h2/schema.sql");
                return;
            }
            String sql = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
            StringBuilder current = new StringBuilder();
            for (String line : sql.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--")) continue;
                current.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    String stmtSql = current.toString().replace(";", "").trim();
                    if (!stmtSql.isEmpty()) {
                        stmt.execute(stmtSql);
                    }
                    current.setLength(0);
                }
            }
            logger.info("Database schema initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing database schema", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP connection pool closed");
        }
    }
}