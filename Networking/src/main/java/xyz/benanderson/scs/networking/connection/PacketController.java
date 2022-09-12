package xyz.benanderson.scs.networking.connection;

import xyz.benanderson.scs.networking.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The {@code PacketController} class provides an encapsulation around the low-level
 * reading/writing operations from/to the {@code Socket} which underpins the
 * {@link Connection}.
 * No API is publicly exposed and the class has no visibility modifier set, therefore
 * defaulting to package-private (only accessible by classes in the same package -
 * components of the connection abstraction). A {@code PacketController} is only
 * expected to be used by a {@link PacketListener} and a {@link PacketSender}.
 */
class PacketController implements AutoCloseable {

    /**
     * ObjectOutputStream wrapping & encapsulating the low-level
     * output stream of the {@code Socket}
     */
    private final ObjectOutputStream objectOutputStream;
    /**
     * ObjectInputStream wrapping & encapsulating the low-level
     * input stream of the {@code Socket}
     */
    private final ObjectInputStream objectInputStream;

    /**
     * Constructor for {@code PacketController} class
     *
     * @param connection {@code Connection} object that this {@code PacketController} is
     *                                     controlling the packets for.
     * @throws IOException thrown if an I/O error occurs when accessing the
     * input or output streams
     */
    public PacketController(Connection connection) throws IOException {
        //create ObjectInputStream from socket's abstract InputStream
        this.objectInputStream = new ObjectInputStream(
                //get InputStream from the Socket in the Connection
                //and pass it as the argument to the ObjectInputStream constructor
                connection.getSocket().getInputStream()
        );
        //create ObjectOutputStream from socket's abstract OutputStream
        this.objectOutputStream = new ObjectOutputStream(
                //get OutputStream from the Socket in the Connection
                //and pass it as the argument to the ObjectOutputStream constructor
                connection.getSocket().getOutputStream()
        );
    }

    /**
     * Method to write a {@code Packet} object to the output stream of the socket.
     * Visibility is 'protected' in order to encapsulate the method and make it only
     * accessible to classes in the same package (components of the connection abstraction).
     *
     * @param packet {@code Packet} object to write to the socket
     * @throws IOException thrown if an I/O error occurs
     */
    protected void writePacketToSocket(Packet packet) throws IOException {
        //synchronize access to the output stream across threads to avoid race conditions
        //in a multithreaded environment - because this method is not guaranteed to
        //be run by one thread at a time (atomically)
        synchronized (objectOutputStream) {
            //write the packet object to the output stream
            objectOutputStream.writeObject(packet);
            //flush the output stream to ensure it is pushed out of memory and
            //across the network to the receiving Socket
            objectOutputStream.flush();
            //reset the output stream to clear the cache and reset the state
            //of the output stream
            objectOutputStream.reset();
        }
    }

    /**
     * Method to read a {@code Packet} object from the input stream of the socket.
     * Visibility is 'protected' in order to encapsulate the method and make it only
     * accessible to classes in the same package (components of the connection abstraction).
     *
     * @throws IOException thrown if an I/O error occurs
     * @throws ClassNotFoundException thrown if the class of the object received does
     * not exist in the source code of the receiving application.
     */
    protected Packet readPacketFromSocket() throws IOException, ClassNotFoundException {
        //synchronize access to the input stream across threads to avoid race conditions
        //in a multithreaded environment - because this method is not guaranteed to
        //be run by one thread at a time (atomically)
        synchronized (objectInputStream) {
            //read an object from the objectInputStream and then cast it to a
            //`Packet` object (changes the type to be the `Packet` class)
            return (Packet) objectInputStream.readObject();
        }
    }

    /**
     * Method overriding the default `close()` implementation from {@link AutoCloseable}.
     * This method closes the input and output streams of the Socket.
     *
     * @throws Exception thrown if an exception occurs when closing the I/O streams.
     */
    @Override
    public void close() throws Exception {
        //close object input & output streams - will also close the underlying
        //abstract InputStream & OutputStream on the Socket.
        objectInputStream.close();
        objectOutputStream.close();
    }

}
