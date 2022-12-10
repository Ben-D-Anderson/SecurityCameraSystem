package xyz.benanderson.scs.networking.packets;

import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

public class InfoPacket extends Packet {

    @Getter
    private final String info;

    public InfoPacket(String info) {
        super(InfoPacket.class);
        this.info = info;
    }

}
