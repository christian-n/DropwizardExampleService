package de.nelius.service.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Christian Nelius
 */
public class JwtFactory {

    @Valid
    @NotNull
    @JsonProperty
    private String issuer = null;

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
