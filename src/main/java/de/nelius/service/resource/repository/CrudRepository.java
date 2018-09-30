package de.nelius.service.resource.repository;

import org.hibernate.query.Query;

import java.util.List;

public interface CrudRepository<T> {

    List<T> getAll();

    T getOne(String id);

    T save(T domainClass);

    boolean delete(String id);

}
