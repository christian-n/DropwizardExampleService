package de.nelius.service.simple;

import com.codahale.metrics.annotation.Timed;
import de.nelius.service.entities.Person;
import de.nelius.service.entities.Response;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.PATCH;
import org.eclipse.jetty.http.HttpStatus;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/person")
@Produces(MediaType.APPLICATION_JSON)
public class PersonEndpoint {

    private PersonRepository personRepository;

    public PersonEndpoint(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GET
    @Timed
    @UnitOfWork
    @RolesAllowed("read")
    public List<Person> getAll() {
        return personRepository.getAll();
    }

    @GET
    @Path("{id}")
    @Timed
    @UnitOfWork
    @RolesAllowed("read")
    public Person get(@PathParam("id") String id) {
        return personRepository.getOne(id);
    }

    @POST
    @Timed
    @UnitOfWork
    @RolesAllowed("write")
    public Person create(@NotNull @Valid Person person) {
        return personRepository.save(person);
    }

    @PUT
    @Path("{id}")
    @Timed
    @UnitOfWork
    @RolesAllowed("write")
    public Person updatePut(@PathParam("id") String id, @NotNull @Valid Map<String, Object> body) {
        return update(id, body);
    }


    @PATCH
    @Path("{id}")
    @Timed
    @UnitOfWork
    @RolesAllowed("write")
    public Person updatePatch(@PathParam("id") String id, @NotNull @Valid Map<String, Object> body) {
        return update(id, body);
    }

    @DELETE
    @Path("{id}")
    @Timed
    @UnitOfWork
    @RolesAllowed("write")
    public Response delete(@PathParam("id") String id) {
        if (personRepository.delete(id)) {
            return new Response(HttpStatus.NO_CONTENT_204);
        }
        return new Response(HttpStatus.NOT_FOUND_404);
    }

    private Person update(String id, Map<String, Object> body) {
        Person source = personRepository.getOne(id);
        source.setForename(body.containsKey("forename") ? body.get("forename").toString() : source.getForename());
        source.setSurname(body.containsKey("surname") ? body.get("surname").toString() : source.getSurname());
        source.setBirthDate(body.containsKey("birthDate") ? body.get("birthDate").toString() : source.getBirthDate());
        return personRepository.save(source);
    }

}
