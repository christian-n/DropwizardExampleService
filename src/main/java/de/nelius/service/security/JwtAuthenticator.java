package de.nelius.service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import de.nelius.service.security.user.User;
import de.nelius.service.security.user.UserProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import java.util.Optional;

/**
 * Jwt implementation of {@link Authenticator}. Verifies the signature with HMAC256 algorithm and a secret and decodes the claims.
 * <p>
 * That's just an example. We could also use other {@link Algorithm} or a {@link java.security.KeyStore}.
 * <p>
 * In this case we start with subject claim but we can add all other oauth2 claims.
 *
 * @author Christian Nelius
 */
public class JwtAuthenticator implements Authenticator<String, User> {

    private JwtFactory jwtFactory;
    private UserProvider userProvider;

    public JwtAuthenticator(JwtFactory jwtFactory, UserProvider userProvider) {
        this.jwtFactory = jwtFactory;
        this.userProvider = userProvider;
    }

    @Override
    public Optional<User> authenticate(String token) throws AuthenticationException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtFactory.getSecret());
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(jwtFactory.getIssuer()).build();
            return Optional.ofNullable(userProvider.getUser(verifier.verify(token).getSubject()));
        } catch (JWTVerificationException exception) {
            throw new AuthenticationException(exception);
        }
    }
}
