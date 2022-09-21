package xyz.benanderson.scs.networking.connection;

import xyz.benanderson.scs.networking.Packet;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code PacketSender} class exposes a high-level API to developers,
 * allowing them to send packets across the parent {@code Connection}.
 * This class internally uses a {@code Queue} for {@code Packet}s waiting to be sent.
 * This packet queue is then accessed internally to send packets asynchronously.
 */
public class PacketSender implements AutoCloseable {

    //encapsulated packet queue which stores packets to be sent asynchronously
    private final Queue<Packet> packetQueue;
    //encapsulated asynchronous thread which packets are sent from
    private final Thread packetSendingThread;
    //private, thread-safe, atomic boolean variable to control the condition-controlled
    //loop in the packet sending thread.
    private final AtomicBoolean sendingPackets = new AtomicBoolean(true);

    /**
     * Constructor for {@code PacketSender} class
     *
     * @param connection {@code Connection} object that this {@code PacketSender} is
     *                                     sending the packets for.
     */
    public PacketSender(Connection connection) {
        //initialise queue attribute with a ConcurrentLinkedQueue object,
        //this allows asynchronous access to the queue without any race
        //conditions or unexpected behaviour.
        this.packetQueue = new ConcurrentLinkedQueue<>();

        //create thread
        this.packetSendingThread = new Thread(() -> {
            //code to run in the thread
            while (sendingPackets.get()) {
                //using peek instead of poll so that if a packet failed to send
                //it can be tried again
                Packet packetToSend = packetQueue.peek();
                //if there are no packets in the queue, go to the start of the while loop
                //and check again for packets to send
                if (packetToSend == null) continue;

                //try to write the packet to the underlying PacketController
                try {
                    connection.getPacketController().writePacketToSocket(packetToSend);
                    //if the packet was successfully sent across the connection,
                    //remove it from the queue of packets to send
                    packetQueue.remove();
                } catch (IOException e) {
                    //if the packet was NOT successfully sent across the connection,
                    //keep it in the queue of packets to send and instead log the error
                    //to the standard error stream - this results in trying to
                    //send the packet again later.
                    System.err.println("[ERROR] An error occurred whilst sending a packet" +
                            " across a connection:");
                    e.printStackTrace();
                }
            }
        }, "Packet Sending Thread" /* name of the thread */);

        //start the asynchronous packet sending thread
        this.packetSendingThread.start();
    }

    /**
     * Public API method to be used when a developer wishes to send a {@code Packet}
     * across the parent {@code Connection}. This method adds the provided {@code Packet} object
     * to the packet queue. The packet will then be sent across the {@code Connection} asynchronously.
     *
     * @param packet {@code Packet} to send across the connection
     */
    public void sendPacket(Packet packet) {
        //add packet to queue
        this.packetQueue.add(packet);
    }

    /**
     * Method overriding the default `close()` implementation from {@link AutoCloseable}.
     * This method disables the constant sending of packets on the asynchronous
     * 'Packet Sending Thread' and waits for the thread to die.
     *
     * @throws InterruptedException thrown if an exception occurs when waiting for the thread to die.
     */
    @Override
    public void close() throws InterruptedException {
        //set loop-controlling variable to false in order to disable the while loop
        //in the packet sending thread
        this.sendingPackets.set(false);
        //wait for the packet sending thread to die
        this.packetSendingThread.join();
    }

}
