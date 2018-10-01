package de.nelius.service.health;

import com.codahale.metrics.health.HealthCheck;
import de.nelius.service.generic.repository.CRUDRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link HealthCheck} for {@link javax.persistence.Entity} with use of {@link CRUDRepository}.
 *
 * @author Christian Nelius
 */
public class DbHealth extends HealthCheck {

    private List<CRUDRepository<?, ?>> repositories = new ArrayList<>();

    public DbHealth() {
    }

    public DbHealth(List<CRUDRepository<?, ?>> repositories) {
        this.repositories = repositories;
    }

    public void addRepository(CRUDRepository<?, ?> repository) {
        this.repositories.add(repository);
    }

    @Override
    protected Result check() throws Exception {
        repositories.forEach(CRUDRepository::getAll);
        return Result.healthy();
    }
}
