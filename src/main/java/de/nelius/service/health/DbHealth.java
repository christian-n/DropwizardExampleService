package de.nelius.service.health;

import com.codahale.metrics.health.HealthCheck;
import de.nelius.service.generic.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;

public class DbHealth extends HealthCheck {

    private List<CrudRepository<?, ?>> repositories = new ArrayList<>();

    public DbHealth() {
    }

    public DbHealth(List<CrudRepository<?, ?>> repositories) {
        this.repositories = repositories;
    }

    public void addRepository(CrudRepository<?, ?> repository) {
        this.repositories.add(repository);
    }

    @Override
    protected Result check() throws Exception {
        repositories.forEach(CrudRepository::getAll);
        return Result.healthy();
    }
}
