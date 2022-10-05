package xyz.benanderson.scs.networking.connection;

import lombok.AccessLevel;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.benanderson.scs.networking.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PacketControllerTest {

    private PacketController packetController;
    private Socket connectionSocket, peerSocket;

    //method runs before each test method in this class
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

    //method runs after each test method in this class
    @AfterEach
    void destroyPacketController() {
        try {
            packetController.close();
            connectionSocket.close();
            peerSocket.close();
        } catch (IOException ignored) {}
    }

    //test packet type only used in testing to confirm
    //data is correctly transmitted and received
    static class TestPacket extends Packet {
        @Getter(AccessLevel.PUBLIC)
        private final int testData;

        public TestPacket(int testData) {
            super(TestPacket.class);
            this.testData = testData;
        }
    }

    @Test
    void testWritePacket() throws IOException, ClassNotFoundException {
        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        packetController.writePacketToSocket(testPacket);

        Packet receivedPacket;
        try (ObjectInputStream peerInputStream = new ObjectInputStream(peerSocket.getInputStream())) {
            receivedPacket = (Packet) peerInputStream.readObject();
        }
        assertEquals(testPacket.getType(), receivedPacket.getType());
        assertEquals(testPacket.getTestData(), ((TestPacket) receivedPacket).getTestData());
    }

    @Test
    void testReadPacket() throws IOException, ClassNotFoundException {
        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        try (ObjectOutputStream peerOutputStream = new ObjectOutputStream(peerSocket.getOutputStream())) {
            peerOutputStream.writeObject(testPacket);
        }

        Packet receivedPacket = packetController.readPacketFromSocket();
        assertEquals(testPacket.getType(), receivedPacket.getType());
        assertEquals(testPacket.getTestData(), ((TestPacket) receivedPacket).getTestData());
    }

}
