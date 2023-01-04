package xyz.benanderson.scs.server.video;

import lombok.AllArgsConstructor;
import org.jcodec.api.awt.AWTSequenceEncoder;

import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
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
            randomAccessFile.seek(randomAccessFile.length());
            //write compressed media frame
            randomAccessFile.write(compressImage(image));
        } catch (Exception e) {
            //output error if one is encountered
            System.err.println("[ERROR] An error occurred when writing timestamp metadata to a save file.");
            e.printStackTrace();
        }
    }

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
                BufferedImage image;
                try {
                    image = reader.read(frameNumber);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
                encoder.encodeImage(image);
            }
            encoder.finish();
            System.out.println("[INFO] Finished encoding video " + videoOutputFile.getName());
            //delete raw media save file after video constructed from it
            Files.delete(rawMediaSaveFile);
        } catch (Exception e) {
            //output error if encountered
            System.err.println("[ERROR] An error occurred when encoding a video from a raw media save file.");
            e.printStackTrace();
        }
    }

    public byte[] compressImage(BufferedImage bufferedImage) {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (ImageOutputStream outputStream = new MemoryCacheImageOutputStream(compressed)) {
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();

            // Configure JPEG compression: 20% quality
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(0.2f);

            jpgWriter.setOutput(outputStream);
            jpgWriter.write(null, new IIOImage(bufferedImage, null, null), jpgWriteParam);
            jpgWriter.dispose();
        } catch (IOException e) {
            System.err.println("[ERROR] An error occurred when compressing a media frame for a raw media save file.");
            e.printStackTrace();
        }
        return compressed.toByteArray();
    }

}
