package xyz.benanderson.scs.networking.packet;

import lombok.AccessLevel;
import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

public class TestPacket extends Packet {

    @Getter(AccessLevel.PUBLIC)
    private final int testData;

    public TestPacket(int testData) {
        super(TestPacket.class);
        this.testData = testData;
    }

}
