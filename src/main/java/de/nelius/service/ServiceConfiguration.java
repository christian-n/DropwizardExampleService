package de.nelius.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.server.ServerFactory;

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
    private String serviceName = null;

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @JsonProperty("service.name")
    public String getServiceName(){
        return serviceName;
    }

    @JsonProperty("service.name")
    public void setServiceName(String serviceName){
        this.serviceName = serviceName;
    }
}
