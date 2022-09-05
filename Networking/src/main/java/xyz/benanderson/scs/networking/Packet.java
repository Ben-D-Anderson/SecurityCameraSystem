package xyz.benanderson.scs.networking;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class Packet {

    @Getter
    private final Class<?> type;

}
