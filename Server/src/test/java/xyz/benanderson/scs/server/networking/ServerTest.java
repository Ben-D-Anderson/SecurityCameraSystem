package xyz.benanderson.scs.server.networking;

import org.junit.jupiter.api.Test;
import xyz.benanderson.scs.networking.connection.Connection;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ServerTest {

    @Test
    public void testServer() throws UnknownHostException {
        CompletableFuture<String> shutdownCompletableFuture = new CompletableFuture<>();
        Consumer<Server> serverShutdownListener =
                serv -> shutdownCompletableFuture.complete("Shutdown");
        CompletableFuture<String> connectCompletableFuture = new CompletableFuture<>();
        BiConsumer<Connection, Server> connectListener =
                (con, serv) -> connectCompletableFuture.complete("Connected");
        CompletableFuture<String> disconnectCompletableFuture = new CompletableFuture<>();
        BiConsumer<Connection, Server> disconnectListener =
                (con, serv) -> disconnectCompletableFuture.complete("Disconnected");

        ServerBuilder serverBuilder = new ServerBuilder(0, InetAddress.getLocalHost())
                .onServerShutdown(serverShutdownListener)
                .onClientConnect(connectListener)
                .onClientDisconnect(disconnectListener);

        try (Server server = serverBuilder.build()) {
            Socket socket = new Socket(InetAddress.getLocalHost(), server.getPort());
            Connection connection = new Connection(socket);

            assertEquals("Connected", connectCompletableFuture.get());
            assertFalse(disconnectCompletableFuture.isDone());
            assertFalse(shutdownCompletableFuture.isDone());

            connection.close();
            assertEquals("Connected", connectCompletableFuture.get());
            assertEquals("Disconnected", disconnectCompletableFuture.get());
            assertFalse(shutdownCompletableFuture.isDone());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            assertEquals("Connected", connectCompletableFuture.get());
            assertEquals("Disconnected", disconnectCompletableFuture.get());
            assertEquals("Shutdown", shutdownCompletableFuture.get());
        } catch (InterruptedException | ExecutionException ignored) {}
    }

}
