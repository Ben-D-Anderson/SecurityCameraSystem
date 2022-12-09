package xyz.benanderson.scs.networking.packets;

import lombok.AccessLevel;
import lombok.Getter;
import xyz.benanderson.scs.networking.Packet;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

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
    private final String hashedPassword;

    /**
     * Constructor for {@code LoginPacket} class
     *
     * @param username username of user to authenticate as
     * @param hashedPassword password to use when authenticating
     */
    private LoginPacket(String username, String hashedPassword) {
        //set `type` property in the superclass to `LoginPacket.class`
        super(LoginPacket.class);
        //assign object properties to constructor parameters
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public static LoginPacket fromPlainTextPassword(String username, String plainTextPassword) {
        return new LoginPacket(username, hashPassword(plainTextPassword));
    }

    public static LoginPacket fromHashedPassword(String username, String hashedPassword) {
        return new LoginPacket(username, hashedPassword);
    }

    public static String hashPassword(String plainTextPassword) {
        try{
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));
            final StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                final String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
