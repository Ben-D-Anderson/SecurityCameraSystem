package xyz.benanderson.scs.server.account;

import java.util.Optional;

public interface UserManager {

    Optional<User> getUser(String username);

    void createUser(User user) throws Exception;

    void deleteUser(String username) throws Exception;

}
