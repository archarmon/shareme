package org.kimrgrey.shareme;

public class FileSpec {

    private String fileName = null;
    private double fileSize = 0.0;

    public FileSpec() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }
}
