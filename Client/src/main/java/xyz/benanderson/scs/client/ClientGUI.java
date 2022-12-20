package xyz.benanderson.scs.client;

import lombok.Getter;

import javax.swing.*;

public class ClientGUI {
    @Getter
    private JPanel contentPane;
    @Getter
    private JPanel cameraPane;
    @Getter
    private JButton settingsButton;
    @Getter
    private JButton connectButton;
    @Getter
    private JLabel videoComponent;
}
