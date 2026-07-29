/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class TestExtensionVersion {

    private static final int EXPECTED_MINOR_VERSION = 5;

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
        ExtensionVersion v1d5 = new ExtensionVersion("1.5");
        assertThat(v1d5.major()).isEqualTo(1);
        assertThat(v1d5.minor()).isEqualTo(EXPECTED_MINOR_VERSION);
        assertThat(v1d5).hasToString("1.5");
        ExtensionVersion v2d1 = new ExtensionVersion("2.1");
        ExtensionVersion v2d5 = new ExtensionVersion("2.5");
        assertThat(v1d5.isCompatibleWith(v2d1)).isFalse();
        assertThat(v2d1.isCompatibleWith(v1d5)).isFalse();
        assertThat(v2d1.isCompatibleWith(v2d5)).isFalse();
        assertThat(v2d5.isCompatibleWith(v2d1)).isTrue();
    }

}
