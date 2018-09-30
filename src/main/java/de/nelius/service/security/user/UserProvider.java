package de.nelius.service.security.user;

import de.nelius.service.security.OAuth2Authenticator;

/**
 * User provider interface for authentication with {@link OAuth2Authenticator}
 *
 * @author Christian Nelius
 */
public interface UserProvider {

    User getUser(String username);

}
