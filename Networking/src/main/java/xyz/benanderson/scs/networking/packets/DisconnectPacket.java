package xyz.benanderson.scs.networking.packets;

import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

public class DisconnectPacket extends Packet {

    //disconnect packet property (reason)
    @Getter
    private final String reason;

    //constructor taking a disconnect reason as the parameter
    public DisconnectPacket(String reason) {
        //set `type` property in the superclass to `DisconnectPacket.class`
        super(DisconnectPacket.class);
        //assign instance property to constructor parameter
        this.reason = reason;
    }

}
