/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.groovy;

import iti.wakamiti.api.WakamitiException;
import iti.wakamiti.api.plan.Document;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class GroovyStepContributorTest {

    private final GroovyStepContributor contributor = new GroovyStepContributor();

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
