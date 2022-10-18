package xyz.benanderson.scs.server;

import xyz.benanderson.scs.networking.connection.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server implements AutoCloseable {

    //todo pull Server.java from Curio

    private final AtomicBoolean listeningForConnections = new AtomicBoolean(true);
    private final Thread listeningThread;
    private ServerSocket serverSocket;
    private final int port;

    public Server(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port number outside acceptable range");
        }
        this.port = port;
        this.listeningThread = new Thread(this::listen, "Server Listening Thread");
    }

    public void start() throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.listeningThread.start();
    }

    private void listen() {
        try {
            while (listeningForConnections.get()) {
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(socket);

            }
        } catch (IOException e) {
            if (listeningForConnections.get()) {
                System.err.println("[ERROR] An I/O Error Occurred On The Server");
                e.printStackTrace();
            }
            System.out.println("[INFO] Server Shutdown");
        }
    }

    @Override
    public void close() throws Exception {
        this.listeningForConnections.set(false);
        this.serverSocket.close();
        this.listeningThread.join();
    }

}
