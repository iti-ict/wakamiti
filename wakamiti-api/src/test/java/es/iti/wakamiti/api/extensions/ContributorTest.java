/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;

import es.iti.commons.jext.Extension;
import es.iti.commons.jext.ExtensionPoint;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContributorTest {

    @Test
    public void testContributorInfo() {
        assertThat(new TestContributor().info()).isEqualTo("es.iti.wakamiti.api.extensions.ContributorTest.TestContributor");
        assertThat(new TestContributorExt1().info()).isEqualTo("wakamiti-moreText:test-contrib-moreText:1.0");
        assertThat(new TestContributorExt2().info()).isEqualTo("es.iti_somethingElse:test_somethingElse:1.0");
        assertThat(new TestContributorExt3().info()).isEqualTo("es.iti_someText:test_aCapText:1.0");
    }

    @Test
    public void testStepContributorInfo() {
        assertThat(new TestStepContributor().info()).isEqualTo("es.iti:TestStep");
        assertThat(new TestStepContributorExt1().info()).isEqualTo("wakamiti-more:test-contrib");
        assertThat(new TestStepContributorExt2().info()).isEqualTo("es.iti:test_something");
        assertThat(new TestStepContributorExt3().info()).isEqualTo("es.iti_some:test_aCapText");
        assertThat(new TestStepContributorExt4().info()).isEqualTo("thisisthenameo:nameoftheexten");
    }


    @ExtensionPoint
    interface MockContributor extends Contributor {}


    class TestContributor implements MockContributor {}
    @Extension(provider = "wakamiti-moreText", name = "test-contrib-moreText")
    class TestContributorExt1 implements MockContributor {}
    @Extension(provider = "es.iti_somethingElse", name = "test_somethingElse")
    class TestContributorExt2 implements MockContributor { }
    @Extension(provider = "es.iti_someText", name = "test_aCapText")
    class TestContributorExt3 implements MockContributor {}

    class TestStepContributor implements StepContributor {}
    @Extension(provider = "wakamiti-moreText", name = "test-contrib-moreText")
    class TestStepContributorExt1 implements StepContributor {}
    @Extension(provider = "es.iti_somethingElse", name = "test_somethingElse")
    class TestStepContributorExt2 implements StepContributor {}
    @Extension(provider = "es.iti_someText", name = "test_aCapText")
    class TestStepContributorExt3 implements StepContributor {}
    @Extension(provider = "thisisthenameofprovider", name = "nameoftheextension")
    class TestStepContributorExt4 implements StepContributor {}

}
