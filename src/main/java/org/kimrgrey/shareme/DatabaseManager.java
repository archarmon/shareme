package org.kimrgrey.shareme;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private DatabaseManager() {
    }

    public static Connection getDatabaseConnection() throws InvalidConfigException {
        Configuration configuration = Configuration.getConfiguration();
        if (configuration == null) {
            logger.debug("Failed to lookup configuration, current instance is null");
            throw new InvalidConfigException("Failed to lookup configuration, current instance is null");
        }
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException exception) {
            logger.debug("Database driver class was not found, please check configuration file", exception);
            throw new InvalidConfigException("Database driver class was not found, please check configuration file");
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(configuration.getDatabaseUrl(), configuration.getDatabaseUser(), configuration.getDatabasePassword());
        } catch (SQLException exception) {
            logger.debug("Failed to open database connection", exception);
            throw new InvalidConfigException("Failed to open database connection");
        }
        return connection;
    }

    public static long getDatabaseRevision() throws InvalidConfigException {
        Connection connection = getDatabaseConnection();
        ResultSetHandler<Long> handler = new ResultSetHandler<Long>() {

            @Override
            public Long handle(ResultSet resultSet) throws SQLException {
                return resultSet.getLong("revision");
            }
        };
        QueryRunner runner = new QueryRunner();
        try {
            return runner.query(connection, "SELECT revision FROM tb_revision", handler);
        } catch (SQLException exception) {
            logger.debug("Failed to read database revision because of query exception", exception);
            throw new InvalidConfigException("Failed to read database revision because of query exception");
        } finally {
            try {
                DbUtils.close(connection);
            } catch (SQLException exception) {
                logger.warn("Failed to close database connection", exception);
            }
        }
    }

    public static boolean executeScript(File script) {
        Connection connection = null;
        try {
            connection = DatabaseManager.getDatabaseConnection();
        } catch (InvalidConfigException exception) {
            logger.debug("Failed to execute given script", exception);
            return false;
        }
        QueryRunner runner = new QueryRunner();
        try {
            Scanner scanner = new Scanner(script);
            scanner.useDelimiter(";");
            boolean result = true;
            while (scanner.hasNext()) {
                String statementText = scanner.next().trim().replaceAll("\\s+", " ");
                logger.debug("Execute statement {}", statementText);
                try {
                    runner.update(connection, statementText);
                } catch (SQLException exception) {
                    logger.debug("Failed to execute statement {} because of database exception", statementText, exception);
                    result = false;
                    break;
                }
            }
            if (result) {
                try {
                    connection.commit();
                } catch (SQLException exception) {
                    logger.warn("Failed to perform commit", exception);
                }
            } else {
                try {
                    connection.rollback();
                } catch (SQLException exception) {
                    logger.warn("Failed to perform rollback", exception);
                }
            }
            try {
                DbUtils.close(connection);
            } catch (SQLException exception) {
                logger.warn("Failed to close database connection", exception);
            }
            return result;
        } catch (FileNotFoundException exception) {
            logger.debug("Failed to execute given script", exception);
            return false;
        }
    }

    public static void initializeDatabase() throws InvalidConfigException {
        Configuration configuration = Configuration.getConfiguration();
        File dropScript = new File(configuration.getScriptFolder() + File.separatorChar + "drop.sql");
        if (!dropScript.exists() || !dropScript.isFile()) {
            logger.debug("Script that drops database schema could not be found in directory {}, please check configuration", configuration.getScriptFolder());
        } else {
            logger.info("Try to execute script that drops database schema");
            if (!DatabaseManager.executeScript(dropScript)) {
                logger.debug("Database schema could not be dropped, this step will be sckipped");
            }
        }
        File createScript = new File(configuration.getScriptFolder() + File.separatorChar + "create.sql");
        if (!createScript.exists() || !createScript.isFile()) {
            logger.debug("Script that creates database schema could not be found in directory {}, please check configuration", configuration.getScriptFolder());
            throw new InvalidConfigException("Script that creates database schema could not be found, please check configuration");
        }
        logger.info("Try to execute script that creates database schema");
        if (!DatabaseManager.executeScript(createScript)) {
            logger.debug("Script that creates database schema could not be read from directory {}, please check configuration", configuration.getScriptFolder());
            throw new InvalidConfigException("Script that creates database schema could not be read, please check configuration");
        }
        logger.info("Database schema was successfully initialized");
    }

    public static void checkDatabaseRevision() {
        
    }

    public static void updateFriendList() {
        
    }
}
