package iti.kukumo.api;

/**
 * @author ITI
 *         Created by ITI on 9/01/19
 */
public class Resource<T> {

    private final String absolutePath;
    private final String relativePath;
    private final T content;

    public Resource(String absolutePath, String relativePath, T content) {
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
        this.content = content;
    }


    public String relativePath() {
        return relativePath;
    }

    public String absolutePath() {
        return absolutePath;
    }

    public T content() { return content; }
}
