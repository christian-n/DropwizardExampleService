package de.nelius.service.security.user;

import io.dropwizard.auth.Authorizer;

/**
 * Authorizes given roles with {@link User} roles.
 *
 * @author Christian Nelius
 */
public class UserAuthorizer implements Authorizer<User> {

    @Override
    public boolean authorize(User principal, String role) {
        return principal.getRoles().contains(role);
    }

}
