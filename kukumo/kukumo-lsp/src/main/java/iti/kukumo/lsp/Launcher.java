package iti.kukumo.lsp;

import java.io.IOException;
import java.net.*;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.*;
import org.eclipse.lsp4j.launch.LSPLauncher;


public class Launcher extends Thread {

    public static void main(String[] args) throws ParseException, IOException {

        CliArguments arguments = new CliArguments().parse(args);
        if (arguments.isHelpActive()) {
            arguments.printUsage();
            return;
        }

        if (arguments.isTcpServer()) {
        	if (arguments.debugEnabled()) {
        		enableDebugLogs();
        	}
        	InetSocketAddress address = new InetSocketAddress(arguments.port());
			var server = new TcpSocketLanguageServer(address , arguments.positionBase());
			server.start();
        } else {
        	disableConsoleLogs();
          	if (arguments.debugEnabled()) {
        		enableDebugLogs();
        	}
        	var server = new KukumoLanguageServer(arguments.positionBase());
            var launcher = LSPLauncher.createServerLauncher(
                server,
                System.in,
                System.out
            );
            server.connect(launcher.getRemoteProxy());
            launcher.startListening();
        }
    }





	private static void disableConsoleLogs() throws IOException {
		try {
			Configurator.reconfigure(
				Thread.currentThread().getContextClassLoader().getResource("log4j2-noconsole.xml").toURI()
			);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}


	private static void enableDebugLogs() {
		Configurator.setRootLevel(Level.DEBUG);

	}

}
