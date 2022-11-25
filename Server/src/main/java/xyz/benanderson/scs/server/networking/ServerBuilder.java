package xyz.benanderson.scs.server.networking;

import lombok.Getter;
import xyz.benanderson.scs.networking.connection.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The {@code ServerBuilder} class allows a developer to construct a {@code Server} instance
 * and configure it prior to instantiation. Use {@link ServerBuilder#build()} to build a {@code Server}
 * from this {@code ServerBuilder}.
 */
@Getter
public class ServerBuilder {

    //attributes that will be used by the server during construction
    private final int port;
    private final InetAddress bindAddress;
    private BiConsumer<Connection, Server> clientConnectListener;
    private BiConsumer<Connection, Server> clientDisconnectListener;
    private Consumer<Server> serverShutdownListener;

    /**
     * @param port TCP port to run the networking server on
     * @param bindAddress TCP address to run the networking server on
     */
    public ServerBuilder(int port, InetAddress bindAddress) {
        this.port = port;
        this.bindAddress = bindAddress;
        this.clientConnectListener = (con, serv) -> {};
        this.clientDisconnectListener = (con, serv) -> {};
        this.serverShutdownListener = serv -> {};
    }

    /**
     * Runs after a client connects and the {@code Connection} is established
     */
    public ServerBuilder onClientConnect(BiConsumer<Connection, Server> clientConnectListener) {
        this.clientConnectListener = clientConnectListener;
        return this;
    }

    /**
     * Runs as the client disconnects, prior to the {@code Connection} closing
     */
    public ServerBuilder onClientDisconnect(BiConsumer<Connection, Server> clientDisconnectListener) {
        this.clientDisconnectListener = clientDisconnectListener;
        return this;
    }

    /**
     * Runs as the server shuts down, prior to the {@code Connection}s closing
     */
    public ServerBuilder onServerShutdown(Consumer<Server> serverShutdownListener) {
        this.serverShutdownListener = serverShutdownListener;
        return this;
    }

    /**
     * Construct a {@code Server} from this {@code ServerBuilder}
     */
    public Server build() throws IOException {
        return new Server(this);
    }

}