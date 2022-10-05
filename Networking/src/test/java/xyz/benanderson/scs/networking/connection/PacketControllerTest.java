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

    private Connection localConnection, peerConnection;

    //method runs before each test method in this class
    @BeforeEach
    void setupPacketController() throws IOException {
        Socket localSocket, peerSocket;
        //create server on randomly assigned available port
        try (ServerSocket embeddedServer = new ServerSocket(0)) {
            //create socket connections from both sides
            localSocket = new Socket(embeddedServer.getInetAddress(), embeddedServer.getLocalPort());
            peerSocket = embeddedServer.accept();
        }
        //create a mock Connection object to return localSocket when Connection#getSocket is called
        Connection localConnection = mock(Connection.class);
        doReturn(localSocket).when(localConnection).getSocket();
        this.localConnection = localConnection;

        //create a mock Connection object to return peerSocket when Connection#getSocket is called
        Connection peerConnection = mock(Connection.class);
        doReturn(peerSocket).when(peerConnection).getSocket();
        this.peerConnection = peerConnection;
    }

    //method runs after each test method in this class
    @AfterEach
    void destroyPacketController() {
        try {
            localConnection.getSocket().close();
            peerConnection.getSocket().close();
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
        //instantiated to stop the PacketController from hanging
        ObjectOutputStream peerOutputStream = new ObjectOutputStream(peerConnection.getSocket().getOutputStream());
        PacketController localPacketController = new PacketController(localConnection);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        localPacketController.writePacketToSocket(testPacket);

        Packet receivedPacket;
        try (ObjectInputStream peerInputStream = new ObjectInputStream(peerConnection.getSocket().getInputStream())) {
            receivedPacket = (Packet) peerInputStream.readObject();
        }
        peerOutputStream.close();
        localPacketController.close();

        assertNotNull(receivedPacket);
        assertEquals(testPacket.getType(), receivedPacket.getType());
        assertEquals(testPacket.getTestData(), ((TestPacket) receivedPacket).getTestData());
    }

    @Test
    void testReadPacket() throws IOException, ClassNotFoundException {
        //instantiated to stop the PacketController from hanging
        ObjectOutputStream peerOutputStream = new ObjectOutputStream(peerConnection.getSocket().getOutputStream());
        PacketController localPacketController = new PacketController(localConnection);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        peerOutputStream.writeObject(testPacket);

        Packet receivedPacket = localPacketController.readPacketFromSocket();
        peerOutputStream.close();
        localPacketController.close();

        assertEquals(testPacket.getType(), receivedPacket.getType());
        assertEquals(testPacket.getTestData(), ((TestPacket) receivedPacket).getTestData());
    }

}
