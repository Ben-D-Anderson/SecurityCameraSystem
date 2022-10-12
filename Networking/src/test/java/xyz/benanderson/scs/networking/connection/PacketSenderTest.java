package xyz.benanderson.scs.networking.connection;

import org.junit.jupiter.api.Test;
import xyz.benanderson.scs.networking.Packet;
import xyz.benanderson.scs.networking.packets.TestPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class PacketSenderTest {

    @Test
    void testAddPacketToQueue() throws NoSuchFieldException, IllegalAccessException {
        PacketController packetControllerMock = mock(PacketController.class);
        Connection connectionMock = mock(Connection.class);
        doReturn(packetControllerMock).when(connectionMock).getPacketController();
        //so that the listening thread doesn't start - it isn't relevant to this test
        doReturn(false).when(connectionMock).isConnected();
        PacketSender packetSender = new PacketSender(connectionMock);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        Field packetQueueField = packetSender.getClass()
                .getDeclaredField("packetQueue");
        packetQueueField.setAccessible(true);

        Queue<Packet> packetQueue = (Queue<Packet>) packetQueueField.get(packetSender);
        assertEquals(0, packetQueue.size());
        packetSender.sendPacket(testPacket);
        assertEquals(1, packetQueue.size());
        assertEquals(testPacket, packetQueue.peek());

        try {
            packetSender.close();
        } catch (InterruptedException ignored) {}
    }

    @Test
    void testPacketSending() throws IOException, ClassNotFoundException {
        Connection localConnectionMock = mock(Connection.class);
        Socket localSocket, peerSocket;
        //create server on randomly assigned available port
        try (ServerSocket embeddedServer = new ServerSocket(0)) {
            //create socket connections from both sides
            localSocket = new Socket(embeddedServer.getInetAddress(), embeddedServer.getLocalPort());
            peerSocket = embeddedServer.accept();
        }
        doReturn(localSocket).when(localConnectionMock).getSocket();
        doCallRealMethod().when(localConnectionMock).isConnected();
        new ObjectOutputStream(peerSocket.getOutputStream());
        PacketController localPacketController = new PacketController(localConnectionMock);
        ObjectInputStream peerObjectInputStream = new ObjectInputStream(peerSocket.getInputStream());
        doReturn(localPacketController).when(localConnectionMock).getPacketController();
        PacketSender packetSender = new PacketSender(localConnectionMock);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        packetSender.sendPacket(testPacket);

        TestPacket receivedPacket = (TestPacket) peerObjectInputStream.readObject();
        try {
            localSocket.close();
            peerSocket.close();
        } catch (IOException ignored) {}

        assertNotNull(receivedPacket);
        assertEquals(testPacket.getTestData(), receivedPacket.getTestData());
    }

}
