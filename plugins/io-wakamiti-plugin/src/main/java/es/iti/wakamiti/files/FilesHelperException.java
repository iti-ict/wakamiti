/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.files;


import java.io.IOException;


/**
 * Exception raised by the Files Helper Exception component.
 */
public class FilesHelperException extends RuntimeException {

    /**
     * Wraps a low-level filesystem failure raised by helper operations.
     *
     * @param e original I/O failure
     */
    public FilesHelperException(
            IOException e
    ) {
        super(e);
    }

}
