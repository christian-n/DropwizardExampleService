package de.nelius.service;

import de.nelius.service.health.DbHealth;
import de.nelius.service.resource.Person;
import de.nelius.service.resource.PersonEndpoint;
import de.nelius.service.resource.repository.PersonRepository;
import de.nelius.service.security.User;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.activation.DataSource;
import java.util.Collections;

public class ServiceStarter  extends Application<ServiceConfiguration> {

    private final HibernateBundle<ServiceConfiguration> hibernateBundle =
            new HibernateBundle<ServiceConfiguration>(Person.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(ServiceConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    public static void main(String[] args) throws Exception {
        new ServiceStarter().run(args);
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) {
        // Datasource configuration
        PersonRepository personRepository = new PersonRepository(hibernateBundle.getSessionFactory());

        // Register Health Check
        environment.healthChecks().register("Database Health", new DbHealth(Collections.singletonList(personRepository)));

        // Register OAuth authentication
        environment.jersey()
                .register(new AuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(new DropwizardBlogAuthenticator())
                        .setAuthorizer(new DropwizardBlogAuthorizer()).setPrefix(BEARER).buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        // Register resources
        environment.jersey().register(new PersonEndpoint(dbi.onDemand(PersonRepository.class)));
    }

}
