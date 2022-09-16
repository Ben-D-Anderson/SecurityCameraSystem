package xyz.benanderson.scs.networking.connection;

import xyz.benanderson.scs.networking.Packet;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacketSender implements AutoCloseable {

    private final Queue<Packet> packetQueue;
    private final Thread packetSendingThread;
    private final AtomicBoolean sendingPackets = new AtomicBoolean(true);

    //todo comments & javadoc
    public PacketSender(Connection connection) {
        this.packetQueue = new ConcurrentLinkedQueue<>();
        this.packetSendingThread = new Thread(() -> {
            while (sendingPackets.get()) {
                Packet packetToSend = packetQueue.peek();
                if (packetToSend == null) continue;
                try {
                    connection.getPacketController().writePacketToSocket(packetToSend);
                    packetQueue.remove();
                } catch (IOException e) {
                    //todo log error
                }
            }
        }, "Packet Sending Thread");
        this.packetSendingThread.start();
    }

    @Override
    public void close() throws Exception {
        this.sendingPackets.set(false);
        this.packetSendingThread.join();
    }

}
