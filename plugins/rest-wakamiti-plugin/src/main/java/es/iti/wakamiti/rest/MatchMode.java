/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest;


/**
 * Defines the values supported by Match Mode.
 */
public enum MatchMode {

    /** Requires exactly the expected JSON structure and array order. */
    STRICT,
    /** Requires exact JSON content while allowing array elements in any order. */
    STRICT_ANY_ORDER,
    /** Accepts additional actual JSON fields while checking all expected content. */
    LOOSE

}
