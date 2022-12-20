package xyz.benanderson.scs.client;

import javax.swing.*;
import java.awt.event.*;

public class ConnectionDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonConnect;
    private JButton buttonCancel;
    private JTextField addressInput;
    private JTextField portInput;
    private JTextField usernameInput;
    private JPasswordField passwordInput;

    public ConnectionDialog() {
        setContentPane(contentPane);
        setModal(true);
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
    }

    private void onClickConnect() {
        // add your code here
        dispose();
    }

    private void onClickCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        ConnectionDialog dialog = new ConnectionDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
