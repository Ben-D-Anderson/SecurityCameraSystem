package xyz.benanderson.scs.server.networking;

import lombok.Getter;
import xyz.benanderson.scs.networking.connection.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Server implements AutoCloseable {

    @Getter
    private ServerSocket serverSocket;
    private Thread receiveConnectionsThread;
    @Getter
    private final int port;
    private final InetAddress bindAddress;
    private final SortedMap<UUID, Connection> connections;
    private final AtomicBoolean running;
    private final BiConsumer<Connection, Server> clientConnectListener;
    private final BiConsumer<Connection, Server> clientDisconnectListener;
    private final Consumer<Server> serverShutdownListener;

    public Server(ServerBuilder serverBuilder) {
        this.connections = Collections.synchronizedSortedMap(new TreeMap<>());
        this.running = new AtomicBoolean(true);
        this.port = serverBuilder.getPort();
        this.bindAddress = serverBuilder.getBindAddress();
        this.clientConnectListener = serverBuilder.getClientConnectListener();
        this.clientDisconnectListener = serverBuilder.getClientDisconnectListener();
        this.serverShutdownListener = serverBuilder.getServerShutdownListener();
    }

    public boolean isOpen() {
        return serverSocket != null && !serverSocket.isClosed();
    }

    /**
     * @return An unmodifiable SortedMap representing the Connections to the server
     */
    public SortedMap<UUID, Connection> getConnections() {
        return Collections.unmodifiableSortedMap(connections);
    }

    public void start() throws IOException {
        start(new ServerSocket(this.port, 5, this.bindAddress));
    }

    public void start(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.receiveConnectionsThread = new Thread(this::receiveConnections);
        this.receiveConnectionsThread.start();
    }

    @Override
    public void close() throws Exception {
        connections.values().forEach(con -> {
            try {
                con.close();
            } catch (Exception ignored) {}
        });
        connections.clear();
        running.set(false);
        serverSocket.close();
        receiveConnectionsThread.join();
    }

    private void receiveConnections() {
        while (running.get() && !serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(socket);
                connections.put(connection.getId(), connection);
                clientConnectListener.accept(connection, this);
                BiConsumer<Connection, Server> closeListener = (con, server) -> {
                    try {
                        connections.remove(con.getId()).close();
                    } catch (Exception ignored) {}
                };
                connection.setDisconnectListener(con -> clientDisconnectListener.andThen(closeListener).accept(con, this));
            } catch (IOException ignored) {
            }
        }
        serverShutdownListener.accept(this);
    }

}