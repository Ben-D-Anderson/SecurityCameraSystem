package xyz.benanderson.scs.client;

import xyz.benanderson.scs.networking.connection.Connection;
import xyz.benanderson.scs.networking.packets.DisconnectPacket;
import xyz.benanderson.scs.networking.packets.InfoPacket;
import xyz.benanderson.scs.networking.packets.LoginPacket;
import xyz.benanderson.scs.networking.packets.MediaPacket;

import javax.swing.*;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame();
        f.setVisible(true);
        f.setSize(800, 480);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel();
        f.getContentPane().add(label);

        Socket socket = new Socket("127.0.0.1", 8192);
        Connection connection = new Connection(socket);
        connection.getPacketListener().addCallback(MediaPacket.class, mediaPacket -> {
            label.setIcon(new ImageIcon(mediaPacket.getMediaFrame()));
        });
        connection.getPacketListener().addCallback(InfoPacket.class, infoPacket -> {
            System.out.println("[INFO] " + infoPacket.getInfo());
        });
        connection.getPacketListener().addCallback(DisconnectPacket.class, disconnectPacket -> {
            System.out.println("[DISCONNECTED] Reason: " + disconnectPacket.getReason());
            f.dispose();
        });
        Thread.sleep(5000);
        LoginPacket loginPacket = LoginPacket.fromPlainTextPassword("testUsername", "testPassword");
        connection.getPacketSender().sendPacket(loginPacket);
    }

}
