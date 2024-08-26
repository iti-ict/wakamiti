/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.properties;


import com.fasterxml.jackson.databind.JsonNode;
import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.extensions.PropertyEvaluator;
import es.iti.wakamiti.api.util.JsonUtils;
import es.iti.wakamiti.api.util.XmlUtils;
import es.iti.wakamiti.core.properties.StepPropertyEvaluator;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static es.iti.wakamiti.api.util.MapUtils.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class StepPropertyEvaluatorTest {

    private final StepPropertyEvaluator resolver = new StepPropertyEvaluator();
    private final JsonNode json = JsonUtils.json(Map.of("id", 3, "user", "pepe"));
    private final XmlObject xml = XmlUtils.xml("response", Map.of("id", 3, "user", "pepe"));

    @Before
    public void setup() {
        LinkedHashMap<String, Object> results = new LinkedHashMap<>();
        results.put("results", map(
                "s1", null,
                "s2", json,
                "s3", xml,
                "s4", json.toString(),
                "s5", xml.toString()
        ));

        WakamitiStepRunContext context = mock(WakamitiStepRunContext.class);
        Backend backend = mock(Backend.class);
        when(backend.getExtraProperties()).thenReturn(results);
        when(context.backend()).thenReturn(backend);
        WakamitiStepRunContext.set(context);
    }

    @Test
    public void testResolverWhenJsonNodeResponseWithSuccess() {
        PropertyEvaluator.Result result = resolver.eval("'${2#$.user}'");
        assertThat(result.value()).isEqualTo("'pepe'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${2#$.user}", "pepe"
        ));

        result = resolver.eval("'user${2#$.id}'");
        assertThat(result.value()).isEqualTo("'user3'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${2#$.id}", "3"
        ));

        result = resolver.eval("${2#$.id}");
        assertThat(result.value()).isEqualTo("3");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${2#$.id}", "3"
        ));

        result = resolver.eval("${2#$.id}${2#$.id}");
        assertThat(result.value()).isEqualTo("33");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${2#$.id}", "3"
        ));

        result = resolver.eval("${2#}");
        assertThat(result.value()).isEqualTo(json.toString());
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${2#}", json.toString()
        ));

        result = resolver.eval("${-4#}");
        assertThat(result.value()).isEqualTo(json.toString());
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${-4#}", json.toString()
        ));
    }

    @Test
    public void testResolverWhenJsonStringResponseWithSuccess() {
        PropertyEvaluator.Result result = resolver.eval("'${4#$.user}'");
        assertThat(result.value()).isEqualTo("'pepe'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${4#$.user}", "pepe"
        ));

        result = resolver.eval("'user${4#$.id}'");
        assertThat(result.value()).isEqualTo("'user3'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${4#$.id}", "3"
        ));

        result = resolver.eval("${4#$.id}");
        assertThat(result.value()).isEqualTo("3");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${4#$.id}", "3"
        ));

        result = resolver.eval("{${4#$.id}}");
        assertThat(result.value()).isEqualTo("{3}");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${4#$.id}", "3"
        ));

        result = resolver.eval("${4#$.id}${4#$.id}");
        assertThat(result.value()).isEqualTo("33");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${4#$.id}", "3"
        ));

        result = resolver.eval("${4#}");
        assertThat(result.value()).isEqualTo(json.toString());
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${4#}", json.toString()
        ));

        result = resolver.eval("${-2#}");
        assertThat(result.value()).isEqualTo(json.toString());
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${-2#}", json.toString()
        ));
    }

    @Test
    public void testResolverWhenXmlObjectResponseWithSuccess() {
        PropertyEvaluator.Result result = resolver.eval("'${3#//user/text()}'");
        assertThat(result.value()).isEqualTo("'pepe'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${3#//user/text()}", "pepe"
        ));

        result = resolver.eval("'user${3#//id/text()}'");
        assertThat(result.value()).isEqualTo("'user3'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${3#//id/text()}", "3"
        ));

        result = resolver.eval("${3#//id/text()}");
        assertThat(result.value()).isEqualTo("3");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${3#//id/text()}", "3"
        ));

        result = resolver.eval("${3#//id/text()}${3#//id/text()}");
        assertThat(result.value()).isEqualTo("33");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${3#//id/text()}", "3"
        ));

        result = resolver.eval("${3#}");
        assertThat(result.value()).isEqualTo(xml.toString());
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${3#}", xml.toString()
        ));

        result = resolver.eval("${-3#}");
        assertThat(result.value()).isEqualTo(xml.toString());
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${-3#}", xml.toString()
        ));
    }

    @Test
    public void testResolverWhenXmlStringResponseWithSuccess() {
        PropertyEvaluator.Result result = resolver.eval("'${5#//user/text()}'");
        assertThat(result.value()).isEqualTo("'pepe'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${5#//user/text()}", "pepe"
        ));

        result = resolver.eval("'user${5#//id/text()}'");
        assertThat(result.value()).isEqualTo("'user3'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${5#//id/text()}", "3"
        ));

        result = resolver.eval("${s5#//id/text()}");
        assertThat(result.value()).isEqualTo("3");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${s5#//id/text()}", "3"
        ));

        result = resolver.eval("${5#//id/text()}${5#//id/text()}");
        assertThat(result.value()).isEqualTo("33");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${5#//id/text()}", "3"
        ));

        result = resolver.eval("${5#}");
        assertThat(result.value()).isEqualTo(xml.toString());
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${5#}", xml.toString()
        ));

        result = resolver.eval("${-1#}");
        assertThat(result.value()).isEqualTo(xml.toString());
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${-1#}", xml.toString()
        ));
    }

    @Test(expected = WakamitiException.class)
    public void testResolveWhenResultNullWithError() {
        resolver.eval("'${1#$.user}'");
    }

    @Test(expected = WakamitiException.class)
    public void testResolveWhenNotValidOperationWithError() {
        resolver.eval("'${2#//user}'");
    }
}
