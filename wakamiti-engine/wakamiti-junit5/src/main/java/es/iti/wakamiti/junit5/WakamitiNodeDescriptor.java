/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;


/**
 * JUnit Platform descriptor representing a single Wakamiti plan node
 * (a feature, scenario or step) within the test tree.
 */
class WakamitiNodeDescriptor extends AbstractTestDescriptor {

    private final Type type;

    WakamitiNodeDescriptor(UniqueId uniqueId, String displayName, TestSource source, Type type) {
        super(uniqueId, displayName, source);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

}
