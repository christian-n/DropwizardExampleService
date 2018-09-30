package de.nelius.service.resource.repository;

import de.nelius.service.resource.Person;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class PersonRepository extends AbstractDAO<Person> implements CrudRepository<Person> {

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

    public boolean delete(String id){
        currentSession().delete(get(id));
        return get(id) == null;
    }

}