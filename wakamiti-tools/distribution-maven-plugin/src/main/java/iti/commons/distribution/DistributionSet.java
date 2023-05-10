/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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