package de.nelius.service.generic.repository;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.List;

/**
 * Repository that offers CRUD functionality. Layer between Hibernate and {@link org.glassfish.jersey.server.model.Resource}.
 *
 * @author Christian Nelius
 */
import static java.util.Objects.requireNonNull;

public class SimpleCRUDRepository<T, S extends Serializable> implements CRUDRepository<T, S> {

    private Class<T> domainClass;
    private SessionFactory sessionFactory;

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public SimpleCRUDRepository(Class<T> domainClass, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.domainClass = domainClass;
    }

    public List<T> getAll() {
        return requireNonNull((Query<T>) sessionFactory.getCurrentSession().createQuery("from " + domainClass.getSimpleName())).list();
    }

    public T getOne(S id) {
        return sessionFactory.getCurrentSession().get(domainClass, requireNonNull(id));
    }

    public T save(T object) {
        sessionFactory.getCurrentSession().saveOrUpdate(requireNonNull(object));
        return object;
    }

    public boolean delete(S id) {
        sessionFactory.getCurrentSession().delete(getOne(id));
        return getOne(id) == null;
    }

}
