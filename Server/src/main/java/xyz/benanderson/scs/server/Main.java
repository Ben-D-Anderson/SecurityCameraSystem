package xyz.benanderson.scs.server;

import com.github.sarxos.webcam.Webcam;
import xyz.benanderson.scs.networking.Packet;
import xyz.benanderson.scs.networking.packets.DisconnectPacket;
import xyz.benanderson.scs.networking.packets.InfoPacket;
import xyz.benanderson.scs.networking.packets.LoginPacket;
import xyz.benanderson.scs.networking.packets.MediaPacket;
import xyz.benanderson.scs.server.account.User;
import xyz.benanderson.scs.server.account.UserManager;
import xyz.benanderson.scs.server.account.managers.MultiFileUserManager;
import xyz.benanderson.scs.server.configuration.ConfigurationWrapper;
import xyz.benanderson.scs.server.networking.Server;
import xyz.benanderson.scs.server.networking.ServerBuilder;
import xyz.benanderson.scs.server.video.CameraViewer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerBuilder serverBuilder = new ServerBuilder(ConfigurationWrapper.getInstance().getServerPort(),
                InetAddress.getByName(ConfigurationWrapper.getInstance().getServerAddress()));
        UserManager userManager = new MultiFileUserManager();
        addAuthenticationToServerBuilder(serverBuilder, userManager);

        //todo demo test then remove / or ship with preset creds / or allow set creds in config
        try {
            userManager.createUser(User.fromPlainTextPassword("testUsername",
                    LoginPacket.hashPassword("testPassword"), false));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //build server and open camera
        try {
            if (Webcam.getDefault() == null) {
                System.err.println("[ERROR] No Camera Detected");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error Occurred Accessing Camera");
            e.printStackTrace();
            System.exit(1);
        }
        try (Server server = serverBuilder.build();
             CameraViewer cameraViewer = new CameraViewer(Webcam.getDefault())) {
            System.out.println("[INFO] Server Started Successfully");
            while (true) {
                //attempt to capture camera image
                cameraViewer.captureImage().ifPresent(img -> {
                    //if successful in capturing an image, create a packet from the image
                    //and send it to all active connections
                    Packet packet = new MediaPacket(img);
                    server.getConnections().values().forEach(conn -> {
                        if (loggedInUsers.contains(conn.getId()))
                            conn.getPacketSender().sendPacket(packet);
                    });
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final static Set<UUID> loggedInUsers = Collections.synchronizedSet(new HashSet<>());

    static void addAuthenticationToServerBuilder(ServerBuilder serverBuilder, UserManager userManager) {
        //add `LoginPacket` listener on connect
        serverBuilder.onClientConnect((connection, server) -> {
            connection.getPacketListener().addCallback(LoginPacket.class, loginPacket -> {
                Optional<User> userOptional = userManager.getUser(
                        //remove all '..' to protect against directory traversal vulnerability
                        loginPacket.getUsername().replace("..", "")
                );
                //check if user exists with given username
                if (userOptional.isEmpty()) {
                    //username not found in users
                    connection.getPacketSender().sendPacket(new DisconnectPacket("Incorrect Credentials"));
                    try {
                        Thread.sleep(200);
                        connection.close();
                    } catch (Exception ignored) {}
                    return;
                }
                User expectedUser = userOptional.get();
                //check if password is correct
                if (!expectedUser.getHashedPassword().equals(
                        LoginPacket.hashPassword(loginPacket.getHashedPassword()))) {
                    //password is wrong
                    connection.getPacketSender().sendPacket(new DisconnectPacket("Incorrect Credentials"));
                    try {
                        Thread.sleep(200);
                        connection.close();
                    } catch (Exception ignored) {}
                    return;
                }
                //login successful
                loggedInUsers.add(connection.getId());
                connection.getPacketSender().sendPacket(new InfoPacket("Correct Credentials"));
            });
        });
        //remove connection from loggedInUsers on disconnect - prevents infinite memory usage
        serverBuilder.onClientDisconnect((connection, server) -> {
            loggedInUsers.remove(connection.getId());
        });
    }

}
