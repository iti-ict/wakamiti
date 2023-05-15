/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.server;

import java.net.InetSocketAddress;

import es.iti.wakamiti.lsp.TcpSocketLanguageServer;
import io.quarkus.runtime.*;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Runner {

    public static void main(String... args) {
        Quarkus.run(WakamitiServer.class, args);
    }

    public static class WakamitiServer implements QuarkusApplication {

        @Override
        public int run(String... args) throws Exception {

        	int languageServerPort = 8090;
			InetSocketAddress address = new InetSocketAddress(languageServerPort);
			TcpSocketLanguageServer languageServer = new TcpSocketLanguageServer(address , 0);
			languageServer.start();

            Quarkus.waitForExit();

            languageServer.close();

            return 0;
        }
    }
}