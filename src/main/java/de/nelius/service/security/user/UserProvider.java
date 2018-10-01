package de.nelius.service.security.user;

import de.nelius.service.security.JwtAuthenticator;

/**
 * User provider interface for authentication with {@link JwtAuthenticator}
 *
 * @author Christian Nelius
 */
public interface UserProvider {

    User getUser(String username);

}
