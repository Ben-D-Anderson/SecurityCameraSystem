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
import xyz.benanderson.scs.server.video.VideoEncoder;
import xyz.benanderson.scs.server.video.VideoFileManager;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerBuilder serverBuilder = new ServerBuilder(ConfigurationWrapper.getInstance().getServerPort(),
                InetAddress.getByName(ConfigurationWrapper.getInstance().getServerAddress()));
        UserManager userManager = new MultiFileUserManager();
        addAuthenticationToServerBuilder(serverBuilder, userManager);

        try {
            userManager.createUser(ConfigurationWrapper.getInstance().getDefaultUser());
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
        //setup server components
        ImageIO.setUseCache(false);
        try (Server server = serverBuilder.build();
             CameraViewer cameraViewer = new CameraViewer(Webcam.getDefault())) {
            VideoFileManager videoFileManager = new VideoFileManager(
                    ConfigurationWrapper.getInstance().getVideoSaveDirectory(),
                    ConfigurationWrapper.getInstance().getVideoDuration()
            );
            VideoEncoder videoEncoder = new VideoEncoder(videoFileManager);
            startVideoEncodingScheduledJob(videoFileManager, videoEncoder);
            System.out.println("[INFO] Server Started Successfully");
            while (true) {
                //attempt to capture camera image
                cameraViewer.captureImage().ifPresent(img -> {
                    videoEncoder.appendToStream(img, System.currentTimeMillis());
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


    private final static Set<Path> videosBeingEncoded = Collections.synchronizedSet(new HashSet<>());

    private static void startVideoEncodingScheduledJob(VideoFileManager videoFileManager, VideoEncoder videoEncoder) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            Optional<Path> currentSaveFile = videoFileManager.getCurrentSaveFile();
            if (currentSaveFile.isEmpty()) {
                System.err.println("[ERROR] An error occurred when accessing the current save file.");
                return;
            }
            try (Stream<Path> pathStream = Files.list(videoFileManager.getSaveDirectory())) {
                pathStream.filter(path -> path.toString().endsWith(".crms"))
                        .filter(path -> !path.equals(currentSaveFile.get()))
                        .filter(path -> !videosBeingEncoded.contains(path))
                        .forEach(path -> {
                            videosBeingEncoded.add(path);
                            videoEncoder.processRawMediaSave(path);
                        });
            } catch (IOException e) {
                System.err.println("[ERROR] An error occurred when searching for raw media save files to encode into videos.");
            }
        }, 1, 1, TimeUnit.SECONDS);
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
                System.out.println("[INFO] User '" + loginPacket.getUsername() + "' logged in successfully.");
            });
        });
        //remove connection from loggedInUsers on disconnect - prevents infinite memory usage
        serverBuilder.onClientDisconnect((connection, server) -> {
            loggedInUsers.remove(connection.getId());
        });
    }

}
