package de.nelius.service.generic.updater;

/**
 * @author Christian Nelius
 */
public class UpdaterException extends RuntimeException {


    public UpdaterException(Throwable cause) {
        super("Could not update entity!\nCause:", cause);
    }

}
