package de.nelius.service.resource.repository;

import com.codahale.metrics.annotation.Timed;
import de.nelius.service.resource.Status;
import io.dropwizard.jersey.PATCH;
import org.eclipse.jetty.http.HttpStatus;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import java.util.List;

public class CrudEndoint<T extends Resource<String>> {

    private SimpleCrudRepository<T> crudRepository;

    public CrudEndoint(SimpleCrudRepository<T> crudRepository) {
        this.crudRepository = crudRepository;
    }

    @GET
    @Timed
    @RolesAllowed("user")
    public List<T> getAll() {
        return crudRepository.getAll();
    }

    @GET
    @Path("{id}")
    @Timed
    @RolesAllowed("user")
    public T get(@PathParam("id") String id) {
        return crudRepository.getOne(id);
    }

    @POST
    @Timed
    @RolesAllowed("admin")
    public T create(@NotNull @Valid T resource) {
        return crudRepository.save(resource);
    }

    @PUT
    @PATCH
    @Path("{id}")
    @Timed
    @RolesAllowed("admin")
    public T update(@PathParam("id") String id, @NotNull @Valid T resource) {
        resource.setId(id);
        return crudRepository.save(resource);
    }

    @DELETE
    @Path("{id}")
    @Timed
    @RolesAllowed("admin")
    public Status delete(@PathParam("id") String id) {
        if(crudRepository.delete(id)){
            return new Status(HttpStatus.NO_CONTENT_204);
        }
        return new Status(HttpStatus.NOT_FOUND_404);
    }
}
