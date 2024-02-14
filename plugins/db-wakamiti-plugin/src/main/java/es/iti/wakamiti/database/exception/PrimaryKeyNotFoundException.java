/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.exception;


import es.iti.wakamiti.database.DatabaseConfigContributor;


public class PrimaryKeyNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Cannot determine primary key for table '%s'. Please, disable the '%s' property";

    public PrimaryKeyNotFoundException(String table) {
        super(String.format(MESSAGE, table, DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION));
    }

    public PrimaryKeyNotFoundException(String table, Throwable e) {
        super(String.format(MESSAGE, table, DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION), e);
    }

}
