/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.properties;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.PropertyEvaluator;
import es.iti.wakamiti.core.properties.GlobalPropertyEvaluator;
import es.iti.wakamiti.api.imconfig.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class GlobalPropertyEvaluatorTest {

    private final GlobalPropertyEvaluator resolver = new GlobalPropertyEvaluator();

    @Before
    public void setup() {
        Configuration configuration = Configuration.factory().fromPairs(
                "user", "pepe",
                "user.id", "3",
                "number", "4"
        );
        resolver.configure(configuration);
    }

    @Test
    public void testResolveWithSuccess() {
        PropertyEvaluator.Result result = resolver.eval("'${user}'");
        assertThat(result.value()).isEqualTo("'pepe'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${user}", "pepe"
        ));

        result = resolver.eval("'user${user.id}'");
        assertThat(result.value()).isEqualTo("'user3'");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${user.id}", "3"
        ));

        result = resolver.eval("${user.id}");
        assertThat(result.value()).isEqualTo("3");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${user.id}", "3"
        ));

        result = resolver.eval("${user.id}${user.id}");
        assertThat(result.value()).isEqualTo("33");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${user.id}", "3"
        ));

        result = resolver.eval("1${number}");
        assertThat(result.value()).isEqualTo("14");
        assertThat(result.evaluations()).containsExactlyEntriesOf(Map.of(
                "${number}", "4"
        ));

        result = resolver.eval("is equal to 1");
        assertThat(result.value()).isEqualTo("is equal to 1");
        assertThat(result.evaluations()).isEmpty();
    }

    @Test(expected = WakamitiException.class)
    public void testResolveWhenPropertyIsNotPresentWithError() {
        resolver.eval("'${other}'");
    }
}
