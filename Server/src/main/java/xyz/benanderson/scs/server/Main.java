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
        //create basic ServerBuilder without any additional configuration
        ServerBuilder serverBuilder = new ServerBuilder(ConfigurationWrapper.getInstance().getServerPort(),
                InetAddress.getByName(ConfigurationWrapper.getInstance().getServerAddress()));
        //build server and open camera
        try (Server server = serverBuilder.build();
             CameraViewer cameraViewer = new CameraViewer(Webcam.getDefault())) {
            while (true) {
                //attempt to capture camera image
                cameraViewer.captureImage().ifPresent(img -> {
                    //if successful in capturing an image, create a packet from the image
                    //and send it to all active connections
                    Packet packet = new MediaPacket(img);
                    server.getConnections().values().forEach(conn -> {
                        conn.getPacketSender().sendPacket(packet);
                    });
                });
            }
        }
    }

}
