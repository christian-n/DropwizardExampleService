package de.nelius.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.nelius.service.security.JwtFactory;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Service configuration extends Dropwizards {@link Configuration}
 */
public class ServiceConfiguration extends Configuration {

    @Valid
    @NotNull
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Valid
    @NotNull
    private JwtFactory jwtFactory;


    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @JsonProperty("jwt")
    public JwtFactory getJwtFactory() {
        return jwtFactory;
    }

    @JsonProperty("jwt")
    public void setJwtFactory(JwtFactory jwtFactory) {
        this.jwtFactory = jwtFactory;
    }
}
