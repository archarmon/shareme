package org.kimrgrey.shareme;

import java.util.ArrayList;
import java.util.List;

public class FolderScanner {

    private List<FileSpec> knownFiles = new ArrayList<>();
    private String folderName = null;

    public FolderScanner(String folderName) {
        this.folderName = folderName;
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
}
