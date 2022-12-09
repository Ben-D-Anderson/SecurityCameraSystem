package xyz.benanderson.scs.server.account;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.benanderson.scs.networking.packets.LoginPacket;

@Data
@EqualsAndHashCode
public class User {

    private final String username, hashedPassword;
    private final boolean admin;

    public static User fromHashedPassword(String username, String hashedPassword, boolean admin) {
        return new User(username, hashedPassword, admin);
    }

    public static User fromPlainTextPassword(String username, String plainTextPassword, boolean admin) {
        return new User(username, LoginPacket.hashPassword(plainTextPassword), admin);
    }

    private User(String username, String hashedPassword, boolean admin) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.admin = admin;
    }

}
