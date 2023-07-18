/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;

import es.iti.wakamiti.api.extensions.*;
import es.iti.wakamiti.api.extensions.*;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WakamitiContributorsTest {

    @Test
    public void testAllContributors() {
        WakamitiContributors wakamitiContributors = new WakamitiContributors();
        wakamitiContributors.setClassLoaders(getClass().getClassLoader());
        Map<Class<?>, List<Contributor>> allContributors = wakamitiContributors.allContributors();
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
