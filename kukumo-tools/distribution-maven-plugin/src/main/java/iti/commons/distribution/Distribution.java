package iti.commons.distribution;

import java.util.List;

public class Distribution {

    private List<PlatformDistribution> platform;

    public void setPlatform(List<PlatformDistribution> platform) {
        this.platform = platform;
    }

    public List<PlatformDistribution> getPlatform() {
        return platform;
    }
}
