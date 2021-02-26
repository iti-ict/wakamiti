package iti.kukumo.lsp;

import java.io.IOException;
import java.net.InetSocketAddress;

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
        	InetSocketAddress address = new InetSocketAddress(arguments.port());
			var server = new TcpSocketLanguageServer(address , arguments.positionBase());
			server.start();
        } else {
        	disableLogs();
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


	private static void disableLogs() {
		Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);
	}


}
