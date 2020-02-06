package iti.commons.distribution;

import java.util.List;

public class FileSet {

    private String destinationFolder;
    private boolean clean;
    private Access access;
    private List<String> files;



    public void setDestinationFolder(String destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public Access getAccess() {
        return access == null ? new Access() : access;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public boolean isClean() {
        return clean;
    }


}
