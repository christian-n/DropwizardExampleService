package de.nelius.service.generic.updater;

import java.io.IOException;
import java.util.Map;

/**
 * @author Christian Nelius
 */
public interface Updater {

    <T> T update(T source, Map<String, Object> dto);

}
