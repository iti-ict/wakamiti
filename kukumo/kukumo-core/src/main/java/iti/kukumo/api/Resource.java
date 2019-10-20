/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;



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


    public T content() {
        return content;
    }
}
