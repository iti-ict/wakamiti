/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.annotations;


public enum Level {

    /** Operations that surround the execution of the complete plan. */
    PLAN,
    /** Operations that surround the execution of one feature. */
    FEATURE,
    /** Operations that surround the execution of one scenario. */
    SCENARIO

}
