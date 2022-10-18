package xyz.benanderson.scs.server.networking;

import lombok.Getter;
import xyz.benanderson.scs.networking.connection.Connection;

import java.net.InetAddress;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class ServerBuilder {

    private final int port;
    private final InetAddress bindAddress;
    private BiConsumer<Connection, Server> clientConnectListener;
    private BiConsumer<Connection, Server> clientDisconnectListener;
    private Consumer<Server> serverShutdownListener;

    public ServerBuilder(int port, InetAddress bindAddress) {
        this.port = port;
        this.bindAddress = bindAddress;
        this.clientConnectListener = (con, serv) -> {};
        this.clientDisconnectListener = (con, serv) -> {};
        this.serverShutdownListener = serv -> {};
    }

    /**
     * Runs after client connects and Connection is established
     */
    public ServerBuilder onClientConnect(BiConsumer<Connection, Server> clientConnectListener) {
        this.clientConnectListener = clientConnectListener;
        return this;
    }

    /**
     * Runs as the client disconnects, prior to the Connection closing
     */
    public ServerBuilder onClientDisconnect(BiConsumer<Connection, Server> clientDisconnectListener) {
        this.clientDisconnectListener = clientDisconnectListener;
        return this;
    }

    public ServerBuilder onServerShutdown(Consumer<Server> serverShutdownListener) {
        this.serverShutdownListener = serverShutdownListener;
        return this;
    }

    public Server build() {
        return new Server(this);
    }

}
