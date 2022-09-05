package xyz.benanderson.scs.networking.packets;

import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

public class LoginPacket extends Packet {

    //login packet properties (username and password)
    @Getter
    private final String username, password;

    //constructor taking username and password as parameters
    public LoginPacket(String username, String password) {
        //set `type` property in the superclass to `LoginPacket.class`
        super(LoginPacket.class);
        //assign instance properties to constructor parameters
        this.username = username;
        this.password = password;
    }

}
