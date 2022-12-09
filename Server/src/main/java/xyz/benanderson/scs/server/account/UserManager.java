package xyz.benanderson.scs.server.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

public interface UserManager {

    Optional<User> getUser(String username);

    void createUser(User user) throws Exception;

    void deleteUser(String username) throws Exception;

    static String hashPassword(String plainTextPassword) {
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
