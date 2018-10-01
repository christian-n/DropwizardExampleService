package de.nelius.service.generic.updater;

import java.io.IOException;
import java.util.Map;

/**
 * Mapper between DTO and Entity
 *
 * @author Christian Nelius
 */
public interface Updater {

    <T> T update(T source, Map<String, Object> dto);

}
