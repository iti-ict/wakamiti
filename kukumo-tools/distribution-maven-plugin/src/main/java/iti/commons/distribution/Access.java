package iti.commons.distribution;

public class Access {

    public static final String ANY = "any";
    public static final String USER = "user";
    public static final String GROUP = "group";
    public static final String ROOT = "root";

    private String readableBy = ANY;
    private String writableBy = ANY;
    private String executableBy = ANY;


    public void setExecutableBy(String executableBy) {
        this.executableBy = executableBy;
    }

    public void setReadableBy(String readableBy) {
        this.readableBy = readableBy;
    }

    public void setWritableBy(String writableBy) {
        this.writableBy = writableBy;
    }

    public String getExecutableBy() {
        return executableBy == null ? "" : executableBy;
    }

    public String getReadableBy() {
        return readableBy == null ? ANY : readableBy;
    }

    public String getWritableBy() {
        return writableBy == null ? "" : writableBy;
    }


}
