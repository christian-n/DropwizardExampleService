package de.nelius.service.resource;

import com.codahale.metrics.annotation.Timed;
import de.nelius.service.resource.repository.PersonRepository;
import io.dropwizard.jersey.PATCH;
import org.eclipse.jetty.http.HttpStatus;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/person")
@Produces(MediaType.APPLICATION_JSON)
public class PersonEndpoint {

    private PersonRepository personRepository;

    public PersonEndpoint(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GET
    @Timed
    @RolesAllowed("user")
    public List<Person> getAll() {
        return personRepository.getAll();
    }

    @GET
    @Path("{id}")
    @Timed
    @RolesAllowed("user")
    public Person get(@PathParam("id") String id) {
        return personRepository.getOne(id);
    }

    @POST
    @Timed
    @RolesAllowed("admin")
    public Person create(@NotNull @Valid Person person) {
        return personRepository.save(person);
    }

    @PUT
    @PATCH
    @Path("{id}")
    @Timed
    @RolesAllowed("admin")
    public Person update(@PathParam("id") String id, @NotNull @Valid Person person) {
        person.setId(id);
        return personRepository.save(person);
    }

    @DELETE
    @Path("{id}")
    @Timed
    @RolesAllowed("admin")
    public Status delete(@PathParam("id") String id) {
        if(personRepository.delete(id)){
            return new Status(HttpStatus.NO_CONTENT_204);
        }
        return new Status(HttpStatus.NOT_FOUND_404);
    }


}
