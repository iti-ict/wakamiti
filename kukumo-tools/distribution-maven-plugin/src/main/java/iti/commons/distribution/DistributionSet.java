package iti.commons.distribution;

import java.util.List;

public class DistributionSet {

    private String applicationName;
    private List<PlatformDistribution> distributions;


    public void setDistributions(List<PlatformDistribution> distributions) {
        this.distributions = distributions;
    }

    public List<PlatformDistribution> getDistributions() {
        return distributions;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }


}
