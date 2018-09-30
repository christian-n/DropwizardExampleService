package de.nelius.service.resource.repository;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class SimpleCrudRepository<T> extends AbstractDAO<T> implements CrudRepository<T>{

    private Class<T> domainClass;

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public SimpleCrudRepository(SessionFactory sessionFactory, Class<T> domainClass) {
        super(sessionFactory);
        this.domainClass = domainClass;
    }

    public List<T> getAll() {
        return list((Query<T>) currentSession().createQuery("from " + domainClass.getSimpleName()));
    }

    public T getOne(String id) {
        return get(id);
    }

    public T save(T domainClass) {
        return persist(domainClass);
    }

    public boolean delete(String id) {
        currentSession().delete(get(id));
        return get(id) == null;
    }
}
