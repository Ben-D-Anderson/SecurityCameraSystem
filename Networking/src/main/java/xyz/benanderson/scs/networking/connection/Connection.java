package xyz.benanderson.scs.networking.connection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Connection interface which provides high level access to the connection between
 * two services. This component contains the key elements for a peer-to-peer connection
 * including the Socket, PacketController, PacketSender and PacketListener.
 */
public class Connection implements AutoCloseable {

    /**
     * Socket attribute with connection-package level getter visibility
     */
    @Getter(AccessLevel.PACKAGE)
    private final Socket socket;

    /**
     * PacketController attribute with connection-package level getter visibility
     */
    @Getter(AccessLevel.PACKAGE)
    private final PacketController packetController;

    /**
     * Unique ID attribute to help tell difference between connections
     */
    @Getter(AccessLevel.PUBLIC)
    private final UUID id;

    /**
     * PacketSender attribute with getter
     */
    @Getter(AccessLevel.PUBLIC)
    private final PacketSender packetSender;

    /**
     * PacketListener attribute with getter
     */
    @Getter(AccessLevel.PUBLIC)
    private final PacketListener packetListener;

    /**
     * Constructor for {@code Connection} class.
     *
     * @param socket the lower-level Java socket which the {@code Connection}
     *               object will be built on top of.
     * @throws IOException thrown if an I/O errors when preparing the I/O streams.
     */
    public Connection(Socket socket) throws IOException {
        this.id = UUID.randomUUID();
        this.socket = socket;
        this.packetController = new PacketController(this);
        this.packetSender = new PacketSender(this);
        this.packetListener = new PacketListener(this);
    }

    @Getter
    @Setter
    private Consumer<Connection> disconnectListener;

    /**
     * Method to override default implementation in {@link AutoCloseable} interface.
     * Requests {@code PacketController} to close input & output streams of the socket,
     * the closes the socket directly.
     *
     * @throws Exception thrown if an I/O errors when closing the I/O streams or socket.
     */
    @Override
    public synchronized void close() throws Exception {
        getDisconnectListener().accept(this);
        getPacketController().close();
        getPacketSender().close();
        getPacketListener().close();
        socket.close();
    }

    /**
     * Method to check the open/close state of the connection.
     *
     * @return true if this connection is connected to a peer, false if it isn't.
     */
    public synchronized boolean isConnected() {
        return getSocket().isConnected() && !getSocket().isClosed();
    }

}
