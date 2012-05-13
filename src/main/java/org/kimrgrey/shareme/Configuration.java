package org.kimrgrey.shareme;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    private static final int DEFAULT_SCANNER_TIMEOUT = 5000;
    private static Configuration configuration = null;

    public static void setConfiguration(Configuration configuration) {
        Configuration.configuration = configuration;
    }

    public static Configuration getConfiguration() {
        return Configuration.configuration;
    }

    public static Configuration load(String configFileName) throws InvalidConfigException {
        Configuration config = null;
        try {
            config = new Gson().fromJson(new InputStreamReader(new FileInputStream(configFileName)), Configuration.class);
        } catch (FileNotFoundException exception) {
            logger.debug("Failed to read configuration because file " + configFileName + " not found", exception);
            throw new InvalidConfigException("Failed to read configuration because file " + configFileName + " not found");
        } catch (SecurityException exception) {
            logger.debug("Failed to read configuration because access to file " + configFileName + " was denied", exception);
            throw new InvalidConfigException("Failed to read configuration because access to file " + configFileName + " was denied");
        } catch (JsonIOException exception) {
            logger.debug("Failed to read configuration from file " + configFileName + " because of I/O error", exception);
            throw new InvalidConfigException("Failed to read configuration from file " + configFileName + " because of I/O error");
        } catch (JsonSyntaxException exception) {
            logger.debug("Failed to read configuration from file " + configFileName + " because of syntax error", exception);
            throw new InvalidConfigException("Failed to read configuration from file " + configFileName + " because of syntax error");
        }
        return config;
    }
    private String scriptFolder = System.getProperty("user.dir");
    private String folderName = System.getProperty("user.home");
    private long scannerTimeout = DEFAULT_SCANNER_TIMEOUT;
    private String databaseUrl = null;
    private String databaseUser = null;
    private String databasePassword = null;
    private String serviceUrl = null;
    private String serviceUser = null;

    public Configuration() {
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public long getScannerTimeout() {
        return scannerTimeout;
    }

    public void setScannerTimeout(long scannerTimeout) {
        this.scannerTimeout = scannerTimeout;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getServiceUser() {
        return serviceUser;
    }

    public void setServiceUser(String serviceUser) {
        this.serviceUser = serviceUser;
    }

    public String getScriptFolder() {
        return scriptFolder;
    }

    public void setScriptFolder(String scriptFolder) {
        this.scriptFolder = scriptFolder;
    }
}
