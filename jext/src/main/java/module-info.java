/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import es.iti.commons.jext.ExtensionLoader;
import es.iti.commons.jext.ExtensionProcessor;

module iti.commons.jext {

    exports es.iti.commons.jext;

    requires transitive java.compiler;
    requires org.slf4j;

    uses javax.annotation.processing.Processor;
    uses ExtensionLoader;

    provides javax.annotation.processing.Processor with ExtensionProcessor;
}