package de.nelius.service.security.user;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

/**
 * Simple user that implements {@link Principal}.
 *
 * @author Christian Nelius
 */
public class User implements Principal {

    private String username;
    private List<String> roles;

    public User(String username, String... roles) {
        this.username = username;
        this.roles = Arrays.asList(roles);
    }

    @Override
    public String getName() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }
}
