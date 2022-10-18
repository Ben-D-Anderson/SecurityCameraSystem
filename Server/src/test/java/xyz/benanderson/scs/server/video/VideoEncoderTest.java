package xyz.benanderson.scs.server.video;

import com.github.sarxos.webcam.Webcam;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.model.Rational;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public class VideoEncoderTest {

    public static void main(String[] args) throws IOException {
        CameraViewer cameraViewer = new CameraViewer(Webcam.getDefault());
        VideoFileManager videoFileManager = new VideoFileManager(Paths.get("/home/ben/Videos/"), Duration.ofMinutes(1));
        VideoEncoder videoEncoder = new VideoEncoder(videoFileManager);
        Path currentSaveFile = videoFileManager.getCurrentSaveFile().get();
        AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(currentSaveFile.toFile(), 9);
        long start = Instant.now().getEpochSecond();
        for (int i = 0; i < 100; i++) {
            encoder.encodeImage(cameraViewer.captureImage().get());
        }
        long end = Instant.now().getEpochSecond();
        System.out.println(100d / (end - start));
        encoder.finish();
    }

}
