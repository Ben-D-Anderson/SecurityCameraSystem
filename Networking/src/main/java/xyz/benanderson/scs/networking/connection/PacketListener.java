package xyz.benanderson.scs.networking.connection;

import xyz.benanderson.scs.networking.Packet;

import java.io.EOFException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * The {@code PacketSender} class exposes a high-level API to developers,
 * allowing them to create callbacks for received packets from the parent {@code Connection}.
 * This class internally uses a map to correlate packet types to a list of packet callbacks
 * for that type. These packet callbacks are executed asynchronously from packet listening
 * thread and therefore should not block the flow of execution with blocking callouts.
 */
public class PacketListener implements AutoCloseable {

    //encapsulated map data structure storing a list of callback code blocks to run for each
    //packet type.
    private final Map<Class<? extends Packet>, List<Consumer<Packet>>> callbacks;
    //encapsulated asynchronous thread, on which, packets are listened for.
    private final Thread packetListeningThread;
    //private, thread-safe, atomic boolean variable to control the condition-controlled
    //loop in the packet listening thread.
    private final AtomicBoolean listeningForPackets = new AtomicBoolean(true);

    /**
     * Constructor for {@code PacketListener} class
     *
     * @param connection {@code Connection} object that this {@code PacketListener} is
     *                                     listening for packets from.
     */
    public PacketListener(Connection connection) {
        //initialise the callback map with an empty hashmap
        this.callbacks = new HashMap<>();

        //create thread
        this.packetListeningThread = new Thread(() -> {
            //code to run in the thread
            while (listeningForPackets.get() && connection.isConnected()) {
                //try to read the packet from the underlying PacketController
                try {
                    Packet packet = connection.getPacketController().readPacketFromSocket();
                    //if the packet was successfully read from the connection,
                    //run callbacks associated with the packet
                    runCallbacks(packet);
                } catch (EOFException disconnected) {
                    break;
                } catch (IOException e) {
                    //exception will be thrown if the connection was closed, in which
                    //case ignore it and return
                    if (!connection.isConnected()) return;

                    //if the packet was NOT successfully read from the connection,
                    //log the error to the standard error stream.
                    System.err.println("[ERROR] An error occurred whilst reading a packet" +
                            " from a connection:");
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    //if the packet received was not a valid packet type, log the error
                    //to the standard error stream.
                    System.err.println("[WARNING] An unknown packet type was received" +
                            " from a connection: ");
                    e.printStackTrace();
                }
            }
            try {
                if (connection.isConnected())
                    connection.close();
            } catch (Exception ignored) {}
        }, "Packet Listening Thread" /* name of the thread */);

        //start the asynchronous packet listening thread
        this.packetListeningThread.start();
    }

    /**
     * Method to add a callback to the parent connection. The callback will be run when a packet
     * is received with the correct type for that callback.
     *
     * @param packetClass type of the packet that the callback will be triggered by
     * @param callback the code to run with the packet
     */
    public <T extends Packet> void addCallback(Class<T> packetClass, Consumer<T> callback) {
        //if a callback of the packet class type has not already been registered,
        //then add it to the map with a new, empty linked list.
        if (!callbacks.containsKey(packetClass))
            callbacks.put(packetClass, new LinkedList<>());

        //add the callback to the list corresponding to the packet class
        //key in the callbacks map
        callbacks.get(packetClass).add((Consumer<Packet>) callback);
    }

    /**
     * Method with private visibility to run all callbacks associated with the type
     * of the packet provided.
     *
     * @param packet packet to run callbacks on
     */
    private void runCallbacks(Packet packet) {
        //check if any callbacks are registered for the packet type
        if (callbacks.containsKey(packet.getType()))
            //if callbacks are registered run all callbacks for the packet type
            //using the packet as the argument
            callbacks.get(packet.getType()).forEach(callback -> callback.accept(packet));
    }

    /**
     * Method overriding the default `close()` implementation from {@link AutoCloseable}.
     * This method disables the constant listening for packets on the asynchronous
     * 'Packet Listening Thread' and waits for the thread to die.
     *
     * @throws InterruptedException thrown if an exception occurs when waiting for the thread to die.
     */
    @Override
    public void close() throws InterruptedException {
        //set loop-controlling variable to false in order to disable the while loop
        //in the packet listening thread
        this.listeningForPackets.set(false);
        //wait for the packet listening thread to die
        this.packetListeningThread.join();
    }

}
