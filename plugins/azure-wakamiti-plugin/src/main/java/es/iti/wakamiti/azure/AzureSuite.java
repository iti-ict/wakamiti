package es.iti.wakamiti.azure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AzureSuite {


    private final String id;
    private final String name;
    private final AzurePlan plan;
    private final AzureSuite parent;
    private final String idPath;


    public AzureSuite(String id, String name, AzurePlan plan, AzureSuite parent) {
        this.id = id;
        this.name = name;
        this.plan = plan;
        this.parent = parent;
        this.idPath = (parent == null ? "" : parent.idPath ) + "/" + id;
    }


    public AzureSuite(String id, String name, AzurePlan plan) {
        this(id,name,plan,null);
    }


    public AzureSuite(String name) {
        this(null,name,null,null);
    }



    public String name() {
        return name;
    }


    public String id() {
        return id;
    }


    public AzurePlan plan() {
        return plan;
    }


    public AzureSuite parent() {
        return parent;
    }


    public String idPath() {
        return idPath;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AzureSuite that = (AzureSuite) o;
        return Objects.equals(name, that.name) && Objects.equals(idPath, that.idPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, idPath);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s",id,name,idPath);
    }

}
