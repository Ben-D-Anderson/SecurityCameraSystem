package xyz.benanderson.scs.server.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings("ClassCanBeRecord")
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class User {

    private final String username, password;
    private final boolean admin;

}
