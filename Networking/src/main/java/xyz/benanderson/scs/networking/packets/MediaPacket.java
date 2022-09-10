package xyz.benanderson.scs.networking.packets;

import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

import java.awt.image.BufferedImage;

/**
 * Packet sent from a server to a client. This packet contains an image
 * which is the media frame/image of the security camera at a point in time.
 */
public class MediaPacket extends Packet {

    /**
     * Media frame/image that makes up the main content packet
     */
    @Getter
    private final BufferedImage mediaFrame;

    /**
     * Constructor for {@code MediaPacket} class
     *
     * @param mediaFrame BufferedImage representing the media frame/image
     */
    public MediaPacket(BufferedImage mediaFrame) {
        //set `type` property in the superclass to `MediaPacket.class`
        super(MediaPacket.class);
        //assign instance property to constructor parameter
        this.mediaFrame = mediaFrame;
    }

}
