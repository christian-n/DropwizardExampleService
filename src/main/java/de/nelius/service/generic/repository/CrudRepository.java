package de.nelius.service.generic.repository;

import java.io.Serializable;
import java.util.List;

/**
 * CRUD interface for abstract handling of entities.
 *
 * @param <T> {@link javax.persistence.Entity}
 * @author Christian Nelius
 */
public interface CrudRepository<T, S extends Serializable> {

    List<T> getAll();

    T getOne(S id);

    T save(T object);

    boolean delete(S id);

}
