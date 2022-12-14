package xyz.benanderson.scs.server;

import org.junit.jupiter.api.Test;
import xyz.benanderson.scs.networking.connection.Connection;
import xyz.benanderson.scs.networking.packets.DisconnectPacket;
import xyz.benanderson.scs.networking.packets.InfoPacket;
import xyz.benanderson.scs.networking.packets.LoginPacket;
import xyz.benanderson.scs.server.account.User;
import xyz.benanderson.scs.server.account.UserManager;
import xyz.benanderson.scs.server.networking.Server;
import xyz.benanderson.scs.server.networking.ServerBuilder;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ServerLoginTest {

    private UserManager mockUserManager() {
        User user = User.fromPlainTextPassword("testUsername",
                LoginPacket.hashPassword("testPassword"),
                false);
        UserManager userManager = mock(UserManager.class);
        doReturn(Optional.of(user)).when(userManager).getUser("testUsername");
        try {
            doNothing().when(userManager).createUser(any(User.class));
            doNothing().when(userManager).deleteUser(anyString());
        } catch (Exception ignored) {}
        return userManager;
    }

    @Test
    public void testIncorrectPassword() throws UnknownHostException {
        CompletableFuture<String> result = new CompletableFuture<>();
        ServerBuilder serverBuilder = new ServerBuilder(0, InetAddress.getLocalHost());
        Main.addAuthenticationToServerBuilder(serverBuilder, mockUserManager());
        try (Server server = serverBuilder.build()) {
            Socket socket = new Socket(InetAddress.getLocalHost(), server.getPort());
            Connection connection = new Connection(socket);
            connection.getPacketListener().addCallback(InfoPacket.class, infoPacket -> {
                result.complete(infoPacket.getInfo());
            });
            connection.getPacketListener().addCallback(DisconnectPacket.class, disconnectPacket -> {
                result.complete(disconnectPacket.getReason());
            });
            connection.getPacketSender().sendPacket(
                    LoginPacket.fromPlainTextPassword(
                            "testUsername", "incorrectPassword"
                    )
            );
            assertEquals("Incorrect Credentials", result.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testIncorrectUsername() throws UnknownHostException {
        CompletableFuture<String> result = new CompletableFuture<>();
        ServerBuilder serverBuilder = new ServerBuilder(0, InetAddress.getLocalHost());
        Main.addAuthenticationToServerBuilder(serverBuilder, mockUserManager());
        try (Server server = serverBuilder.build()) {
            Socket socket = new Socket(InetAddress.getLocalHost(), server.getPort());
            Connection connection = new Connection(socket);
            connection.getPacketListener().addCallback(InfoPacket.class, infoPacket -> {
                result.complete(infoPacket.getInfo());
            });
            connection.getPacketListener().addCallback(DisconnectPacket.class, disconnectPacket -> {
                result.complete(disconnectPacket.getReason());
            });
            connection.getPacketSender().sendPacket(
                    LoginPacket.fromPlainTextPassword(
                            "testUsername", "incorrectPassword"
                    )
            );
            assertEquals("Incorrect Credentials", result.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSuccessfulLogin() throws UnknownHostException {
        CompletableFuture<String> result = new CompletableFuture<>();
        ServerBuilder serverBuilder = new ServerBuilder(0, InetAddress.getLocalHost());
        Main.addAuthenticationToServerBuilder(serverBuilder, mockUserManager());
        try (Server server = serverBuilder.build()) {
            Socket socket = new Socket(InetAddress.getLocalHost(), server.getPort());
            Connection connection = new Connection(socket);
            connection.getPacketListener().addCallback(InfoPacket.class, infoPacket -> {
                result.complete(infoPacket.getInfo());
            });
            connection.getPacketListener().addCallback(DisconnectPacket.class, disconnectPacket -> {
                result.complete(disconnectPacket.getReason());
            });
            connection.getPacketSender().sendPacket(
                    LoginPacket.fromPlainTextPassword(
                            "testUsername", "testPassword"
                    )
            );
            assertEquals("Correct Credentials", result.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
