package xyz.benanderson.scs.server.video;

import com.github.sarxos.webcam.Webcam;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class CameraViewerTest {

    public static void main(String[] args) {
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
                image = horizontalFlip(image);
                label.setIcon(new ImageIcon(image));
            }
        }
    }

    public static BufferedImage horizontalFlip(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage flippedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = flippedImage.createGraphics();
        g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
        g.dispose();
        return flippedImage;
    }


}
