package org.kimrgrey.shareme;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.LoggerFactory;

public class FolderScanner {

    private List<FileSpec> knownFiles = new ArrayList<>();
    private String folderName = null;
    private Timer timer = null;
    private boolean status = false;
    private final long PERIOD = 5000;
    private Connection connection;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FolderScanner.class);

    public FolderScanner(String folderName) {
        this.folderName = folderName;
        status = false;
        try {
            connection = DatabaseManager.getDatabaseConnection();
        } catch (InvalidConfigException ex) {
            Logger.getLogger(FolderScanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        timer = new Timer();
    }

    public List<FileSpec> getKnownFiles() {
        return knownFiles;
    }

    public List<FileSpec> getNewFiles() {
        ArrayList<FileSpec> files = new ArrayList<>();

        return files;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void startScanning() throws InvalidConfigException {
        status = true;
        File folder = new File(folderName);
        if (!folder.isDirectory()) {
            status = false;
            throw new InvalidConfigException("Folder " + folderName + " does not exist. Please, check your configuration.");
        }
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                File folder = new File(folderName);
                String[] files = folder.list();
                loadKnownFiles();
                if (knownFiles == null) {
                    knownFiles = new ArrayList<FileSpec>();
                }
                for (int i = 0; i < files.length; ++i) {
                    if (knownFiles.contains(new FileSpec(files[i], new File(files[i]).getTotalSpace()))) {
                        System.out.println("have a file - " + files[i]);
                    } else {
                        insertNewFile(new FileSpec(files[i], new File(files[i]).getTotalSpace()));
                        System.out.println("add file - " + files[i]);
                    }

                }
            }

            private void insertNewFile(FileSpec file) {
                QueryRunner runner = new QueryRunner();
                try {
                    runner.update(connection, "INSERT INTO tb_known_files(filename, file_size) VALUES('"
                            + file.getFileName() + "', '" + file.getFileSize() + "')");
                } catch (SQLException ex) {
                    logger.warn("Failed to perform insert", ex);
                }
            }

            private void loadKnownFiles() {
                ResultSetHandler<List<FileSpec>> handler = new ResultSetHandler<List<FileSpec>>() {

                    @Override
                    public List<FileSpec> handle(ResultSet resultSet) throws SQLException {
                        if (!resultSet.first()) {
                            return null;
                        }
                        ArrayList<FileSpec> result = new ArrayList<FileSpec>();
                        do {
                            result.add(new FileSpec(resultSet.getString("filename"),
                                    resultSet.getLong("file_size")));
                        } while (resultSet.next());
                        return result;
                    }
                };
                QueryRunner runner = new QueryRunner();
                try {
                    knownFiles.clear();
                    knownFiles = runner.query(connection, "SELECT filename, file_size FROM tb_known_files", handler);
                } catch (SQLException exception) {
                    logger.debug("Failed to read database revision because of query exception", exception);
                }
            }
        };
        timer.schedule(task, 0, PERIOD);
    }

    public void stopScanning() {
        status = false;
        timer.cancel();
        try {
            DbUtils.close(connection);
        } catch (SQLException exception) {
            logger.warn("Failed to close database connection", exception);
        }
    }

    public boolean isScanning() {
        return status;
    }
}
