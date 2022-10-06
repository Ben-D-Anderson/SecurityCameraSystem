package xyz.benanderson.scs.networking.connection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.benanderson.scs.networking.Packet;
import xyz.benanderson.scs.networking.packets.TestPacket;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PacketListenerTest {

    private PacketListener packetListener;
    private Connection connectionMock;

    @BeforeEach
    void setupPacketListener() {
        PacketController packetControllerMock = mock(PacketController.class);
        connectionMock = mock(Connection.class);
        doReturn(packetControllerMock).when(connectionMock).getPacketController();
    }

    @AfterEach
    void destroyPacketListener() {
        try {
            packetListener.close();
        } catch (Exception ignored) {}
    }

    @Test
    void testNoCallbacks() throws NoSuchMethodException {
        //so that the listening thread doesn't start - it isn't relevant to this test
        doReturn(false).when(connectionMock).isConnected();
        packetListener = new PacketListener(connectionMock);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        Method runCallbacksMethod = packetListener.getClass()
                .getDeclaredMethod("runCallbacks", Packet.class);
        runCallbacksMethod.setAccessible(true);

        assertDoesNotThrow(() -> runCallbacksMethod.invoke(packetListener, testPacket));
    }

    @Test
    void testSingleCallbackRun() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //so that the listening thread doesn't start - it isn't relevant to this test
        doReturn(false).when(connectionMock).isConnected();
        packetListener = new PacketListener(connectionMock);

        AtomicBoolean callbackRan = new AtomicBoolean(false);
        Consumer<TestPacket> callback = testPacket -> callbackRan.set(true);
        packetListener.addCallback(TestPacket.class, callback);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        Method runCallbacksMethod = packetListener.getClass()
                .getDeclaredMethod("runCallbacks", Packet.class);
        runCallbacksMethod.setAccessible(true);
        runCallbacksMethod.invoke(packetListener, testPacket);

        assertTrue(callbackRan.get());
    }

    @Test
    void testSingleCallbackNoRun() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //so that the listening thread doesn't start - it isn't relevant to this test
        doReturn(false).when(connectionMock).isConnected();
        packetListener = new PacketListener(connectionMock);

        AtomicBoolean callbackRan = new AtomicBoolean(false);
        Consumer<Packet> callback = testPacket -> callbackRan.set(true);
        packetListener.addCallback(Packet.class, callback);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        Method runCallbacksMethod = packetListener.getClass()
                .getDeclaredMethod("runCallbacks", Packet.class);
        runCallbacksMethod.setAccessible(true);
        runCallbacksMethod.invoke(packetListener, testPacket);

        assertFalse(callbackRan.get());
    }

    @Test
    void testMultipleCallbacksRunOnlyOne() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //so that the listening thread doesn't start - it isn't relevant to this test
        doReturn(false).when(connectionMock).isConnected();
        packetListener = new PacketListener(connectionMock);

        AtomicBoolean callbackOneRan = new AtomicBoolean(false);
        Consumer<TestPacket> callbackOne = testPacket -> callbackOneRan.set(true);
        AtomicBoolean callbackTwoRan = new AtomicBoolean(false);
        Consumer<Packet> callbackTwo = testPacket -> callbackTwoRan.set(true);
        packetListener.addCallback(TestPacket.class, callbackOne);
        packetListener.addCallback(Packet.class, callbackTwo);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        Method runCallbacksMethod = packetListener.getClass()
                .getDeclaredMethod("runCallbacks", Packet.class);
        runCallbacksMethod.setAccessible(true);
        runCallbacksMethod.invoke(packetListener, testPacket);

        assertTrue(callbackOneRan.get());
        assertFalse(callbackTwoRan.get());
    }

    @Test
    void testMultipleCallbacksRunBoth() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //so that the listening thread doesn't start - it isn't relevant to this test
        doReturn(false).when(connectionMock).isConnected();
        packetListener = new PacketListener(connectionMock);

        AtomicBoolean callbackOneRan = new AtomicBoolean(false);
        Consumer<TestPacket> callbackOne = testPacket -> callbackOneRan.set(true);
        AtomicBoolean callbackTwoRan = new AtomicBoolean(false);
        Consumer<TestPacket> callbackTwo = testPacket -> callbackTwoRan.set(true);
        packetListener.addCallback(TestPacket.class, callbackOne);
        packetListener.addCallback(TestPacket.class, callbackTwo);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        Method runCallbacksMethod = packetListener.getClass()
                .getDeclaredMethod("runCallbacks", Packet.class);
        runCallbacksMethod.setAccessible(true);
        runCallbacksMethod.invoke(packetListener, testPacket);

        assertTrue(callbackOneRan.get());
        assertTrue(callbackTwoRan.get());
    }

    @Test
    void testPacketListening() throws IOException, ExecutionException, InterruptedException {
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
        ObjectOutputStream peerObjectOutputStream = new ObjectOutputStream(peerSocket.getOutputStream());
        PacketController localPacketController = new PacketController(localConnectionMock);
        doReturn(localPacketController).when(localConnectionMock).getPacketController();
        packetListener = new PacketListener(localConnectionMock);

        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        Consumer<TestPacket> callback = testPacket -> {
            //deactivate the listening thread
            completableFuture.complete("Callback Ran Successfully");
            try {
                localSocket.close();
                peerSocket.close();
            } catch (IOException ignored) {}
        };
        packetListener.addCallback(TestPacket.class, callback);

        int testData = new Random().nextInt();
        TestPacket testPacket = new TestPacket(testData);
        peerObjectOutputStream.writeObject(testPacket);

        assertEquals("Callback Ran Successfully", completableFuture.get());
    }

}
