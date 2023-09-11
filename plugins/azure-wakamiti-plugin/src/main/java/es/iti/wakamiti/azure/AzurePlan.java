package es.iti.wakamiti.azure;

import java.util.Objects;

public class AzurePlan {

    private final String id;

    private final String name;
    private final String area;
    private final String iteration;

    private final String rootSuiteID;


    public AzurePlan(String id, String name, String area, String iteration, String rootSuiteID) {
        this.id = id;
        this.name = name;
        this.area = area;
        this.iteration = iteration;
        this.rootSuiteID = rootSuiteID;
    }


    public AzurePlan(String name, String area, String iteration) {
        this.id = null;
        this.name = name;
        this.area = area;
        this.iteration = iteration;
        this.rootSuiteID = null;
    }



    public String id() {
        return id;
    }


    public String area() {
        return area;
    }


    public String name() {
        return name;
    }


    public String iteration() {
        return iteration;
    }


    public String rootSuiteID() {
        return rootSuiteID;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AzurePlan azurePlan = (AzurePlan) o;
        return name.equals(azurePlan.name) && Objects.equals(area, azurePlan.area) && Objects.equals(iteration, azurePlan.iteration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, area, iteration);
    }

}
