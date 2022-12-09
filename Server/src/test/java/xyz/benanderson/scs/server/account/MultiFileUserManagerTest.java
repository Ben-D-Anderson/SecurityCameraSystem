package xyz.benanderson.scs.server.account;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.benanderson.scs.server.account.managers.MultiFileUserManager;
import xyz.benanderson.scs.server.configuration.ConfigurationWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MultiFileUserManagerTest {

    static UserManager userManager = new MultiFileUserManager();
    static Path usersFolder = ConfigurationWrapper.getInstance().getUsersSaveDirectory();
    static Path userFile = ConfigurationWrapper.getInstance().getUsersSaveDirectory()
            .resolve("testUser".toLowerCase());

    @BeforeAll
    static void createUsersFolder() throws IOException {
        Files.createDirectories(usersFolder);
    }

    @AfterEach
    void deleteUserFile() throws IOException {
        Files.deleteIfExists(userFile);
    }

    @AfterAll
    static void deleteUsersFolder() throws IOException {
        Files.deleteIfExists(userFile);
        Files.deleteIfExists(usersFolder);
    }

    @Test
    public void testGetUserNotExists() {
        assertTrue(userManager.getUser("noUser").isEmpty());
    }

    @Test
    public void testGetUserExists() throws IOException {
        Files.writeString(userFile, "testUser\nhashedPassword\nfalse");
        Optional<User> userOptional = userManager.getUser("testUser");
        assertTrue(userOptional.isPresent());
        assertEquals("testUser", userOptional.get().getUsername());
        assertEquals("hashedPassword", userOptional.get().getHashedPassword());
        assertFalse(userOptional.get().isAdmin());
    }

    @Test
    public void testCreateUser() throws Exception {
        assertFalse(Files.exists(userFile));
        User user = User.fromHashedPassword("testUser", "hashedPassword", false);
        userManager.createUser(user);
        assertTrue(Files.exists(userFile));
        List<String> lines = Files.readAllLines(userFile);
        assertEquals("testUser", lines.get(0));
        assertEquals("hashedPassword", lines.get(1));
        assertFalse(Boolean.parseBoolean(lines.get(2)));
    }

    @Test
    public void deleteUser() throws Exception {
        assertFalse(Files.exists(userFile));
        Files.writeString(userFile, "testUser\nhashedPassword\nfalse");
        assertTrue(Files.exists(userFile));
        userManager.deleteUser("testUser");
        assertFalse(Files.exists(userFile));
    }

}
