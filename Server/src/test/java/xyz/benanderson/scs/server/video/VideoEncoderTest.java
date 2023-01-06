package xyz.benanderson.scs.server.video;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VideoEncoderTest {

    @Test
    public void testAppendToStream(@TempDir Path tempDir) throws IOException {
        //test data
        long numberOfFrames = 2;
        long startTimestamp = System.currentTimeMillis() - 1000;
        long endTimestamp = startTimestamp + 1000;
        BufferedImage[] testImages = getTestImages();

        //running the method to be tested with the test data
        VideoFileManager videoFileManager = new VideoFileManager(tempDir, Duration.ofMinutes(5));
        VideoEncoder videoEncoder = new VideoEncoder(videoFileManager);
        videoEncoder.appendToStream(testImages[0], startTimestamp);
        videoEncoder.appendToStream(testImages[1], endTimestamp);

        //serializing images so that they can be compared to the output
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.writeBytes(videoEncoder.compressImage(testImages[0]));
        byteArrayOutputStream.writeBytes(videoEncoder.compressImage(testImages[1]));

        Path rawMediaSaveFile = videoFileManager.getCurrentSaveFile().orElseThrow(FileNotFoundException::new);
        //checking that the data outputted by the method being tested is correct
        try (InputStream inputStream = Files.newInputStream(rawMediaSaveFile);
             DataInputStream dataInputStream = new DataInputStream(inputStream)) {
            assertEquals(numberOfFrames, dataInputStream.readLong());
            assertEquals(startTimestamp, dataInputStream.readLong());
            assertEquals(endTimestamp, dataInputStream.readLong());
            assertArrayEquals(byteArrayOutputStream.toByteArray(), dataInputStream.readAllBytes());
        }
    }

    @Test
    public void testProcessRawMediaSave(@TempDir Path tempDir) throws IOException {
        //test data
        long numberOfFrames = 2;
        long startTimestamp = System.currentTimeMillis() - 1000;
        long endTimestamp = startTimestamp + 1000;
        BufferedImage[] testImages = getTestImages();

        //file setup
        VideoFileManager videoFileManager = new VideoFileManager(tempDir, Duration.ofMinutes(5));
        VideoEncoder videoEncoder = new VideoEncoder(videoFileManager);

        Path rawMediaSaveFile = videoFileManager.getCurrentSaveFile().orElseThrow(FileNotFoundException::new);
        //writing test data to file
        try (OutputStream outputStream = Files.newOutputStream(rawMediaSaveFile);
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            dataOutputStream.writeLong(numberOfFrames);
            dataOutputStream.writeLong(startTimestamp);
            dataOutputStream.writeLong(endTimestamp);
            ImageIO.write(testImages[0], "jpg", dataOutputStream);
            ImageIO.write(testImages[1], "jpg", dataOutputStream);
        }

        //running the method to be tested
        videoEncoder.processRawMediaSave(rawMediaSaveFile);

        File videoOutputFile = new File(rawMediaSaveFile.toString().replace(".crms", ".mp4"));
        //todo parse output file to check length, fps etc. (maybe even frames
    }

    //private method to generate test images used as test data by other methods
    private BufferedImage[] getTestImages() {
        //create two images
        BufferedImage frameOne = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        BufferedImage frameTwo = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        //fill first image with red pixels
        for (int y = 0; y < frameOne.getHeight(); y++) {
            for (int x = 0; x < frameOne.getWidth(); x++) {
                frameOne.setRGB(x, y, 0xff0000);
            }
        }
        //fill second image with blue pixels
        for (int y = 0; y < frameTwo.getHeight(); y++) {
            for (int x = 0; x < frameTwo.getWidth(); x++) {
                frameTwo.setRGB(x, y, 0x0000ff);
            }
        }
        //return images
        return new BufferedImage[] {frameOne, frameTwo};
    }

}
