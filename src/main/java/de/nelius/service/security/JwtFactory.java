package de.nelius.service.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Configuration for {@link JwtAuthenticator}.
 * <p>
 * properties:
 * <p>
 * jwt.issuer : token issuer : http://your-auth-server
 * jwt.secret : HS256 secret : YourSecret
 *
 * @author Christian Nelius
 */
public class JwtFactory {

    /**
     * The issuer for token validation.
     *
     * @return Issuer
     */
    @Valid
    @NotNull
    @JsonProperty
    private String issuer = null;

    /**
     * The secret for verifying the token.
     * Token uses HS256 algorithm.
     *
     * @return Issuer
     */
    @Valid
    @NotNull
    @JsonProperty
    private String secret = null;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
