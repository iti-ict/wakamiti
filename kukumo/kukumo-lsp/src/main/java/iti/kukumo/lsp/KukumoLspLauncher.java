package iti.kukumo.lsp;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KukumoLspLauncher extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(KukumoLspLauncher.class);

    public static void main(String[] args) throws ParseException, IOException {
        CliArguments arguments = new CliArguments().parse(args);
        if (arguments.isHelpActive()) {
            arguments.printUsage();
        } else {
            int port = arguments.port();
            var address = new InetSocketAddress(port);
            var socketServer = new TcpSocketServer(address);
            socketServer.start();
        }
    }


}
