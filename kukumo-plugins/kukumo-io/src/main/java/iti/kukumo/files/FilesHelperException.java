package iti.kukumo.files;

import java.io.IOException;

public class FilesHelperException extends RuntimeException {

    public FilesHelperException(IOException e) {
        super(e);
    }
}
