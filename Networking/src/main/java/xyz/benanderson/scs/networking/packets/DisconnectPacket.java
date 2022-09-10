package xyz.benanderson.scs.networking.packets;

import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

/**
 * Packet sent by a connection when the connection wishes to disconnect from its peer.
 */
public class DisconnectPacket extends Packet {

    /**
     * Reason for the connection disconnecting
     */
    @Getter
    private final String reason;

    /**
     * Constructor for {@code DisconnectPacket} class
     *
     * @param reason reason why the connection wishes to disconnect
     */
    public DisconnectPacket(String reason) {
        //set `type` property in the superclass to `DisconnectPacket.class`
        super(DisconnectPacket.class);
        //assign instance property to constructor parameter
        this.reason = reason;
    }

}
