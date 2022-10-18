package xyz.benanderson.scs.server.video;

import lombok.AllArgsConstructor;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@AllArgsConstructor
public class VideoEncoder {

    private final VideoFileManager videoFileManager;

    //todo convert to using imageio to store lots of images in file, then once duration is reached
    //convert the images file to a video
    public void appendToStream(BufferedImage image) throws IOException {
        Optional<Path> currentSaveFileOptional = videoFileManager.getCurrentSaveFile();
        if (currentSaveFileOptional.isEmpty()) {
            System.err.println("[ERROR] Failed to fetch video save file.");
            return;
        }
        Path currentSaveFile = currentSaveFileOptional.get();
        AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(currentSaveFile.toFile(), 30);
//        SequenceEncoder sequenceEncoder = SequenceEncoder.createSequenceEncoder();
//        sequenceEncoder.encodeNativeFrame(AWTUtil.fromBufferedImageRGB(image));
        encoder.encodeImage(image);
//        encoder.finish();
    }


}
