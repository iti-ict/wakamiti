/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
module es.iti.wakamiti.launcher {

    exports es.iti.wakamiti.launcher;

    requires es.iti.wakamiti.core;
    requires java.instrument;
    requires org.slf4j;
    requires slf4jansi;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires net.harawata.appdirs;
    requires org.apache.commons.cli;
    requires es.iti.wakamiti.api;
    requires org.apache.commons.lang3;

}
