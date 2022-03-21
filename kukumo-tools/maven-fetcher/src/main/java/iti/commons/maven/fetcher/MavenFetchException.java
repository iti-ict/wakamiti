/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.maven.fetcher;

public class MavenFetchException extends Exception{

    private static final long serialVersionUID = 1L;


    public MavenFetchException(Throwable e) {
        super(e);
    }

}