package xyz.benanderson.scs.networking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
public abstract class Packet implements Serializable {

    @Getter
    private final Class<? extends Packet> type;

}
