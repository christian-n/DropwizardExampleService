package de.nelius.service.health;

import com.codahale.metrics.health.HealthCheck;
import de.nelius.service.resource.repository.CrudRepository;
import org.hibernate.SessionFactory;

import java.util.List;

public class DbHealth extends HealthCheck {

    private List<CrudRepository<?>> repositories;

    public DbHealth(List<CrudRepository<?>> repositories) {
        this.repositories = repositories;
    }

    @Override
    protected Result check() throws Exception {
        repositories.forEach(CrudRepository::getAll);
        return Result.healthy();
    }
}
