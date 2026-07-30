/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


/**
 * Defines the values supported by Completion Type.
 */
public enum CompletionType {

    /** Completion that inserts a localized Gherkin structural keyword. */
    KEYWORD,
    /** Completion that inserts a configured Wakamiti property reference. */
    PROPERTY,
    /** Completion that inserts a matching step definition expression. */
    STEP

}
