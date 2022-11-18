package xyz.benanderson.scs.server.video;

import com.github.sarxos.webcam.Webcam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

public class VideoEncoderTest {

    private void manualTest() {
        //instantiate CameraViewer instance
        try (CameraViewer cameraViewer = new CameraViewer(Webcam.getDefault())) {
            //instantiate VideoFileManager instance to define
            //the video save directory and video duration
            VideoFileManager videoFileManager = new VideoFileManager(Paths.get("/home/ben/Videos/"),
                    Duration.ofMinutes(1));
            //instantiate VideoEncoder instance to be tested
            VideoEncoder videoEncoder = new VideoEncoder(videoFileManager);
            //capture 100 images from the CameraViewer and write each of them
            //to the VideoEncoder instance straight after capturing them
            //(order is 'capture', 'write', 'capture', 'write', etc.)
            for (int i = 0; i < 100; i++) {
                cameraViewer.captureImage().ifPresent(image -> {
                    try {
                        videoEncoder.appendToStream(image);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                //output current video file size in KB (Kilobytes)
                videoFileManager.getCurrentSaveFile().ifPresent(file ->
                {
                    try {
                        System.out.println(Files.size(file) / 1_000d);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

}
