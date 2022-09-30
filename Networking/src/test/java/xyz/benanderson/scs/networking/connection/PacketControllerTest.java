package xyz.benanderson.scs.networking.connection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.benanderson.scs.networking.Packet;
import xyz.benanderson.scs.networking.packet.TestPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PacketControllerTest {

    private PacketController packetController;
    private Socket connectionSocket, peerSocket;

    @BeforeEach
    void setupPacketController() throws IOException {
        //create server on randomly assigned available port
        try (ServerSocket embeddedServer = new ServerSocket(0)) {
            //create socket connections from both sides
            connectionSocket = new Socket(embeddedServer.getInetAddress(), embeddedServer.getLocalPort());
            peerSocket = embeddedServer.accept();
        }
        //create a mock Connection object to return connectionSocket when Connection#getSocket is called
        Connection connection = mock(Connection.class);
        doReturn(connectionSocket).when(connection).getSocket();

        //instantiate PacketController with connection
        packetController = new PacketController(connection);
    }

    @AfterEach
    void destroyPacketController() {
        try {
            packetController.close();
            connectionSocket.close();
            peerSocket.close();
        } catch (IOException ignored) {}
    }

    @Test
    void testWritePacket() throws IOException, ClassNotFoundException {
        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);

        packetController.writePacketToSocket(testPacket);
        Packet receivedPacket = (Packet) new ObjectInputStream(peerSocket.getInputStream()).readObject();

        assertEquals(testPacket.getType(), receivedPacket.getType());
        assertEquals(testPacket.getTestData(), ((TestPacket) receivedPacket).getTestData());
    }

}
