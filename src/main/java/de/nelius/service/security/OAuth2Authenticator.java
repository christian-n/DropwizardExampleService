package de.nelius.service.security;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import java.util.Optional;

public class OAuth2Authenticator implements Authenticator<String, User> {
    @Override
    public Optional<User> authenticate(String token) throws AuthenticationException {
        if ("test_token".equals(token)) {
            return Optional.of(new User());
        }
        return Optional.empty();
    }
}
