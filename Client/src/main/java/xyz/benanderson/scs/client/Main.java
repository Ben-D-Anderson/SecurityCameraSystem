package xyz.benanderson.scs.client;

import xyz.benanderson.scs.networking.connection.Connection;
import xyz.benanderson.scs.networking.packets.DisconnectPacket;
import xyz.benanderson.scs.networking.packets.InfoPacket;
import xyz.benanderson.scs.networking.packets.LoginPacket;
import xyz.benanderson.scs.networking.packets.MediaPacket;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;

public class Main {

    private static ClientGUI clientGUI;
    private static Connection connection;

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        JFrame frame = new JFrame();
        frame.setVisible(true);
        frame.setTitle("Security Camera Client");
        frame.setSize(1280, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        clientGUI = new ClientGUI();
        clientGUI.getSettingsButton().setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        clientGUI.getSettingsButton().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clientGUI.getConnectButton().setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        clientGUI.getConnectButton().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clientGUI.getConnectButton().addActionListener(event -> new ConnectionDialog());

        frame.setContentPane(clientGUI.getContentPane());
    }

    public static void attemptConnect(String address, int port, String username, String password) throws IOException {
        if (connection != null && connection.isConnected()) {
            try {
                connection.close();
            } catch (Exception ignored) {}
        }

        Socket socket = new Socket(address, port);
        connection = new Connection(socket);
        connection.getPacketListener().addCallback(MediaPacket.class, mediaPacket -> {
            EventQueue.invokeLater(() -> clientGUI.getVideoComponent()
                    .setIcon(new ImageIcon(mediaPacket.getMediaFrame())));
        });
        connection.getPacketListener().addCallback(InfoPacket.class, infoPacket -> {
            JOptionPane.showMessageDialog(clientGUI.getContentPane(), infoPacket.getInfo(),
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        });
        connection.getPacketListener().addCallback(DisconnectPacket.class, disconnectPacket -> {
            JOptionPane.showMessageDialog(clientGUI.getContentPane(),
                    "Disconnected From Camera." + System.lineSeparator() + "Reason: " + disconnectPacket.getReason(),
                    "Disconnected", JOptionPane.WARNING_MESSAGE);
        });
        LoginPacket loginPacket = LoginPacket.fromPlainTextPassword(username, password);
        connection.getPacketSender().sendPacket(loginPacket);
    }

}
