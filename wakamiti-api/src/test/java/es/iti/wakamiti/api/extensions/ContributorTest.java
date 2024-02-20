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
        assertThat(new TestContributorExt3().info()).isEqualTo("es.iti_someText:test_aCapText:1.0.2");
    }


    @ExtensionPoint
    interface MockContributor extends Contributor {
    }


    static class TestContributor implements MockContributor {
    }

    @Extension(provider = "wakamiti-moreText", name = "test-contrib-moreText")
    static
    class TestContributorExt1 implements MockContributor {
        @Override
        public String version() {
            return "1.0";
        }
    }

    @Extension(provider = "es.iti_somethingElse", name = "test_somethingElse")
    static
    class TestContributorExt2 implements MockContributor {
        @Override
        public String version() {
            return "1.0";
        }
    }

    @Extension(provider = "es.iti_someText", name = "test_aCapText")
    static
    class TestContributorExt3 implements MockContributor {
        @Override
        public String version() {
            return "1.0.2";
        }
    }

}
