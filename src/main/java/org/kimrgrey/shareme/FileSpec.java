package org.kimrgrey.shareme;

import java.util.Objects;

public class FileSpec {

    private String fileName = null;
    private long fileSize = 0;

    public FileSpec() {
    }
    
    public FileSpec(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileSpec other = (FileSpec) obj;
        if (!this.fileName.equals(other.fileName)) {
            return false;
        }
        if (this.fileSize != other.fileSize) {
            System.out.println("false " + this.fileSize + other.fileSize);
            return false;
            
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.fileName);
        hash = 11 * hash + (int) (this.fileSize ^ (this.fileSize >>> 32));
        return hash;
    }
}
