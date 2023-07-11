/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;

import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.extensions.PropertyEvaluator;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroovyPropertyEvaluatorTest {

    PropertyEvaluator evaluator = new GroovyPropertyEvaluator();

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
    public void test() {
        PropertyEvaluator.Result result = evaluator.eval("${=2+2}");
        assertThat(result.value()).isEqualTo("4");

        result = evaluator.eval("Today is ${=new Date().format('yyyy-MM-dd')}");
        assertThat(result.value())
                .isEqualTo("Today is " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        result = evaluator.eval("Current ID: ${=ctx.id}");
        assertThat(result.value())
                .isEqualTo("Current ID: ID-01");
    }

    @Test(expected = WakamitiException.class)
    public void testWithError() {
        evaluator.eval("${=assert false}");
    }
}
