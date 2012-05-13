package org.kimrgrey.shareme;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FolderScanner {

    private List<FileSpec> knownFiles = new ArrayList<>();
    private String folderName = null;
    private Timer timer = null;
    private boolean status = false;
    private final long PERIOD = 5000;
    private Connection connection;

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
                for (int i = 0; i < files.length; ++i) {
                    if (knownFiles.contains(new FileSpec(files[i],new File(files[i]).getTotalSpace()))) {
                        System.out.println("have a file - " + files[i]);
                    } else {
                        knownFiles.add(new FileSpec(files[i],new File(files[i]).getTotalSpace()));
                        System.out.println("add file - " + files[i]);
                    }
                    
                }
            }
        };
        timer.schedule(task, 0, PERIOD);
    }

    public void stopScanning() {
        status = false;
        timer.cancel();
    }

    public boolean isScanning() {
        return status;
    }
}
