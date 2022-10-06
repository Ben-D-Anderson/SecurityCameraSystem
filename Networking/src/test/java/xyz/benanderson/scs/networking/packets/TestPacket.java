package xyz.benanderson.scs.networking.packets;

import lombok.AccessLevel;
import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

//test packet type only used in testing to confirm
//data is correctly transmitted and received
public class TestPacket extends Packet {

    @Getter(AccessLevel.PUBLIC)
    private final int testData;

    public TestPacket(int testData) {
        super(TestPacket.class);
        this.testData = testData;
    }

}