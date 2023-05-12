/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.server;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeExecutionResourceIT extends ExecutionResourceTest {

    // Execute the same tests but in native mode.
}