package iti.kukumo.lsp;

import org.apache.commons.cli.ParseException;
import org.eclipse.lsp4j.launch.LSPLauncher;

public class Launcher extends Thread {

    public static void main(String[] args) throws ParseException {
        CliArguments arguments = new CliArguments().parse(args);
        if (arguments.isHelpActive()) {
            arguments.printUsage();
        } else {
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


}
