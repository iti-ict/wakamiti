/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.files;

import java.io.IOException;

public class FilesHelperException extends RuntimeException {

    public FilesHelperException(IOException e) {
        super(e);
    }
}