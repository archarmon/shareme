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
    private List<ScanerListener> listeners;
    private String folderName = null;
    private Timer timer = null;
    private boolean status = false;
    private final long PERIOD = 5000;
    private Connection connection;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FolderScanner.class);

    public FolderScanner(String folderName) {
        this.folderName = folderName;
        status = false;
        listeners = new ArrayList<ScanerListener>();
        try {
            connection = DatabaseManager.getDatabaseConnection();
        } catch (InvalidConfigException ex) {
            Logger.getLogger(FolderScanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        timer = new Timer();
    }

    public void addListener(ScanerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ScanerListener listener) {
        listeners.remove(listener);
    }

    private void onChange(FileSpec file) {
        System.out.println("on change - " + file.getFileName());
        for (int i = 0; i < listeners.size(); ++i) {
            listeners.get(i).onChange(file);
        }
    }

    private void onDelete(FileSpec file) {
        System.out.println("on delete - " + file.getFileName());
        for (int i = 0; i < listeners.size(); ++i) {
            listeners.get(i).onDelete(file);
        }
    }

    private void onNew(FileSpec file) {
        System.out.println("on new - " + file.getFileName());
        for (int i = 0; i < listeners.size(); ++i) {
            listeners.get(i).onNew(file);
        }
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
                File[] files = folder.listFiles();
                
                loadKnownFiles();
                if (knownFiles == null) {
                    knownFiles = new ArrayList<FileSpec>();
                }
                for (int i = 0; i < files.length; ++i) {
                    FileSpec file = new FileSpec(files[i].getName(), files[i].length());
                    if (knownFiles.contains(file)) {
                        System.out.println("have a file " + files[i] + " " + (files[i]).length());
                        knownFiles.remove(file);
                    } else {
                        for (int j = 0; j < knownFiles.size(); ++j) {
                            // if true then size has been change
                            if (knownFiles.get(j).getFileName().equals(files[i])) {
                                System.out.println("on update - " + file.getFileName() + files[i]);
                                updateFile(file);
                                onChange(file);
                            } else {
                                System.out.println("on update - " + file.getFileName() + files[i]);
                            }
                        }
                        insertNewFile(file);
                        onNew(file);
                    }
                }
                if (!knownFiles.isEmpty()) {
                    for (int i = 0; i < knownFiles.size(); ++i) {
                        deleteFile(knownFiles.get(i));
                        onDelete(knownFiles.get(i));
                    }
                }
            }

            private void deleteFile(FileSpec file) {
                QueryRunner runner = new QueryRunner();
                try {
                    runner.update(connection, "DELETE FROM tb_known_files where filename = ?",
                            file.getFileName());
                } catch (SQLException ex) {
                    logger.warn("Failed to perform update", ex);
                }
            }

            private void updateFile(FileSpec file) {
                QueryRunner runner = new QueryRunner();
                try {
                    runner.update(connection, "UPDATE tb_known_files set file_size = ? where filename = ?",
                            file.getFileSize(), file.getFileName());
                } catch (SQLException ex) {
                    logger.warn("Failed to perform update", ex);
                }
            }

            private void insertNewFile(FileSpec file) {
                QueryRunner runner = new QueryRunner();
                try {
                    runner.update(connection, "INSERT INTO tb_known_files(filename, file_size) VALUES(?,?)",
                            file.getFileName(), file.getFileSize());
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
