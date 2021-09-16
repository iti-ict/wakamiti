package iti.kukumo.server;

import java.net.InetSocketAddress;

import io.quarkus.runtime.*;
import io.quarkus.runtime.annotations.QuarkusMain;
import iti.kukumo.lsp.TcpSocketLanguageServer;

@QuarkusMain
public class Runner {

    public static void main(String... args) {
        Quarkus.run(KukumoServer.class, args);
    }

    public static class KukumoServer implements QuarkusApplication {

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