package de.nelius.service.generic.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nelius.service.generic.repository.CRUDRepository;
import de.nelius.service.generic.updater.JacksonUpdater;
import de.nelius.service.generic.updater.Updater;
import io.dropwizard.hibernate.UnitOfWork;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Resource;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Maps Jersey {@link Resource} with {@link CRUDRepository}.
 *
 * @author Christian Nelius
 */
public class CRUDResourceMapping<T, S extends Serializable> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Class<T> resource;
    private CRUDRepository<T, S> crudRepository;
    private String path;
    private Updater updater;

    public CRUDResourceMapping(String path, Class<T> resource, CRUDRepository<T, S> crudRepository) {
        this.path = path;
        this.resource = resource;
        this.crudRepository = crudRepository;
        this.updater = new JacksonUpdater();
    }

    public CRUDResourceMapping(String path, Class<T> resource, CRUDRepository<T, S> crudRepository, Updater updater) {
        this.path = path;
        this.resource = resource;
        this.crudRepository = crudRepository;
        this.updater = updater;
    }

    public Resource getResource() {
        Resource.Builder builder = Resource.builder(path);
        builder.path(path);
        builder.addMethod("GET").produces(MediaType.APPLICATION_JSON).handledBy(getAll());
        builder.addChildResource("{id}").addMethod("GET").produces(MediaType.APPLICATION_JSON).handledBy(get());
        builder.addMethod("POST").produces(MediaType.APPLICATION_JSON).handledBy(post());
        builder.addChildResource("{id}").addMethod("PUT").produces(MediaType.APPLICATION_JSON).handledBy(update());
        builder.addChildResource("{id}").addMethod("PATCH").produces(MediaType.APPLICATION_JSON).handledBy(update());
        builder.addChildResource("{id}").addMethod("DELETE").produces(MediaType.APPLICATION_JSON).handledBy(delete());
        return builder.build();
    }


    private Inflector<ContainerRequestContext, Object> getAll() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            public Object apply(ContainerRequestContext containerRequestContext) {
                return crudRepository.getAll();
            }
        };
    }

    private Inflector<ContainerRequestContext, Object> get() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            public Object apply(ContainerRequestContext containerRequestContext) {
                return crudRepository.getOne((S) containerRequestContext.getUriInfo().getPathParameters().get("id").get(0));
            }
        };
    }

    private Inflector<ContainerRequestContext, Object> post() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            public Object apply(ContainerRequestContext containerRequestContext) {
                try {
                    return crudRepository.save(objectMapper.readValue(containerRequestContext.getEntityStream(), resource));
                } catch (IOException e) {
                    throw new MappingException(resource, e);
                }
            }
        };
    }

    private Inflector<ContainerRequestContext, Object> update() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            public Object apply(ContainerRequestContext containerRequestContext) {
                try {
                    return crudRepository.save((T) updater.update(crudRepository.getOne((S) containerRequestContext.getUriInfo().getPathParameters().get("id").get(0)),
                            objectMapper.readValue(containerRequestContext.getEntityStream(), Map.class)));
                } catch (IOException e) {
                    throw new MappingException(resource, e);
                }
            }
        };
    }

    private Inflector<ContainerRequestContext, Object> delete() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            public Object apply(ContainerRequestContext containerRequestContext) {
                if (crudRepository.delete((S) containerRequestContext.getUriInfo().getPathParameters().get("id").get(0))) {
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        };
    }

}
