package xyz.benanderson.scs.networking.packets;

import lombok.AccessLevel;
import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

/**
 * Packet send from a client to a server when attempting to authenticate.
 */
public class LoginPacket extends Packet {

    /**
     * Username that the connection is attempting to authenticate with
     */
    @Getter(AccessLevel.PUBLIC)
    private final String username;

    /**
     * Password that the connection is attempting to authenticate with
     */
    @Getter(AccessLevel.PUBLIC)
    private final String password;

    /**
     * Constructor for {@code LoginPacket} class
     *
     * @param username username of user to authenticate as
     * @param password password to use when authenticating
     */
    public LoginPacket(String username, String password) {
        //set `type` property in the superclass to `LoginPacket.class`
        super(LoginPacket.class);
        //assign object properties to constructor parameters
        this.username = username;
        this.password = password;
    }

}
