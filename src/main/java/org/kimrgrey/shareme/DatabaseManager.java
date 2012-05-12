package org.kimrgrey.shareme;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    
    public static void checkDatabaseRevision() {
        
    }
    
    public static void updateFriendList() {
        
    }
}
