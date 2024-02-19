/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test;

import es.iti.wakamiti.core.DefaultWakamitiAPI;

public class TestWakamitiAPI extends DefaultWakamitiAPI {

    @Override
    public String version() {
        return System.getProperty("version");
    }
}
