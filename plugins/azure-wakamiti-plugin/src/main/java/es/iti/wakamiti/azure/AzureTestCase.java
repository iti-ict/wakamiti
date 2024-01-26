package es.iti.wakamiti.azure;

import java.util.Objects;

public class AzureTestCase {

    private final String id;
    private final String name;


    public AzureTestCase(String id, String name) {
        this.id = id;
        this.name = name;
    }


    public String id() {
        return id;
    }


    public String name() {
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AzureTestCase that = (AzureTestCase) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }


    @Override
    public String toString() {
        return "["+id+"] "+name;
    }
}
