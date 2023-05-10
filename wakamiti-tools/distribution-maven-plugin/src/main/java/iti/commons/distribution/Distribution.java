/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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