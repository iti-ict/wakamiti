/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;

import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.plan.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GroovyStepContributorTest {

    private final GroovyStepContributor contributor = new GroovyStepContributor();

    @Before
    public void setup() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("results", new LinkedList<>());
        properties.put("id", "ID-01");
        Backend backend = mock(Backend.class);
        when(backend.getExtraProperties()).thenReturn(properties);
        WakamitiStepRunContext.set(new WakamitiStepRunContext(null, backend, null, null));
    }

    @Test
    public void testExecute() {
        Object result = contributor.execute(new Document(
                "def result = 2 + 2;" +
                        "result + 2 as String"
        ));
        assertThat(result).isEqualTo("6");
    }

    @Test(expected = WakamitiException.class)
    public void testExecuteWhenError() {
        contributor.execute(new Document("2 / 0"));
    }

}
