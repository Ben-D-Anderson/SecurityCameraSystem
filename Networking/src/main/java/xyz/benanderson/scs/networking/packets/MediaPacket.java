package xyz.benanderson.scs.networking.packets;

import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

import java.awt.image.BufferedImage;

public class MediaPacket extends Packet {

    //media packet property (mediaFrame)
    @Getter
    private final BufferedImage mediaFrame;

    //constructor taking a BufferedImage as the parameter
    public MediaPacket(BufferedImage mediaFrame) {
        //set `type` property in the superclass to `MediaPacket.class`
        super(MediaPacket.class);
        //assign instance property to constructor parameter
        this.mediaFrame = mediaFrame;
    }

}
