package xyz.benanderson.scs.client;

import xyz.benanderson.scs.networking.connection.Connection;
import xyz.benanderson.scs.networking.packets.DisconnectPacket;
import xyz.benanderson.scs.networking.packets.InfoPacket;
import xyz.benanderson.scs.networking.packets.LoginPacket;
import xyz.benanderson.scs.networking.packets.MediaPacket;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;

public class Main {

    private static ClientGUI clientGUI;

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        JFrame f = new JFrame();
        f.setVisible(true);
        f.setSize(1280, 800);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);

        clientGUI = new ClientGUI();
        clientGUI.getSettingsButton().setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        clientGUI.getSettingsButton().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clientGUI.getConnectButton().setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        clientGUI.getConnectButton().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        f.setContentPane(clientGUI.getContentPane());

        Socket socket = new Socket("127.0.0.1", 8192);
        Connection connection = new Connection(socket);
        connection.getPacketListener().addCallback(MediaPacket.class, mediaPacket -> {
            clientGUI.getVideoComponent().setIcon(new ImageIcon(mediaPacket.getMediaFrame()));
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
