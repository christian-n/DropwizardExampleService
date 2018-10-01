package de.nelius.service.simple;

import de.nelius.service.entities.Person;
import de.nelius.service.generic.repository.CRUDRepository;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Basic repository that extends {@link AbstractDAO}.
 * Implements {@link CRUDRepository} for {@link com.codahale.metrics.health.HealthCheck}.
 * <p>
 * Used as middle layer.
 *
 * @author Christian Nelius
 */
public class PersonRepository extends AbstractDAO<Person> implements CRUDRepository<Person, String> {

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public PersonRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Person> getAll() {
        return list((Query<Person>) currentSession().createQuery("from Person"));
    }

    public Person getOne(String id) {
        return get(id);
    }

    public Person save(Person person) {
        return persist(person);
    }

    public boolean delete(String id) {
        currentSession().delete(get(id));
        return get(id) == null;
    }

}
