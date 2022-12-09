package xyz.benanderson.scs.server.account.managers;

import xyz.benanderson.scs.server.account.User;
import xyz.benanderson.scs.server.account.UserManager;
import xyz.benanderson.scs.server.configuration.ConfigurationWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class MultiFileUserManager implements UserManager {

    @Override
    public Optional<User> getUser(String username) {
        Path userSaveFile = ConfigurationWrapper.getInstance().getUsersSaveDirectory()
                .resolve(username);
        if (!Files.exists(userSaveFile)) {
            return Optional.empty();
        }
        try {
            List<String> lines = Files.readAllLines(userSaveFile);
            User parsedUser = User.fromHashedPassword(lines.get(0),
                    lines.get(1),
                    Boolean.parseBoolean(lines.get(2)));
            return Optional.of(parsedUser);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void createUser(User user) throws IOException {
        //create directory if it does not exist
        Path usersSaveDirectory = ConfigurationWrapper.getInstance().getUsersSaveDirectory();
        Files.createDirectories(usersSaveDirectory);
        //creates and writes user data to file (automatically closes it)
        Path userSaveFile = usersSaveDirectory.resolve(user.getUsername());
        Files.writeString(userSaveFile, user.getUsername() + System.lineSeparator()
                + user.getHashedPassword() + System.lineSeparator()
                + user.isAdmin());
    }

    @Override
    public void deleteUser(String username) throws IOException {
        Path userSaveFile = ConfigurationWrapper.getInstance().getUsersSaveDirectory()
                .resolve(username);
        Files.deleteIfExists(userSaveFile);
    }

}
