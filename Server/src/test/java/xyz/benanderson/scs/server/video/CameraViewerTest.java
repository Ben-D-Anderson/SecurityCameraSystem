package xyz.benanderson.scs.server.video;

import com.github.sarxos.webcam.Webcam;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class CameraViewerTest {

    private void manualTest() {
        JFrame f = new JFrame();
        f.setVisible(true);
        f.setSize(800, 480);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel();
        f.getContentPane().add(label);

        try (CameraViewer cameraViewer = new CameraViewer(Webcam.getDefault())) {
            while (true) {
                Optional<BufferedImage> imageOptional = cameraViewer.captureImage();
                if (imageOptional.isEmpty()) continue;
                BufferedImage image = imageOptional.get();
                if (f.getWidth() != image.getWidth() || f.getHeight() != image.getHeight())
                    f.setSize(image.getWidth(), image.getHeight());
                label.setIcon(new ImageIcon(image));
            }
        }
    }

}
