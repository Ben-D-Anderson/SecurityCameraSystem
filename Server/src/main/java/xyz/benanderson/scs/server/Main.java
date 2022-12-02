package xyz.benanderson.scs.server;

import com.github.sarxos.webcam.Webcam;
import xyz.benanderson.scs.networking.Packet;
import xyz.benanderson.scs.networking.packets.MediaPacket;
import xyz.benanderson.scs.server.configuration.ConfigurationWrapper;
import xyz.benanderson.scs.server.networking.Server;
import xyz.benanderson.scs.server.networking.ServerBuilder;
import xyz.benanderson.scs.server.video.CameraViewer;

import java.io.IOException;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerBuilder serverBuilder = new ServerBuilder(ConfigurationWrapper.getInstance().getServerPort(),
                InetAddress.getByName(ConfigurationWrapper.getInstance().getServerAddress()));
        try (Server server = serverBuilder.build();
             CameraViewer cameraViewer = new CameraViewer(Webcam.getDefault())) {
            while (true) {
                cameraViewer.captureImage().ifPresent(img -> {
                    Packet packet = new MediaPacket(img);
                    server.getConnections().values().forEach(conn -> {
                        conn.getPacketSender().sendPacket(packet);
                    });
                });
            }
        }
    }

}
