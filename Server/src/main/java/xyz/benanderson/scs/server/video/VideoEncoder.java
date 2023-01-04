package xyz.benanderson.scs.server.video;

import lombok.AllArgsConstructor;
import org.jcodec.api.awt.AWTSequenceEncoder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@AllArgsConstructor
public class VideoEncoder {

    private final VideoFileManager videoFileManager;

    //file format of raw media save file is as follows:
    //- long (number of media frames in the file)
    //- long (start timestamp in milliseconds)
    //- long (end timestamp in milliseconds)
    //- list of serialized media frames

    public void appendToStream(BufferedImage image, long currentTimeMillis) {
        //output error if can't access a save file
        Optional<Path> currentSaveFileOptional = videoFileManager.getCurrentSaveFile();
        if (currentSaveFileOptional.isEmpty()) {
            System.err.println("[ERROR] Failed to fetch video save file.");
            return;
        }
        //open current save file as `RandomAccessFile`, therefore being able to jump around the file
        Path currentSaveFile = currentSaveFileOptional.get();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(currentSaveFile.toFile(), "rw")) {
            //if file size is zero, it is new and needs some header metadata at the start of the file
            if (Files.size(currentSaveFile) == 0L) {
                randomAccessFile.writeLong(1L);
                randomAccessFile.seek(8);
                randomAccessFile.writeLong(currentTimeMillis);
            } else {
                //if file size is not zero, increase the number of media frames in the header
                long currentNumberFrames = randomAccessFile.readLong();
                randomAccessFile.seek(0);
                randomAccessFile.writeLong(currentNumberFrames + 1);
            }
            //write the time of the last media frame
            randomAccessFile.seek(16);
            randomAccessFile.writeLong(currentTimeMillis);
        } catch (Exception e) {
            //output error if one is encountered
            System.err.println("[ERROR] An error occurred when writing timestamp metadata to a save file.");
            e.printStackTrace();
        }
        //append the latest media frame to the file serialized as bytes
        try (OutputStream outputStream = Files.newOutputStream(currentSaveFile, StandardOpenOption.APPEND)) {
            ImageIO.write(image, "jpg", outputStream);
        } catch (Exception e) {
            //output error if one is encountered
            System.err.println("[ERROR] An error occurred when writing a media frame to a save file.");
            e.printStackTrace();
        }
    }

    //todo make job that runs and calls this method on any raw media saves found
    public void processRawMediaSave(Path rawMediaSaveFile) {
        //open file using `RandomAccessFile` to be able to skip around in the file
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(rawMediaSaveFile.toFile(), "r");
             FileImageInputStream fileImageInputStream = new FileImageInputStream(randomAccessFile)) {
            //read header metadata
            long numberOfFrames = randomAccessFile.readLong();
            long startTimestamp = randomAccessFile.readLong();
            long endTimestamp = randomAccessFile.readLong();
            //calculate fps (frames per second) using header metadata
            long videoDurationInMillis = endTimestamp - startTimestamp;
            int fps = (int) (numberOfFrames / (videoDurationInMillis / 1000));

            //setup image (media frame) reading
            fileImageInputStream.seek(24);
            ImageReader reader = ImageIO.getImageReadersBySuffix("jpg").next();
            reader.setInput(fileImageInputStream);

            //setup video encoding
            File videoOutputFile = new File(rawMediaSaveFile.toString().replace(".crms", ".mp4"));
            AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(videoOutputFile, fps);

            //read frames from the raw media save file and encode them into the output file
            for (int frameNumber = 0; frameNumber < numberOfFrames; frameNumber++) {
                BufferedImage image = reader.read(frameNumber);
                encoder.encodeImage(image);
            }
            encoder.finish();
        } catch (Exception e) {
            //output error if encountered
            System.err.println("[ERROR] An error occurred when encoding a video from a raw media save file.");
            e.printStackTrace();
        }
    }


}
