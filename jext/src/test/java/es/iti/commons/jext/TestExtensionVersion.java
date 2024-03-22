/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class TestExtensionVersion {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVersion1() {
        new ExtensionVersion("1.2.4");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVersion2() {
        new ExtensionVersion("1.dog");
    }


    @Test
    public void testIsCompatible() {
        ExtensionVersion v1_5 = new ExtensionVersion("1.5");
        assertThat(v1_5.major()).isEqualTo(1);
        assertThat(v1_5.minor()).isEqualTo(5);
        assertThat(v1_5).hasToString("1.5");
        ExtensionVersion v2_1 = new ExtensionVersion("2.1");
        ExtensionVersion v2_5 = new ExtensionVersion("2.5");
        assertThat(v1_5.isCompatibleWith(v2_1)).isFalse();
        assertThat(v2_1.isCompatibleWith(v1_5)).isFalse();
        assertThat(v2_1.isCompatibleWith(v2_5)).isFalse();
        assertThat(v2_5.isCompatibleWith(v2_1)).isTrue();
    }

}