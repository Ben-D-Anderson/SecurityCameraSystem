package xyz.benanderson.scs.networking;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class Packet<T> {

    @Getter
    private final Class<T> type;

}
