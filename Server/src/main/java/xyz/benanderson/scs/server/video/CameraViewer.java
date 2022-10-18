package xyz.benanderson.scs.server.video;

import com.github.sarxos.webcam.Webcam;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class CameraViewer implements AutoCloseable {

    private final Webcam camera;

    public CameraViewer(Webcam camera) {
        this.camera = camera;
        this.camera.open();
    }

    public Optional<BufferedImage> captureImage() {
        return Optional.ofNullable(camera.getImage());
    }

    @Override
    public void close() {
        camera.close();
    }
}
