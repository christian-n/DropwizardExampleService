package de.nelius.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.nelius.service.generic.repository.CRUDRepository;
import de.nelius.service.generic.repository.SimpleCRUDRepository;
import de.nelius.service.generic.resource.CRUDResourceMapping;
import de.nelius.service.health.DbHealth;
import de.nelius.service.entities.Address;
import de.nelius.service.entities.Person;
import de.nelius.service.simple.PersonResource;
import de.nelius.service.simple.PersonRepository;
import de.nelius.service.security.user.InMemoryUserProvider;
import de.nelius.service.security.JwtAuthenticator;
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

/**
 * Dropwizard {@link Application}. Configures resources, security and health.
 *
 * @author Christian Nelius
 */
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
        bootstrap.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
        environment.jersey().register(new PersonResource(personRepository));
        dbHealth.addRepository(personRepository);
    }

    /**
     * Configures {@link javax.persistence.Entity} and {@link CRUDRepository} as service layer for
     * {@link io.dropwizard.hibernate.AbstractDAO} the generic way.
     * <p>
     * Use this with caution!
     *
     * @param configuration
     * @param environment
     */
    private void configureResourcesAsGeneric(ServiceConfiguration configuration, Environment environment) {
        CRUDRepository<Address, String> addressRepository = new SimpleCRUDRepository<>(Address.class, hibernateBundle.getSessionFactory());
        environment.jersey().getResourceConfig().registerResources(new CRUDResourceMapping<>("/address", Address.class, addressRepository).getResource());
    }

    /**
     * Configures {@link JwtAuthenticator} for JWT support, {@link UserAuthorizer}
     * for simple role authorization and {@link InMemoryUserProvider} for example user mappings.
     *
     * @param configuration
     * @param environment
     */
    private void configureSecurity(ServiceConfiguration configuration, Environment environment) {
        environment.jersey()
                .register(new AuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(new JwtAuthenticator(configuration.getJwtFactory(), new InMemoryUserProvider()))
                        .setAuthorizer(new UserAuthorizer()).setPrefix("bearer").buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }

    /**
     * Configures {@link DbHealth} as a simple database health check based on listing all {@link javax.persistence.Entity}.
     * Uses {@link CRUDRepository} because of abstraction purposes.
     *
     * @param configuration
     * @param environment
     */
    private void configureHealth(ServiceConfiguration configuration, Environment environment) {
        environment.healthChecks().register("Database Health", dbHealth);
    }

}
