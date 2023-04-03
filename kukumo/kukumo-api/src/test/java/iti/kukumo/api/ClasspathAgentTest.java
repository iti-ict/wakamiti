/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.api;

import org.junit.Test;

import java.lang.instrument.Instrumentation;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class ClasspathAgentTest {

    @Test
    public void testPremain() {
        Instrumentation instrumentation = mock(Instrumentation.class);

        ClasspathAgent.premain("", instrumentation);

        assertNotNull(ClasspathAgent.instrumentation);
    }

    @Test
    public void agentmain() {
        Instrumentation instrumentation = mock(Instrumentation.class);

        ClasspathAgent.agentmain("", instrumentation);

        assertNotNull(ClasspathAgent.instrumentation);
    }

}
