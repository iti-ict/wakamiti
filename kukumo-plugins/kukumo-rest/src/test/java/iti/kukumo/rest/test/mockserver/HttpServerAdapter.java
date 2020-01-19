package iti.kukumo.rest.test.mockserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import iti.kukumo.rest.test.mockserver.InMemoryServer.HttpExchange;
import iti.kukumo.rest.test.mockserver.InMemoryServer.HttpServer;


public class HttpServerAdapter extends Thread implements InMemoryServer.HttpServer {

    public static InMemoryServer.HttpServerFactory serverFactory() {
        return new InMemoryServer.HttpServerFactory() {
            @Override
            public HttpServer create(int port, Charset charset, InMemoryServer.HttpHandler handler) {
                return new HttpServerAdapter(port,charset,handler);
            }
        };
    }


    private int port;
    private Charset charset;
    private ServerSocket serverSocket;
    private InMemoryServer.HttpHandler handler;
    private volatile boolean alive;

    private HttpServerAdapter(int port, Charset charset, InMemoryServer.HttpHandler handler) {
        this.port = port;
        this.charset = charset;
        this.handler = handler;
    }


    @Override
    public void run() {
        while(alive) {
            try (Socket connection = serverSocket.accept()) {
                handler.handle(new HttpExchangeConnection(serverSocket,connection,charset));
            } catch (IOException e) {
                alive = false;
            }
        }
    }


    @Override
    public void startServer() throws IOException {
        this.alive = true;
        this.serverSocket = new ServerSocket(port);
        this.start();
    }


    @Override
    public void stopServer() throws IOException {
        this.alive = false;
        this.serverSocket.close();
    }



    private static class HttpExchangeConnection implements HttpExchange {

        private final InputStream request;
        private final OutputStream response;
        private final String method;
        private final URI requestURI;
        private final Charset charset;
        private String responseContentType;
        private int responseCode;
        private int responseContentLength;
        private String responseBody;

        public HttpExchangeConnection(ServerSocket server, Socket connection, Charset charset) throws IOException {

            this.request = connection.getInputStream();
            this.response = connection.getOutputStream();
            this.charset = charset;

            String[] protocol = readLine(request).split(" ");
            this.method = protocol[0];
            this.requestURI = URI.create("http://127.0.0.1"+server.getLocalPort()+"/"+protocol[1]);

            String line;
            do {
                line = readLine(request);
            } while (!line.isBlank());

        }

        @Override
        public void writeResponse() throws IOException {
            try (var writer = new BufferedWriter(new OutputStreamWriter(response, charset))) {
                writeLine(writer,"HTTP/1.1 "+responseCode);
                writeLine(writer,"Content-Type: "+responseContentType);
                writeLine(writer,"Content-Length: "+responseContentLength);
                writer.newLine();
                writer.write(responseBody);
            }

        }

        @Override
        public String getRequestMethod() {
            return method;
        }

        @Override
        public URI getRequestURI() {
            return requestURI;
        }

        @Override
        public InputStream getRequestBody() {
            return request;
        }


        @Override
        public void setResponseContentType(String responseContentType) {
            this.responseContentType = responseContentType;

        }

        @Override
        public void setResponseCode(int status) {
            this.responseCode = status;
        }


        @Override
        public void setResponseContentLength(int length) {
            this.responseContentLength = length;
        }


        @Override
        public void setResponseBody(String body) {
            this.responseBody = body;

        }

    }



    private static String readLine(InputStream input) throws IOException {
        StringBuilder line = new StringBuilder();
        int read;
        while ((read = input.read()) != '\n') {
            line.append((char)read);
        }
        return line.toString();
    }


    private static void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }




}
