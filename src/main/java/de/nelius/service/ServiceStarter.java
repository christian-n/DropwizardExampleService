package de.nelius.service;

import de.nelius.service.generic.repository.CrudRepository;
import de.nelius.service.generic.repository.SimpleCrudRepository;
import de.nelius.service.generic.resource.ResourceCrudMapping;
import de.nelius.service.health.DbHealth;
import de.nelius.service.entities.Address;
import de.nelius.service.entities.Person;
import de.nelius.service.simple.PersonEndpoint;
import de.nelius.service.simple.PersonRepository;
import de.nelius.service.security.user.InMemoryUserProvider;
import de.nelius.service.security.OAuth2Authenticator;
import de.nelius.service.security.user.User;
import de.nelius.service.security.user.UserAuthorizer;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

public class ServiceStarter extends Application<ServiceConfiguration> {

    private final DbHealth dbHealth = new DbHealth();

    private final HibernateBundle<ServiceConfiguration> hibernateBundle =
            new HibernateBundle<ServiceConfiguration>(Person.class, Address.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(ServiceConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    public static void main(String[] args) throws Exception {
        new ServiceStarter().run(args);
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(path -> getClass().getResourceAsStream("/" + path));
        bootstrap.addBundle(hibernateBundle);
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) {
        configureResourcesAsBasic(configuration, environment);
        configureResourcesAsGeneric(configuration, environment);
        configureSecurity(configuration, environment);
        configureHealth(configuration, environment);
    }

    /**
     * Configures {@link javax.persistence.Entity} and repositories as service layer for
     * {@link io.dropwizard.hibernate.AbstractDAO} the basic way.
     *
     * @param configuration
     * @param environment
     */
    private void configureResourcesAsBasic(ServiceConfiguration configuration, Environment environment) {
        PersonRepository personRepository = new PersonRepository(hibernateBundle.getSessionFactory());
        environment.jersey().register(new PersonEndpoint(personRepository));
        dbHealth.addRepository(personRepository);
    }

    /**
     * Configures {@link javax.persistence.Entity} and {@link CrudRepository} as service layer for
     * {@link io.dropwizard.hibernate.AbstractDAO} the generic way.
     * <p>
     * Use this with caution!
     *
     * @param configuration
     * @param environment
     */
    private void configureResourcesAsGeneric(ServiceConfiguration configuration, Environment environment) {
        CrudRepository<Address, String> addressRepository = new SimpleCrudRepository<>(Address.class, hibernateBundle.getSessionFactory());
        environment.jersey().getResourceConfig().registerResources(new ResourceCrudMapping<>("/address", Address.class, addressRepository).getResource());
    }

    /**
     * Configures {@link OAuth2Authenticator} for JWT support, {@link UserAuthorizer}
     * for simple role authorization and {@link InMemoryUserProvider} for example user mappings.
     *
     * @param configuration
     * @param environment
     */
    private void configureSecurity(ServiceConfiguration configuration, Environment environment) {
        environment.jersey()
                .register(new AuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(new OAuth2Authenticator(configuration.getJwtFactory(), new InMemoryUserProvider()))
                        .setAuthorizer(new UserAuthorizer()).setPrefix("bearer").buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }

    /**
     * Configures {@link DbHealth} as a simple database health check based on listing all {@link javax.persistence.Entity}.
     * Uses {@link CrudRepository} because of abstraction purposes.
     *
     * @param configuration
     * @param environment
     */
    private void configureHealth(ServiceConfiguration configuration, Environment environment) {
        environment.healthChecks().register("Database Health", dbHealth);
    }

}
