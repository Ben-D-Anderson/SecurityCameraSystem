package xyz.benanderson.scs.client;

import lombok.Getter;
import xyz.benanderson.scs.networking.Validation;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

public class ConnectionDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonConnect;
    private JButton buttonCancel;
    @Getter
    private JTextField addressInput;
    @Getter
    private JTextField portInput;
    @Getter
    private JTextField usernameInput;
    @Getter
    private JPasswordField passwordInput;

    public ConnectionDialog() {
        setContentPane(contentPane);
        setModal(true);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(buttonConnect);
        buttonConnect.addActionListener(e -> onClickConnect());
        buttonCancel.addActionListener(e -> onClickCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClickCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onClickCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setVisible(true);
    }

    private void onClickConnect() {
        int validatedPort;
        try {
            validatedPort = Validation.parsePort(getPortInput().getText());
        } catch (Validation.ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Main.attemptConnect(getAddressInput().getText(), validatedPort,
                    getUsernameInput().getText(), new String(getPasswordInput().getPassword()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
    }

    private void onClickCancel() {
        dispose();
    }

}
