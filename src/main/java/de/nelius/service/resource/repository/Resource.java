package de.nelius.service.resource.repository;

import java.io.Serializable;

public interface Resource<T extends Serializable> {

    void setId(T id);

    T getId();

}
