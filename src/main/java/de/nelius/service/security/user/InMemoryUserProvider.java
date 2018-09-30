package de.nelius.service.security.user;

import java.util.HashMap;
import java.util.Map;

/**
 * Example in memory user provider.
 *
 * @author Christian Nelius
 */
public class InMemoryUserProvider implements UserProvider {

    private static final Map<String, User> users = users();

    @Override
    public User getUser(String username) {
        return users.get(username);
    }

    private static Map<String, User> users() {
        Map<String, User> users = new HashMap<>();
        users.put("simple-user", new User("simple-user", "read"));
        users.put("simple-admin", new User("simple-admin", "read", "write"));
        return users;
    }
}
