package org.kimrgrey.shareme;

import java.util.logging.Level;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String... args) {
        Application application = new Application(args);
        application.execute();
    }
    private CommandLine commandLine = null;

    public Application(String... args) {
        Options options = new Options();
        options.addOption("h", "help", false, "Display message that describes parameters for the application");
        options.addOption("c", "config", true, "Name of file that contains configuration for application");
        options.addOption("f", "folder", true, "Folder that should be scanned");
        options.addOption("t", "timeout", true, "Scanning timeout");
        options.addOption("i", "init", false, "Initialize database from scratch");
        options.addOption("ls", "list", false, "Allows to list all files in the personal cloud");
        options.addOption("rm", "remove", true, "Allows to remove files from the personal cloud");
        options.addOption("cm", "commit", true, "Allows to add files in the personal cloud");
        options.addOption("co", "checkout", true, "Allows to checkout files from the personal cloud");
        options.addOption("fr", "friends", true, "Allows to get friend list");

        CommandLineParser parser = new PosixParser();
        try {
            this.commandLine = parser.parse(options, args);
        } catch (ParseException exception) {
            logger.debug("Command line parameters are invalid", exception);
            printUsage(options);
            return;
        }
        if (commandLine.hasOption("help")) {
            printUsage(options);
            this.commandLine = null;
        }
    }

    private void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("shareme-daemon", options);
    }

    public void execute() {
        if (commandLine == null) {
            return;
        }
        Configuration configuration = null;
        if (commandLine.hasOption("config")) {
            String configFileName = commandLine.getOptionValue("config");
            logger.info("Try to read the configuration file {}", configFileName);
            try {
                configuration = Configuration.load(configFileName);
            } catch (InvalidConfigException exception) {
                logger.debug("Failed to read configuration from file {}, defaults will be used", configFileName, exception);
                logger.warn("Failed to read configuration from file {}, defaults will be used", configFileName);
            }
        }
        if (configuration == null) {
            logger.info("Default configuration was applied");
            configuration = new Configuration();
        }
        Configuration.setConfiguration(configuration);
        if (commandLine.hasOption("folder")) {
            configuration.setFolderName(commandLine.getOptionValue("folder"));
        }
        logger.info("Folder that should be scanned is {}", configuration.getFolderName());
        if (commandLine.hasOption("timeout")) {
            try {
                configuration.setScannerTimeout(Long.parseLong(commandLine.getOptionValue("timeout")));
            } catch (NumberFormatException exception) {
                logger.debug("Scanner timeout has invalid value {}, defaults will be used", commandLine.getOptionValue("timeout"), exception);
                logger.warn("Scanner timeout has invalid value {}, defaults will be used", commandLine.getOptionValue("timeout"));
            }
        }
        logger.info("Scanner timeout is {}", configuration.getScannerTimeout());
        if (commandLine.hasOption("init")) {
            try {
                DatabaseManager.initializeDatabase();
            } catch (InvalidConfigException exception) {
                logger.error(exception.getMessage());
            }
        }
        FolderScanner scaner = new FolderScanner(configuration.getFolderName());
        try {
            scaner.startScanning();
        } catch (InvalidConfigException ex) {
            
        }
    }
}
