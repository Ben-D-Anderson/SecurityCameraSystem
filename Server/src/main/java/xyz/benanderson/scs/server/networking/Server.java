package xyz.benanderson.scs.server.networking;

import lombok.Getter;
import xyz.benanderson.scs.networking.connection.Connection;
import xyz.benanderson.scs.server.configuration.ConfigurationWrapper;

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

    //attributes of the server
    private final ServerSocket serverSocket;
    private final Thread receiveConnectionsThread;
    private final int port;
    private final SortedMap<UUID, Connection> connections;
    private final AtomicBoolean running;
    private final BiConsumer<Connection, Server> clientConnectListener;
    private final BiConsumer<Connection, Server> clientDisconnectListener;
    private final Consumer<Server> serverShutdownListener;

    /**
     * Constructor with package-private (default) visibility to allow only the {@code ServerBuilder}
     * class to instantiate {@code Server} instances.
     *
     * @param serverBuilder builder to construct this {@code Server} from
     */
    Server(ServerBuilder serverBuilder) throws IOException {
        //create a synchronized map to avoid race conditions in a multithreaded environment
        //uses a TreeMap as a key-value store for connections, where the key is the
        //Connection's identifier and the value is the Connection object.
        //A TreeMap was used instead of a HashMap as O(1) retrieval is not essential as
        //operations against this map will not occur often.
        this.connections = Collections.synchronizedSortedMap(new TreeMap<>());
        //mark the server as running
        this.running = new AtomicBoolean(true);
        //assign attributes from serverBuilder...
        this.port = serverBuilder.getPort();
        InetAddress bindAddress = serverBuilder.getBindAddress();
        this.clientConnectListener = serverBuilder.getClientConnectListener();
        //append code to remove the Connection from the Server's internal connections map
        //to the end of the client disconnect listener
        BiConsumer<Connection, Server> closeListener = (con, server) -> {
            try { connections.remove(con.getId()).close(); } catch (Exception ignored) {}
        };
        this.clientDisconnectListener = serverBuilder.getClientDisconnectListener().andThen(closeListener);
        this.serverShutdownListener = serverBuilder.getServerShutdownListener();

        this.serverSocket = new ServerSocket(this.port, 5, bindAddress);
        this.receiveConnectionsThread = new Thread(this::receiveConnections);
        this.receiveConnectionsThread.start();
    }

    public int getPort() {
        return getServerSocket().getLocalPort();
    }

    public ServerSocket getServerSocket() {
        synchronized (serverSocket) {
            return serverSocket;
        }
    }

    /**
     * @return boolean denoting if the {@code Server} is open and running.
     */
    public boolean isOpen() {
        return getServerSocket() != null && !getServerSocket().isClosed() && running.get();
    }

    /**
     * @return An unmodifiable SortedMap representing the {@code Connection}(s) to the server
     */
    public SortedMap<UUID, Connection> getConnections() {
        return Collections.unmodifiableSortedMap(connections);
    }

    /**
     * Closes the {@code Server} and stops listening for new connections. The server shutdown
     * listener will be triggered by the receive-connections thread. All connections will be
     * closed and cleared from the {@code Server}.
     */
    @Override
    public void close() {
        //attempt to stop listening for connections on the receive-connections thread
        running.set(false);
        try {
            getServerSocket().close();
        } catch (Exception ignored) {}
        //wait for the receive-connections thread to terminate (also waits for the
        //server shutdown listener to execute).
        try {
            receiveConnectionsThread.join();
        } catch (InterruptedException ignored) {}
        //closes all connections
        connections.values().forEach(con -> {
            try {
                con.close();
            } catch (Exception ignored) {}
        });
        //clear connections from memory of the server
        connections.clear();
    }

    //private-visibility method ran only by the receive-connections thread
    private void receiveConnections() {
        //condition-controlled loop to keep listening thread active as long as the
        //server is running and open
        while (isOpen()) {
            //don't accept new connections if already at max connections as specified in config
            if (connections.size() >= ConfigurationWrapper.getInstance().getMaxConnections())
                continue;
            try {
                //block until connection is accepted from the server's socket
                Socket socket = getServerSocket().accept();
                //instantiate Connection object (from connection framework) using
                //the connection's socket
                Connection connection = new Connection(socket);
                //add disconnect listener to the connection
                connection.setDisconnectListener(con -> clientDisconnectListener.accept(con, this));
                //add connection to server's map of connections
                connections.put(connection.getId(), connection);
                //run the client connection listener with the newly accepted connection
                clientConnectListener.accept(connection, this);
            } catch (IOException e) {
                //print error if an exception occurs
//                e.printStackTrace();
            }
        }
        //run the server shutdown listener as isOpen() now returns false
        serverShutdownListener.accept(this);
    }

}