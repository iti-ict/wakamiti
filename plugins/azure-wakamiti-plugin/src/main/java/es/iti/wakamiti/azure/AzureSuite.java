package es.iti.wakamiti.azure;

import java.util.Objects;

public class AzureSuite {


    private final String id;
    private final String name;
    private final AzurePlan plan;


    public AzureSuite(String id, String name, AzurePlan plan) {
        this.id = id;
        this.name = name;
        this.plan = plan;
    }



    public AzureSuite(String name) {
        this(null,name,null);
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



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AzureSuite that = (AzureSuite) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


}
