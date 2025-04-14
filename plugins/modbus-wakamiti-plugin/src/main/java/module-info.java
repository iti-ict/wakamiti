/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.StepContributor;

open module es.iti.wakamiti.modbus {

    exports es.iti.wakamiti.modbus;

    requires es.iti.wakamiti.api;
    requires org.apache.commons.lang3;
    requires jlibmodbus;

    provides StepContributor with ModbusStepContributor;
    provides ConfigContributor with ModbusConfigContributor;

}