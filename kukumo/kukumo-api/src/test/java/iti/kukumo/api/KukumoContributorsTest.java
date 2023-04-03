/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.api;

import iti.kukumo.api.extensions.*;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KukumoContributorsTest {

    @Test
    public void testAllContributors() {
        KukumoContributors kukumoContributors = new KukumoContributors();
        kukumoContributors.setClassLoaders(getClass().getClassLoader());
        Map<Class<?>, List<Contributor>> allContributors = kukumoContributors.allContributors();
        assertNotNull(allContributors);
        assertTrue(allContributors.containsKey(ConfigContributor.class));
        assertTrue(allContributors.containsKey(DataTypeContributor.class));
        assertTrue(allContributors.containsKey(EventObserver.class));
        assertTrue(allContributors.containsKey(PlanBuilder.class));
        assertTrue(allContributors.containsKey(PlanTransformer.class));
        assertTrue(allContributors.containsKey(Reporter.class));
        assertTrue(allContributors.containsKey(ResourceType.class));
        assertTrue(allContributors.containsKey(StepContributor.class));
    }



}
