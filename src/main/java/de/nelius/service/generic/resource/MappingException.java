package de.nelius.service.generic.resource;

/**
 * Exception occurs when json objects can not be mapped.
 *
 * @author Christian Nelius
 */
public class MappingException extends RuntimeException {

    public MappingException(Class<?> resource, Throwable cause) {
        super("Can not map JSON to " + resource.getSimpleName(), cause);
    }

}
