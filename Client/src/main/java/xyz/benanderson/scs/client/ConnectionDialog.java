package xyz.benanderson.scs.client;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.Getter;
import xyz.benanderson.scs.networking.Validation;

import javax.swing.*;
import java.awt.*;
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
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void onClickConnect() {
        int validatedPort;
        try {
            validatedPort = Validation.parsePort(getPortInput().getText());
        } catch (Validation.ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Main.attemptConnect(getAddressInput().getText(), validatedPort,
                    getUsernameInput().getText(), new String(getPasswordInput().getPassword()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
    }

    private void onClickCancel() {
        dispose();
    }

    {
        $$$setupUI$$$();
    }

    /**
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1,
                new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1,
                new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK |
                        GridConstraints.SIZEPOLICY_CAN_GROW, 1,
                null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 3,
                new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1,
                1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK |
                        GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK |
                        GridConstraints.SIZEPOLICY_CAN_GROW, null,
                null, null, 0, false));
        buttonConnect = new JButton();
        buttonConnect.setText("Connect");
        panel2.add(buttonConnect, new GridConstraints(1, 0,
                1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK |
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(1, 2, 1,
                1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK |
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(1, 1, 1,
                1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, 1, null,
                null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                GridConstraints.SIZEPOLICY_WANT_GROW, null, null,
                null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(8, 1,
                new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1,
                1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK |
                        GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK |
                        GridConstraints.SIZEPOLICY_CAN_GROW, null,
                null, null, 0, false));
        addressInput = new JTextField();
        panel3.add(addressInput, new GridConstraints(1, 0, 1,
                1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(150, -1), null,
                0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Camera Address");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Camera Port");
        panel3.add(label2, new GridConstraints(2, 0, 1,
                1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        portInput = new JTextField();
        panel3.add(portInput, new GridConstraints(3, 0, 1,
                1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(150, -1), null,
                0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Username");
        panel3.add(label3, new GridConstraints(4, 0, 1,
                1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        usernameInput = new JTextField();
        panel3.add(usernameInput, new GridConstraints(5, 0, 1,
                1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(150, -1), null,
                0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Password");
        panel3.add(label4, new GridConstraints(6, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        passwordInput = new JPasswordField();
        panel3.add(passwordInput, new GridConstraints(7, 0, 1,
                1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(150, -1), null,
                0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
